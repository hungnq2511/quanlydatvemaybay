package com.quanlydatvemaybay.ui.panels;

import com.quanlydatvemaybay.entity.User;
import com.quanlydatvemaybay.service.AuthService;
import com.quanlydatvemaybay.service.UserService;
import com.quanlydatvemaybay.ui.UIConstants;
import com.quanlydatvemaybay.ui.dialogs.UserDialog;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class UserPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private final UserService userService = new UserService();

    public UserPanel() {
        setBackground(UIConstants.BG_COLOR);
        setLayout(new BorderLayout());
        initUI();
        loadData(null);
    }

    public void refresh() {
        loadData(null);
    }

    private void initUI() {
        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        topBar.setPreferredSize(new Dimension(0, 60));
        JLabel titleLabel = new JLabel("   Quản Lý Người Dùng");
        titleLabel.setFont(UIConstants.TITLE_FONT);
        titleLabel.setForeground(UIConstants.PRIMARY_DARK);
        topBar.add(titleLabel, BorderLayout.WEST);

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(235, 235, 235)));

        txtSearch = new JTextField();
        txtSearch.setFont(UIConstants.NORMAL_FONT);
        txtSearch.setPreferredSize(new Dimension(240, UIConstants.INPUT_HEIGHT));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        txtSearch.setToolTipText("Tìm theo tên đăng nhập hoặc họ tên...");

        JButton btnSearch  = createButton("Tìm kiếm", UIConstants.PRIMARY_COLOR);
        JButton btnRefresh = createButton("Làm mới", new Color(100, 100, 100));
        JButton btnAdd     = createButton("+ Thêm người dùng", UIConstants.SECONDARY_COLOR);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(new JLabel("Tìm kiếm:") {{ setFont(UIConstants.NORMAL_FONT); }});
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        addPanel.setBackground(Color.WHITE);
        addPanel.add(btnAdd);

        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(addPanel, BorderLayout.EAST);

        // Table
        String[] columns = {"ID", "Tên đăng nhập", "Họ và tên", "Email", "Số ĐT", "Vai trò", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        styleTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.add(topBar, BorderLayout.NORTH);
        headerContainer.add(toolbar, BorderLayout.SOUTH);

        add(headerContainer, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(createBottomBar(), BorderLayout.SOUTH);

        // Actions
        btnSearch.addActionListener(e -> performSearch());
        txtSearch.addActionListener(e -> performSearch());
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); loadData(null); });
        btnAdd.addActionListener(e -> openAddDialog());

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openEditDialog();
            }
        });
    }

    private JPanel createBottomBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(235, 235, 235)));

        JButton btnEdit       = createButton("Sửa", UIConstants.PRIMARY_COLOR);
        JButton btnChangeRole = createButton("Đổi Role", new Color(142, 68, 173));
        JButton btnToggle     = createButton("Khóa / Mở", UIConstants.WARNING_COLOR);
        JButton btnPassword   = createButton("Đổi mật khẩu", new Color(52, 152, 219));
        JButton btnDelete     = createButton("Xóa", UIConstants.DANGER_COLOR);

        btnEdit.addActionListener(e -> openEditDialog());
        btnChangeRole.addActionListener(e -> openChangeRoleDialog());
        btnToggle.addActionListener(e -> toggleStatus());
        btnPassword.addActionListener(e -> openChangePasswordDialog());
        btnDelete.addActionListener(e -> deleteSelected());

        panel.add(btnEdit);
        panel.add(btnChangeRole);
        panel.add(btnPassword);
        panel.add(btnToggle);
        panel.add(btnDelete);
        return panel;
    }

    private void styleTable() {
        table.setFont(UIConstants.NORMAL_FONT);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        UIConstants.applyTableHeaderStyle(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(210, 230, 255));

        int[] widths = {55, 140, 170, 200, 120, 90, 100};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? Color.WHITE : UIConstants.TABLE_ROW_ALT);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!sel) {
                    String val = v != null ? v.toString() : "";
                    if (val.equals("ADMIN"))      setForeground(UIConstants.PRIMARY_COLOR);
                    else if (val.equals("Hoạt động")) setForeground(UIConstants.SECONDARY_COLOR);
                    else if (val.equals("Bị khóa"))   setForeground(UIConstants.DANGER_COLOR);
                    else setForeground(Color.DARK_GRAY);
                } else setForeground(Color.WHITE);
                return c;
            }
        });
    }

    private void loadData(String keyword) {
        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            protected List<User> doInBackground() throws Exception {
                return userService.search(keyword);
            }
            protected void done() {
                try {
                    List<User> users = get();
                    tableModel.setRowCount(0);
                    for (User u : users) {
                        tableModel.addRow(new Object[]{
                                u.getId(),
                                u.getUserName(),
                                u.getFullName(),
                                u.getEmail() != null ? u.getEmail() : "",
                                u.getSdt() != null ? u.getSdt() : "",
                                u.getRole() != null ? u.getRole() : "USER",
                                Boolean.TRUE.equals(u.getStatus()) ? "Hoạt động" : "Bị khóa"
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(UserPanel.this,
                            "Lỗi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void performSearch() {
        String kw = txtSearch.getText().trim();
        loadData(kw.isEmpty() ? null : kw);
    }

    private User getSelectedUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một người dùng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        Long id = (Long) tableModel.getValueAt(row, 0);
        try {
            return userService.getById(id).orElse(null);
        } catch (Exception e) { return null; }
    }

    private void openAddDialog() {
        UserDialog dialog = new UserDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) loadData(null);
    }

    private void openEditDialog() {
        User user = getSelectedUser();
        if (user == null) return;
        UserDialog dialog = new UserDialog((Frame) SwingUtilities.getWindowAncestor(this), user);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) loadData(null);
    }

    private void openChangeRoleDialog() {
        User user = getSelectedUser();
        if (user == null) return;

        // Không đổi role của chính mình
        User current = AuthService.getCurrentUser();
        if (current != null && current.getId().equals(user.getId())) {
            JOptionPane.showMessageDialog(this, "Không thể đổi role của tài khoản đang đăng nhập!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] roles = {"USER", "ADMIN"};
        String selected = (String) JOptionPane.showInputDialog(this,
                "Chọn vai trò mới cho '" + user.getUserName() + "':",
                "Đổi vai trò", JOptionPane.QUESTION_MESSAGE, null,
                roles, user.getRole() != null ? user.getRole() : "USER");

        if (selected != null && !selected.equals(user.getRole())) {
            try {
                userService.changeRole(user.getId(), selected);
                loadData(null);
                JOptionPane.showMessageDialog(this, "Đổi vai trò thành công!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openChangePasswordDialog() {
        User user = getSelectedUser();
        if (user == null) return;

        JPasswordField pwField = new JPasswordField();
        pwField.setFont(UIConstants.NORMAL_FONT);
        JPasswordField pwConfirm = new JPasswordField();
        pwConfirm.setFont(UIConstants.NORMAL_FONT);

        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));
        panel.add(new JLabel("Mật khẩu mới (ít nhất 6 ký tự):"));
        panel.add(pwField);
        panel.add(new JLabel("Xác nhận mật khẩu:"));
        panel.add(pwConfirm);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Đổi mật khẩu cho: " + user.getUserName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String pw1 = new String(pwField.getPassword());
            String pw2 = new String(pwConfirm.getPassword());
            if (!pw1.equals(pw2)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                userService.changePassword(user.getId(), pw1);
                JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void toggleStatus() {
        User user = getSelectedUser();
        if (user == null) return;

        boolean isActive = Boolean.TRUE.equals(user.getStatus());
        String action = isActive ? "khóa" : "mở khóa";
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn " + action + " tài khoản '" + user.getUserName() + "'?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                userService.toggleStatus(user.getId());
                loadData(null);
                JOptionPane.showMessageDialog(this,
                        (isActive ? "Khóa" : "Mở khóa") + " tài khoản thành công!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelected() {
        User user = getSelectedUser();
        if (user == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xóa tài khoản '" + user.getUserName() + "'?\nThao tác này không thể hoàn tác!",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                userService.delete(user.getId());
                loadData(null);
                JOptionPane.showMessageDialog(this, "Xóa tài khoản thành công!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(UIConstants.BUTTON_FONT);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, UIConstants.BUTTON_HEIGHT));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
