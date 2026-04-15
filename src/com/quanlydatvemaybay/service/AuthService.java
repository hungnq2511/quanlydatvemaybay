package com.quanlydatvemaybay.service;

import com.quanlydatvemaybay.dao.UserDAO;
import com.quanlydatvemaybay.entity.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();
    private static User currentUser = null;

    public User login(String userName, String password) throws SQLException {
        Optional<User> userOpt = userDAO.findByUserName(userName);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không tồn tại!");
        }
        User user = userOpt.get();
        if (!Boolean.TRUE.equals(user.getStatus())) {
            throw new IllegalArgumentException("Tài khoản đã bị vô hiệu hóa!");
        }

        String hashedPassword = hashPassword(password);
        if (!hashedPassword.equals(user.getPassword()) && !password.equals(user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu không đúng!");
        }

        currentUser = user;
        return user;
    }

    public User register(String userName, String password, String fullName, String email, String sdt) throws SQLException {
        if (userDAO.existsByUserName(userName)) {
            throw new IllegalArgumentException("Tên đăng nhập '" + userName + "' đã tồn tại!");
        }

        User user = new User();
        user.setUserName(userName);
        user.setPassword(hashPassword(password));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setSdt(sdt);
        user.setStatus(true);

        userDAO.save(user);
        userDAO.saveUserRole(user.getId(), "USER");
        user.setRole("USER");
        return user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return password;
        }
    }
}
