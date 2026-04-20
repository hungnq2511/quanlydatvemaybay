package com.quanlydatvemaybay.ui.dialogs;

import com.quanlydatvemaybay.entity.User;
import com.quanlydatvemaybay.service.UserService;
import com.quanlydatvemaybay.ui.UIConstants;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class UserDialog extends JDialog {

    private boolean confirmed = false;
    private final User user; // null = thêm mới
    private final UserService userService = new UserService();

    // Thêm mới
    private JTextField txtUsername;
    private JPasswordField txtPassword, txtConfirmPassword;

    // Thêm + Sửa
    private JTextField txtFullName, txtEmail, txtPhone;
    private JComboBox<String> cmbRole;

    public UserDialog(Frame parent, User user) {
        super(parent, user == null ? "Thêm người dùng mới" : "Sửa người dùng", true);
        this.user = user;
        setSize(480, user == null ? 560 : 440);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
        if (user != null) fillData();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIConstants.BG_COLOR);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.PRIMARY_COLOR);
        header.setPreferredSize(new Dimension(0, 56));
        header.setBorder(new EmptyBorder(0, 25, 0, 25));
        JLabel titleLbl = new JLabel(user == null ? "  Thêm người dùng mới" : "  Cập nhật người dùng");
        titleLbl.setFont(UIConstants.TITLE_FONT);
        titleLbl.setForeground(Color.WHITE);
        header.add(titleLbl, BorderLayout.WEST);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 30, 10, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.gridwidth = 1;
        gbc.weightx = 1;

        int row = 0;

        // Chỉ hiển thị username/password khi thêm mới
        if (user == null) {
            row = addField(form, gbc, row, "Tên đăng nhập *",
                    txtUsername = createTextField("Ít nhất 4 ký tự"));

            row = addField(form, gbc, row, "Mật khẩu *",
                    txtPassword = createPasswordField("Ít nhất 6 ký tự"));

            row = addField(form, gbc, row, "Xác nhận mật khẩu *",
                    txtConfirmPassword = createPasswordField("Nhập lại mật khẩu"));
        }

        row = addField(form, gbc, row, "Họ và tên *",
                txtFullName = createTextField("Nguyễn Văn A"));

        row = addField(form, gbc, row, "Email",
                txtEmail = createTextField("example@email.com"));

        row = addField(form, gbc, row, "Số điện thoại",
                txtPhone = createTextField("0901234567"));

        // Role
        gbc.gridy = row; gbc.gridx = 0;
        JLabel lblRole = new JLabel("Vai trò *");
        lblRole.setFont(UIConstants.SMALL_FONT);
        lblRole.setForeground(new Color(80, 80, 80));
        form.add(lblRole, gbc);

        row++;
        gbc.gridy = row; gbc.gridx = 0;
        cmbRole = new JComboBox<>(new String[]{"USER", "ADMIN"});
        cmbRole.setFont(UIConstants.NORMAL_FONT);
        cmbRole.setPreferredSize(new Dimension(0, UIConstants.INPUT_HEIGHT));
        form.add(cmbRole, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JButton btnCancel = makeBtn("Hủy", new Color(150, 150, 150));
        JButton btnSave = makeBtn(user == null ? "Thêm mới" : "Lưu thay đổi", UIConstants.PRIMARY_COLOR);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> save());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        main.add(header, BorderLayout.NORTH);
        main.add(new JScrollPane(form), BorderLayout.CENTER);
        main.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(main);

        getRootPane().setDefaultButton(btnSave);
    }

    private int addField(JPanel form, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridy = row; gbc.gridx = 0;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UIConstants.SMALL_FONT);
        lbl.setForeground(new Color(80, 80, 80));
        form.add(lbl, gbc);

        row++;
        gbc.gridy = row; gbc.gridx = 0;
        form.add(field, gbc);
        return row + 1;
    }

    private JTextField createTextField(String tooltip) {
        JTextField tf = new JTextField();
        tf.setFont(UIConstants.NORMAL_FONT);
        tf.setPreferredSize(new Dimension(0, UIConstants.INPUT_HEIGHT));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        tf.setToolTipText(tooltip);
        return tf;
    }

    private JPasswordField createPasswordField(String tooltip) {
        JPasswordField pf = new JPasswordField();
        pf.setFont(UIConstants.NORMAL_FONT);
        pf.setPreferredSize(new Dimension(0, UIConstants.INPUT_HEIGHT));
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        pf.setToolTipText(tooltip);
        return pf;
    }

    private void fillData() {
        txtFullName.setText(user.getFullName());
        txtEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        txtPhone.setText(user.getSdt() != null ? user.getSdt() : "");
        if (user.getRole() != null) cmbRole.setSelectedItem(user.getRole());
        // Không cho đổi role của chính mình
        com.quanlydatvemaybay.entity.User current = com.quanlydatvemaybay.service.AuthService.getCurrentUser();
        if (current != null && current.getId().equals(user.getId())) {
            cmbRole.setEnabled(false);
        }
    }

    private void save() {
        String fullName = txtFullName.getText().trim();
        String email    = txtEmail.getText().trim();
        String phone    = txtPhone.getText().trim();
        String role     = (String) cmbRole.getSelectedItem();

        if (fullName.isEmpty()) {
            showError("Vui lòng nhập họ và tên!");
            return;
        }

        try {
            if (user == null) {
                // Thêm mới
                String username = txtUsername.getText().trim();
                String password = new String(txtPassword.getPassword());
                String confirm  = new String(txtConfirmPassword.getPassword());

                if (username.isEmpty() || password.isEmpty()) {
                    showError("Vui lòng điền đầy đủ thông tin bắt buộc (*)");
                    return;
                }
                if (!password.equals(confirm)) {
                    showError("Mật khẩu xác nhận không khớp!");
                    return;
                }

                userService.create(username, password, fullName, email, phone, role);
                JOptionPane.showMessageDialog(this, "Thêm người dùng thành công!", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Cập nhật thông tin
                userService.update(user.getId(), fullName, email, phone);

                // Đổi role nếu thay đổi và combo được bật
                if (cmbRole.isEnabled() && !role.equals(user.getRole())) {
                    userService.changeRole(user.getId(), role);
                }

                JOptionPane.showMessageDialog(this, "Cập nhật thành công!", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            confirmed = true;
            dispose();
        } catch (Exception e) {
            showError("Lỗi: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private JButton makeBtn(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(UIConstants.BUTTON_FONT);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(130, UIConstants.BUTTON_HEIGHT));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public boolean isConfirmed() { return confirmed; }
}
