package com.quanlydatvemaybay.service;

import com.quanlydatvemaybay.dao.UserDAO;
import com.quanlydatvemaybay.entity.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public List<User> getAll() throws SQLException {
        return userDAO.findAllWithRoles();
    }

    public List<User> search(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank()) return getAll();
        return userDAO.search(keyword);
    }

    public Optional<User> getById(Long id) throws SQLException {
        return userDAO.findById(id);
    }

    /** Admin tạo tài khoản mới với role tùy chọn */
    public User create(String userName, String password, String fullName,
                       String email, String sdt, String role) throws SQLException {
        if (userName == null || userName.isBlank())
            throw new IllegalArgumentException("Tên đăng nhập không được để trống!");
        if (userName.length() < 4)
            throw new IllegalArgumentException("Tên đăng nhập phải có ít nhất 4 ký tự!");
        if (password == null || password.length() < 6)
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự!");
        if (userDAO.existsByUserName(userName))
            throw new IllegalArgumentException("Tên đăng nhập '" + userName + "' đã tồn tại!");

        User user = new User();
        user.setUserName(userName);
        user.setPassword(hashPassword(password));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setSdt(sdt);
        user.setStatus(true);

        userDAO.save(user);
        String roleId = "ADMIN".equalsIgnoreCase(role) ? "ADMIN" : "USER";
        userDAO.saveUserRole(user.getId(), roleId);
        user.setRole(roleId);
        return user;
    }

    /** Cập nhật thông tin cá nhân (không đổi username/role) */
    public void update(Long id, String fullName, String email, String sdt) throws SQLException {
        User user = userDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID=" + id));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setSdt(sdt);
        userDAO.update(user);
    }

    /** Đổi mật khẩu */
    public void changePassword(Long id, String newPassword) throws SQLException {
        if (newPassword == null || newPassword.length() < 6)
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự!");
        userDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID=" + id));
        userDAO.updatePassword(id, hashPassword(newPassword));
    }

    /** Đổi role (ADMIN / USER) */
    public void changeRole(Long id, String newRole) throws SQLException {
        User user = userDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID=" + id));

        // Không được hạ role admin cuối cùng
        if ("ADMIN".equals(user.getRole()) && "USER".equalsIgnoreCase(newRole)) {
            long adminCount = userDAO.countAdmins();
            if (adminCount <= 1)
                throw new IllegalArgumentException("Không thể hạ quyền Admin cuối cùng trong hệ thống!");
        }

        // Không đổi role của chính mình
        User currentUser = AuthService.getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(id))
            throw new IllegalArgumentException("Không thể đổi role của tài khoản đang đăng nhập!");

        String roleId = "ADMIN".equalsIgnoreCase(newRole) ? "ADMIN" : "USER";
        userDAO.updateRole(id, roleId);
    }

    /** Khóa / Mở khóa tài khoản */
    public void toggleStatus(Long id) throws SQLException {
        User user = userDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID=" + id));

        // Không khóa chính mình
        User currentUser = AuthService.getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(id))
            throw new IllegalArgumentException("Không thể khóa tài khoản đang đăng nhập!");

        boolean newStatus = !Boolean.TRUE.equals(user.getStatus());
        userDAO.updateStatus(id, newStatus);
    }

    /** Xóa người dùng */
    public void delete(Long id) throws SQLException {
        User user = userDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID=" + id));

        // Không xóa chính mình
        User currentUser = AuthService.getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(id))
            throw new IllegalArgumentException("Không thể xóa tài khoản đang đăng nhập!");

        // Không xóa admin cuối cùng
        if ("ADMIN".equals(user.getRole()) && userDAO.countAdmins() <= 1)
            throw new IllegalArgumentException("Không thể xóa Admin cuối cùng trong hệ thống!");

        userDAO.delete(id);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return password;
        }
    }
}
