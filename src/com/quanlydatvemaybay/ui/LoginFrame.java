package com.quanlydatvemaybay.ui;

import com.quanlydatvemaybay.entity.User;
import com.quanlydatvemaybay.service.AuthService;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblError;

    public LoginFrame() {
        setTitle("Quản Lý Đặt Vé Máy Bay - Đăng Nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 580);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIConstants.BG_COLOR);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(420, 140));
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel titleIcon = new JLabel("✈", SwingConstants.CENTER);
        titleIcon.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        titleIcon.setForeground(Color.WHITE);

        JLabel titleLabel = new JLabel("QUẢN LÝ ĐẶT VÉ", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Hệ thống quản lý vé máy bay", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(200, 230, 255));

        JPanel titlePanel = new JPanel(new GridLayout(3, 1, 0, 4));
        titlePanel.setBackground(UIConstants.PRIMARY_COLOR);
        titlePanel.add(titleIcon);
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        headerPanel.add(titlePanel, BorderLayout.CENTER);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblUsername = new JLabel("Tên đăng nhập");
        lblUsername.setFont(UIConstants.HEADER_FONT);
        lblUsername.setForeground(UIConstants.PRIMARY_DARK);
        formPanel.add(lblUsername, gbc);

        gbc.gridy = 1;
        txtUsername = new JTextField();
        txtUsername.setFont(UIConstants.NORMAL_FONT);
        txtUsername.setPreferredSize(new Dimension(320, UIConstants.INPUT_HEIGHT));
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        formPanel.add(txtUsername, gbc);

        // Password
        gbc.gridy = 2; gbc.insets = new Insets(14, 0, 6, 0);
        JLabel lblPassword = new JLabel("Mật khẩu");
        lblPassword.setFont(UIConstants.HEADER_FONT);
        lblPassword.setForeground(UIConstants.PRIMARY_DARK);
        formPanel.add(lblPassword, gbc);

        gbc.gridy = 3; gbc.insets = new Insets(6, 0, 6, 0);
        txtPassword = new JPasswordField();
        txtPassword.setFont(UIConstants.NORMAL_FONT);
        txtPassword.setPreferredSize(new Dimension(320, UIConstants.INPUT_HEIGHT));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        formPanel.add(txtPassword, gbc);

        // Error label
        gbc.gridy = 4; gbc.insets = new Insets(6, 0, 0, 0);
        lblError = new JLabel(" ");
        lblError.setFont(UIConstants.SMALL_FONT);
        lblError.setForeground(UIConstants.DANGER_COLOR);
        formPanel.add(lblError, gbc);

        // Login button
        gbc.gridy = 5; gbc.insets = new Insets(16, 0, 6, 0);
        btnLogin = new JButton("ĐĂNG NHẬP");
        btnLogin.setFont(UIConstants.BUTTON_FONT);
        btnLogin.setBackground(UIConstants.PRIMARY_COLOR);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setOpaque(true);
        btnLogin.setPreferredSize(new Dimension(320, UIConstants.BUTTON_HEIGHT + 4));
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        formPanel.add(btnLogin, gbc);

        // Register button
        gbc.gridy = 6; gbc.insets = new Insets(0, 0, 8, 0);
        JButton btnRegister = new JButton("ĐĂNG KÝ TÀI KHOẢN");
        btnRegister.setFont(UIConstants.BUTTON_FONT);
        btnRegister.setBackground(UIConstants.SECONDARY_COLOR);
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setOpaque(true);
        btnRegister.setPreferredSize(new Dimension(320, UIConstants.BUTTON_HEIGHT + 4));
        btnRegister.setBorderPainted(false);
        btnRegister.setFocusPainted(false);
        btnRegister.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        formPanel.add(btnRegister, gbc);

        // Hover effect
        btnLogin.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnLogin.setBackground(UIConstants.PRIMARY_DARK); }
            public void mouseExited(MouseEvent e) { btnLogin.setBackground(UIConstants.PRIMARY_COLOR); }
        });

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);

        // Actions
        btnLogin.addActionListener(e -> doLogin());
        txtPassword.addActionListener(e -> doLogin());
        txtUsername.addActionListener(e -> txtPassword.requestFocus());
        btnRegister.addActionListener(e -> {
            RegisterDialog dialog = new RegisterDialog(LoginFrame.this);
            dialog.setVisible(true);
        });
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");
        lblError.setText(" ");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                AuthService authService = new AuthService();
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    dispose();
                    SwingUtilities.invokeLater(() -> {
                        MainFrame mainFrame = new MainFrame();
                        mainFrame.setVisible(true);
                    });
                } catch (Exception e) {
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    lblError.setText(msg != null ? msg : "Đăng nhập thất bại!");
                } finally {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("ĐĂNG NHẬP");
                }
            }
        };
        worker.execute();
    }
}
