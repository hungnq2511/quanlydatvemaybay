package com.quanlydatvemaybay.ui;

import com.quanlydatvemaybay.service.AuthService;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class RegisterDialog extends JDialog {

    private JTextField txtUsername, txtFullName, txtEmail, txtPhone;
    private JPasswordField txtPassword, txtConfirmPassword;
    private JLabel lblError;

    public RegisterDialog(Frame parent) {
        super(parent, "Đăng Ký Tài Khoản", true);
        setSize(440, 560);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIConstants.BG_COLOR);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.SECONDARY_COLOR);
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(new EmptyBorder(0, 25, 0, 25));
        JLabel title = new JLabel("Tạo tài khoản mới");
        title.setFont(UIConstants.TITLE_FONT);
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 30, 10, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx = 0; gbc.weightx = 1;

        txtUsername    = addField(form, gbc, 0, "Tên đăng nhập *");
        txtPassword    = addPasswordField(form, gbc, 1, "Mật khẩu *");
        txtConfirmPassword = addPasswordField(form, gbc, 2, "Xác nhận mật khẩu *");
        txtFullName    = addField(form, gbc, 3, "Họ và tên *");
        txtEmail       = addField(form, gbc, 4, "Email");
        txtPhone       = addField(form, gbc, 5, "Số điện thoại");

        gbc.gridy = 6;
        lblError = new JLabel(" ");
        lblError.setFont(UIConstants.SMALL_FONT);
        lblError.setForeground(UIConstants.DANGER_COLOR);
        form.add(lblError, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JButton btnCancel = createBtn("Hủy", new Color(150, 150, 150));
        JButton btnRegister = createBtn("Đăng ký", UIConstants.SECONDARY_COLOR);
        btnCancel.addActionListener(e -> dispose());
        btnRegister.addActionListener(e -> doRegister());

        btnPanel.add(btnCancel);
        btnPanel.add(btnRegister);

        main.add(header, BorderLayout.NORTH);
        main.add(form, BorderLayout.CENTER);
        main.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(main);

        getRootPane().setDefaultButton(btnRegister);
    }

    private JTextField addField(JPanel form, GridBagConstraints gbc, int row, String label) {
        gbc.gridy = row * 2;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.NORMAL_FONT);
        lbl.setForeground(UIConstants.PRIMARY_DARK);
        form.add(lbl, gbc);

        gbc.gridy = row * 2 + 1;
        JTextField tf = new JTextField();
        tf.setFont(UIConstants.NORMAL_FONT);
        tf.setPreferredSize(new Dimension(340, UIConstants.INPUT_HEIGHT));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        form.add(tf, gbc);
        return tf;
    }

    private JPasswordField addPasswordField(JPanel form, GridBagConstraints gbc, int row, String label) {
        gbc.gridy = row * 2;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.NORMAL_FONT);
        lbl.setForeground(UIConstants.PRIMARY_DARK);
        form.add(lbl, gbc);

        gbc.gridy = row * 2 + 1;
        JPasswordField pf = new JPasswordField();
        pf.setFont(UIConstants.NORMAL_FONT);
        pf.setPreferredSize(new Dimension(340, UIConstants.INPUT_HEIGHT));
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        form.add(pf, gbc);
        return pf;
    }

    private void doRegister() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirm  = new String(txtConfirmPassword.getPassword());
        String fullName = txtFullName.getText().trim();
        String email    = txtEmail.getText().trim();
        String phone    = txtPhone.getText().trim();

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            lblError.setText("Vui lòng điền đầy đủ thông tin bắt buộc (*)");
            return;
        }
        if (username.length() < 4) {
            lblError.setText("Tên đăng nhập phải có ít nhất 4 ký tự!");
            return;
        }
        if (password.length() < 6) {
            lblError.setText("Mật khẩu phải có ít nhất 6 ký tự!");
            return;
        }
        if (!password.equals(confirm)) {
            lblError.setText("Mật khẩu xác nhận không khớp!");
            return;
        }

        lblError.setText(" ");
        try {
            AuthService authService = new AuthService();
            authService.register(username, password, fullName,
                    email.isEmpty() ? null : email,
                    phone.isEmpty() ? null : phone);
            JOptionPane.showMessageDialog(this,
                    "Đăng ký thành công! Bạn có thể đăng nhập ngay.",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception e) {
            lblError.setText(e.getMessage());
        }
    }

    private JButton createBtn(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(UIConstants.BUTTON_FONT);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(110, UIConstants.BUTTON_HEIGHT));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
