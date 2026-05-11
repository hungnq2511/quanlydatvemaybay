package com.quanlydatvemaybay.ui.panels;

import com.quanlydatvemaybay.dao.AirlineDAO;
import com.quanlydatvemaybay.entity.Airline;
import com.quanlydatvemaybay.ui.UIConstants;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class AirlinePanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private final AirlineDAO airlineDAO = new AirlineDAO();

    public AirlinePanel() {
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
        JLabel title = new JLabel("   Quản Lý Hãng Bay");
        title.setFont(UIConstants.TITLE_FONT);
        title.setForeground(UIConstants.PRIMARY_DARK);
        topBar.add(title, BorderLayout.WEST);

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(235, 235, 235)));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        searchPanel.setBackground(Color.WHITE);
        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UIConstants.NORMAL_FONT);
        txtSearch = new JTextField();
        txtSearch.setFont(UIConstants.NORMAL_FONT);
        txtSearch.setPreferredSize(new Dimension(220, UIConstants.INPUT_HEIGHT));
        JButton btnSearch = createBtn("Tìm", UIConstants.PRIMARY_COLOR, 80);
        JButton btnRefresh = createBtn("Làm mới", new Color(100, 100, 100), 90);
        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        addPanel.setBackground(Color.WHITE);
        JButton btnAdd = createBtn("+ Thêm hãng bay", UIConstants.SECONDARY_COLOR, 140);
        addPanel.add(btnAdd);

        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(addPanel, BorderLayout.EAST);

        // Table
        String[] cols = {"ID", "Mã hãng", "Tên hãng bay"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        // Bottom bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setBackground(Color.WHITE);
        bottomBar.setBorder(new MatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        JButton btnEdit = createBtn("Sửa", new Color(39, 174, 96), 100);
        JButton btnDelete = createBtn("Xóa", new Color(192, 57, 43), 100);
        bottomBar.add(btnEdit);
        bottomBar.add(btnDelete);

        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.add(topBar, BorderLayout.NORTH);
        headerContainer.add(toolbar, BorderLayout.SOUTH);
        add(headerContainer, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        // Listeners
        btnSearch.addActionListener(e -> search());
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); loadData(); });
        txtSearch.addActionListener(e -> search());
        btnAdd.addActionListener(e -> openDialog(null));
        btnEdit.addActionListener(e -> openDialog(getSelected()));
        btnDelete.addActionListener(e -> deleteSelected());
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openDialog(getSelected());
            }
        });
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            for (Airline a : airlineDAO.findAll()) {
                tableModel.addRow(new Object[]{a.getId(), a.getCode(), a.getName()});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void search() {
        String kw = txtSearch.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        try {
            for (Airline a : airlineDAO.findAll()) {
                if (kw.isEmpty() || a.getCode().toLowerCase().contains(kw) || a.getName().toLowerCase().contains(kw)) {
                    tableModel.addRow(new Object[]{a.getId(), a.getCode(), a.getName()});
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openDialog(Airline airline) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, airline == null ? "Thêm hãng bay" : "Sửa hãng bay", true);
        dialog.setSize(400, 240);
        dialog.setLocationRelativeTo(owner);
        dialog.setResizable(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 30, 10, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        JTextField txtCode = new JTextField(airline != null ? airline.getCode() : "");
        JTextField txtName = new JTextField(airline != null ? airline.getName() : "");
        styleField(txtCode); styleField(txtName);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; form.add(formLabel("Mã hãng *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; form.add(txtCode, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; form.add(formLabel("Tên hãng bay *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; form.add(txtName, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));
        JButton btnCancel = createBtn("Hủy", new Color(150, 150, 150), 90);
        JButton btnSave   = createBtn(airline == null ? "Thêm" : "Lưu", UIConstants.SECONDARY_COLOR, 90);
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String code = txtCode.getText().trim().toUpperCase();
            String name = txtName.getText().trim();
            if (code.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
            }
            if (code.length() > 10) {
                JOptionPane.showMessageDialog(dialog, "Mã hãng tối đa 10 ký tự!", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
            }
            try {
                if (airline == null) {
                    if (airlineDAO.existsByCode(code)) {
                        JOptionPane.showMessageDialog(dialog, "Mã hãng \"" + code + "\" đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
                    }
                    airlineDAO.save(new Airline(null, code, name));
                    JOptionPane.showMessageDialog(dialog, "Thêm hãng bay thành công!");
                } else {
                    if (airlineDAO.existsByCodeExcludeId(code, airline.getId())) {
                        JOptionPane.showMessageDialog(dialog, "Mã hãng \"" + code + "\" đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
                    }
                    airlineDAO.update(new Airline(airline.getId(), code, name));
                    JOptionPane.showMessageDialog(dialog, "Cập nhật thành công!");
                }
                loadData();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        JPanel main = new JPanel(new BorderLayout());
        main.add(form, BorderLayout.CENTER);
        main.add(btnPanel, BorderLayout.SOUTH);
        dialog.setContentPane(main);
        dialog.getRootPane().setDefaultButton(btnSave);
        dialog.setVisible(true);
    }

    private void deleteSelected() {
        Airline selected = getSelected();
        if (selected == null) { JOptionPane.showMessageDialog(this, "Vui lòng chọn hãng bay cần xóa!"); return; }
        int ok = JOptionPane.showConfirmDialog(this,
                "Xóa hãng bay \"" + selected.getName() + "\"?\nLưu ý: không thể xóa nếu đang có chuyến bay sử dụng.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            airlineDAO.delete(selected.getId());
            loadData();
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Airline getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Long id     = (Long) tableModel.getValueAt(row, 0);
        String code = (String) tableModel.getValueAt(row, 1);
        String name = (String) tableModel.getValueAt(row, 2);
        return new Airline(id, code, name);
    }

    private void styleTable() {
        table.setFont(UIConstants.NORMAL_FONT);
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(UIConstants.SMALL_FONT.deriveFont(Font.BOLD));
        table.getTableHeader().setBackground(UIConstants.PRIMARY_DARK);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.BLACK);

        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setPreferredWidth(60);
        cm.getColumn(1).setPreferredWidth(100);
        cm.getColumn(2).setPreferredWidth(300);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(sel ? new Color(232, 240, 254) : (r % 2 == 0 ? Color.WHITE : new Color(249, 249, 249)));
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        });
    }

    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIConstants.NORMAL_FONT);
        return l;
    }

    private void styleField(JTextField tf) {
        tf.setFont(UIConstants.NORMAL_FONT);
        tf.setPreferredSize(new Dimension(200, UIConstants.INPUT_HEIGHT));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    }

    private JButton createBtn(String text, Color color, int width) {
        JButton btn = new JButton(text);
        btn.setFont(UIConstants.NORMAL_FONT);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(width, UIConstants.BUTTON_HEIGHT));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
