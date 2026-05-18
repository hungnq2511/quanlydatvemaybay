package com.quanlydatvemaybay.ui.panels;

import com.quanlydatvemaybay.entity.User;
import com.quanlydatvemaybay.service.AuthService;
import com.quanlydatvemaybay.service.UserService;
import com.quanlydatvemaybay.ui.UIConstants;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Panel "Tài khoản của tôi":
 *  - Tab 1: Sửa thông tin cá nhân (fullName, email, SĐT).
 *  - Tab 2: Đổi mật khẩu (xác thực pass cũ).
 *  Username không cho phép đổi (định danh chính).
 */
public class MyAccountPanel extends JPanel {

    private static final java.util.regex.Pattern EMAIL_PATTERN =
            java.util.regex.Pattern.compile("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    private static final java.util.regex.Pattern PHONE_PATTERN =
            java.util.regex.Pattern.compile("^(0[3-9][0-9]{8}|\\+84[3-9][0-9]{8})$");

    private final UserService userService = new UserService();

    private JTextField txtFullName, txtEmail, txtPhone, txtUserName;
    private JPasswordField txtOldPass, txtNewPass, txtConfirmPass;

    public MyAccountPanel() {
        setBackground(UIConstants.BG_COLOR);
        setLayout(new BorderLayout());
        initUI();
        loadData();
    }

    public void refresh() { loadData(); }

    private void initUI() {
        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        topBar.setPreferredSize(new Dimension(0, 60));
        JLabel title = new JLabel("   Tài Khoản Của Tôi");
        title.setFont(UIConstants.TITLE_FONT);
        title.setForeground(UIConstants.PRIMARY_DARK);
        topBar.add(title, BorderLayout.WEST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIConstants.NORMAL_FONT);
        tabs.setBackground(UIConstants.BG_COLOR);
        tabs.addTab("  Thông tin cá nhân  ", buildProfileTab());
        tabs.addTab("  Đổi mật khẩu  ", buildPasswordTab());

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UIConstants.BG_COLOR);
        center.setBorder(new EmptyBorder(16, 24, 16, 24));
        center.add(tabs, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    // ── Tab Thông tin ──────────────────────────────────────────────────────
    private JPanel buildProfileTab() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(24, 30, 16, 30));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        txtUserName = addReadOnly(form, gbc, row++, "Tên đăng nhập");
        txtFullName = addField(form, gbc, row++, "Họ tên *");
        txtEmail    = addField(form, gbc, row++, "Email");
        txtPhone    = addField(form, gbc, row++, "Số điện thoại");

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JButton btnReload = button("Hoàn tác", new Color(120, 120, 120));
        JButton btnSave = button("Lưu thay đổi", UIConstants.PRIMARY_COLOR);
        btnReload.addActionListener(e -> loadData());
        btnSave.addActionListener(e -> saveProfile());
        btnPanel.add(btnReload);
        btnPanel.add(btnSave);

        main.add(form, BorderLayout.NORTH);
        main.add(btnPanel, BorderLayout.SOUTH);
        return main;
    }

    private void saveProfile() {
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();

        if (fullName.isEmpty()) { err("Vui lòng nhập họ tên!"); txtFullName.requestFocus(); return; }
        if (fullName.length() < 2) { err("Họ tên phải có ít nhất 2 ký tự!"); txtFullName.requestFocus(); return; }
        if (fullName.matches(".*[0-9].*")) { err("Họ tên không được chứa chữ số!"); txtFullName.requestFocus(); return; }
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            err("Email không đúng định dạng!"); txtEmail.requestFocus(); return;
        }
        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            err("Số điện thoại không hợp lệ! VD: 0901234567"); txtPhone.requestFocus(); return;
        }

        User current = AuthService.getCurrentUser();
        if (current == null) { err("Phiên đăng nhập không hợp lệ!"); return; }

        try {
            userService.update(current.getId(),
                    fullName,
                    email.isEmpty() ? null : email,
                    phone.isEmpty() ? null : phone);
            // Cập nhật state in-memory để hiển thị đồng bộ ngay
            current.setFullName(fullName);
            current.setEmail(email);
            current.setSdt(phone);
            JOptionPane.showMessageDialog(this,
                    "Cập nhật thông tin thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            err("Lỗi: " + ex.getMessage());
        }
    }

    // ── Tab Đổi mật khẩu ───────────────────────────────────────────────────
    private JPanel buildPasswordTab() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(24, 30, 16, 30));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        txtOldPass     = addPasswordField(form, gbc, row++, "Mật khẩu hiện tại *");
        txtNewPass     = addPasswordField(form, gbc, row++, "Mật khẩu mới *");
        txtConfirmPass = addPasswordField(form, gbc, row++, "Xác nhận mật khẩu mới *");

        // Note
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JLabel note = new JLabel("<html><i>* Mật khẩu mới phải có ít nhất 6 ký tự và khác mật khẩu hiện tại.</i></html>");
        note.setFont(UIConstants.SMALL_FONT);
        note.setForeground(new Color(120, 120, 120));
        form.add(note, gbc);
        gbc.gridwidth = 1;

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JButton btnClear = button("Xóa", new Color(120, 120, 120));
        JButton btnChange = button("Đổi mật khẩu", new Color(192, 57, 43));
        btnClear.addActionListener(e -> {
            txtOldPass.setText(""); txtNewPass.setText(""); txtConfirmPass.setText("");
        });
        btnChange.addActionListener(e -> changePassword());
        btnPanel.add(btnClear);
        btnPanel.add(btnChange);

        main.add(form, BorderLayout.NORTH);
        main.add(btnPanel, BorderLayout.SOUTH);
        return main;
    }

    private void changePassword() {
        String oldPass = new String(txtOldPass.getPassword());
        String newPass = new String(txtNewPass.getPassword());
        String confirmPass = new String(txtConfirmPass.getPassword());

        if (oldPass.isEmpty()) { err("Vui lòng nhập mật khẩu hiện tại!"); txtOldPass.requestFocus(); return; }
        if (newPass.isEmpty()) { err("Vui lòng nhập mật khẩu mới!"); txtNewPass.requestFocus(); return; }
        if (newPass.length() < 6) { err("Mật khẩu mới phải có ít nhất 6 ký tự!"); txtNewPass.requestFocus(); return; }
        if (!newPass.equals(confirmPass)) {
            err("Mật khẩu xác nhận không khớp!"); txtConfirmPass.requestFocus(); return;
        }

        User current = AuthService.getCurrentUser();
        if (current == null) { err("Phiên đăng nhập không hợp lệ!"); return; }

        try {
            userService.changeOwnPassword(current.getId(), oldPass, newPass);
            txtOldPass.setText(""); txtNewPass.setText(""); txtConfirmPass.setText("");
            JOptionPane.showMessageDialog(this,
                    "Đổi mật khẩu thành công! Lần đăng nhập tiếp theo hãy dùng mật khẩu mới.",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            err(ex.getMessage());
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private void loadData() {
        User u = AuthService.getCurrentUser();
        if (u == null) return;
        if (txtUserName != null) txtUserName.setText(u.getUserName() != null ? u.getUserName() : "");
        if (txtFullName != null) txtFullName.setText(u.getFullName() != null ? u.getFullName() : "");
        if (txtEmail    != null) txtEmail.setText(u.getEmail() != null ? u.getEmail() : "");
        if (txtPhone    != null) txtPhone.setText(u.getSdt() != null ? u.getSdt() : "");
    }

    private JTextField addField(JPanel form, GridBagConstraints gbc, int row, String label) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel l = new JLabel(label);
        l.setFont(UIConstants.NORMAL_FONT);
        l.setPreferredSize(new Dimension(180, UIConstants.INPUT_HEIGHT));
        form.add(l, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        JTextField tf = new JTextField();
        tf.setFont(UIConstants.NORMAL_FONT);
        tf.setPreferredSize(new Dimension(360, UIConstants.INPUT_HEIGHT));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        form.add(tf, gbc);
        return tf;
    }

    private JTextField addReadOnly(JPanel form, GridBagConstraints gbc, int row, String label) {
        JTextField tf = addField(form, gbc, row, label);
        tf.setEditable(false);
        tf.setBackground(new Color(245, 245, 245));
        tf.setForeground(new Color(80, 80, 80));
        return tf;
    }

    private JPasswordField addPasswordField(JPanel form, GridBagConstraints gbc, int row, String label) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel l = new JLabel(label);
        l.setFont(UIConstants.NORMAL_FONT);
        l.setPreferredSize(new Dimension(180, UIConstants.INPUT_HEIGHT));
        form.add(l, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        JPasswordField pf = new JPasswordField();
        pf.setFont(UIConstants.NORMAL_FONT);
        pf.setPreferredSize(new Dimension(360, UIConstants.INPUT_HEIGHT));
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        form.add(pf, gbc);
        return pf;
    }

    private JButton button(String text, Color color) {
        JButton b = new JButton(text);
        b.setFont(UIConstants.BUTTON_FONT);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(150, UIConstants.BUTTON_HEIGHT));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void err(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
