package com.quanlydatvemaybay.ui.panels;

import com.quanlydatvemaybay.entity.Flight;
import com.quanlydatvemaybay.enums.FlightStatus;
import com.quanlydatvemaybay.service.FlightService;
import com.quanlydatvemaybay.ui.UIConstants;
import com.quanlydatvemaybay.ui.dialogs.FlightDialog;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FlightPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbSearchType;
    private final FlightService flightService = new FlightService();
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public FlightPanel() {
        setBackground(UIConstants.BG_COLOR);
        setLayout(new BorderLayout());
        initUI();
        loadData(null, null, null);
    }

    public void refresh() {
        loadData(null, null, null);
    }

    private void initUI() {
        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        topBar.setPreferredSize(new Dimension(0, 60));
        JLabel titleLabel = new JLabel("   Quản Lý Chuyến Bay");
        titleLabel.setFont(UIConstants.TITLE_FONT);
        titleLabel.setForeground(UIConstants.PRIMARY_DARK);
        topBar.add(titleLabel, BorderLayout.WEST);

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(235, 235, 235)));

        cmbSearchType = new JComboBox<>(new String[]{"Điểm đi", "Điểm đến", "Hãng bay"});
        cmbSearchType.setFont(UIConstants.NORMAL_FONT);
        cmbSearchType.setPreferredSize(new Dimension(110, UIConstants.INPUT_HEIGHT));

        txtSearch = new JTextField();
        txtSearch.setFont(UIConstants.NORMAL_FONT);
        txtSearch.setPreferredSize(new Dimension(220, UIConstants.INPUT_HEIGHT));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        txtSearch.setToolTipText("Tìm kiếm...");

        JButton btnSearch = createButton("Tìm kiếm", UIConstants.PRIMARY_COLOR);
        JButton btnRefresh = createButton("Làm mới", new Color(100, 100, 100));
        JButton btnAdd = createButton("+ Thêm chuyến bay", UIConstants.SECONDARY_COLOR);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(cmbSearchType);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        addPanel.setBackground(Color.WHITE);
        addPanel.add(btnAdd);

        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(addPanel, BorderLayout.EAST);

        // Table
        String[] columns = {"ID", "Mã CB", "Hãng bay", "Điểm đi", "Điểm đến",
                "Giờ bay", "Giờ đến", "Tổng ghế", "Còn trống", "Giá (VNĐ)", "Trạng thái"};
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
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); loadData(null, null, null); });
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

        JButton btnEdit = createButton("Sửa", UIConstants.PRIMARY_COLOR);
        JButton btnStatus = createButton("Đổi TT", UIConstants.WARNING_COLOR);
        JButton btnDelete = createButton("Xóa", UIConstants.DANGER_COLOR);

        panel.add(btnEdit);
        panel.add(btnStatus);
        panel.add(btnDelete);

        btnEdit.addActionListener(e -> openEditDialog());
        btnStatus.addActionListener(e -> openStatusDialog());
        btnDelete.addActionListener(e -> deleteSelected());

        return panel;
    }

    private void styleTable() {
        table.setFont(UIConstants.NORMAL_FONT);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(UIConstants.HEADER_FONT);
        table.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(210, 230, 255));

        int[] widths = {50, 90, 120, 130, 130, 130, 130, 80, 80, 110, 110};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Alternating rows
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : UIConstants.TABLE_ROW_ALT);
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (v != null && v.toString().contains("Đã hủy")) {
                    setForeground(UIConstants.DANGER_COLOR);
                } else if (v != null && v.toString().contains("Đã lên lịch")) {
                    setForeground(UIConstants.PRIMARY_COLOR);
                } else {
                    setForeground(sel ? Color.WHITE : Color.DARK_GRAY);
                }
                return c;
            }
        });
    }

    private void loadData(String departure, String arrival, String airline) {
        SwingWorker<List<Flight>, Void> worker = new SwingWorker<>() {
            protected List<Flight> doInBackground() throws Exception {
                if ((departure != null && !departure.isEmpty()) ||
                    (arrival != null && !arrival.isEmpty()) ||
                    (airline != null && !airline.isEmpty())) {
                    return flightService.search(departure, arrival, airline);
                }
                return flightService.getAll();
            }
            protected void done() {
                try {
                    List<Flight> flights = get();
                    tableModel.setRowCount(0);
                    for (Flight f : flights) {
                        tableModel.addRow(new Object[]{
                                f.getId(),
                                f.getFlightCode(),
                                f.getAirline(),
                                f.getDepartureAirport(),
                                f.getArrivalAirport(),
                                f.getDepartureTime() != null ? f.getDepartureTime().format(DTF) : "",
                                f.getArrivalTime() != null ? f.getArrivalTime().format(DTF) : "",
                                f.getTotalSeats(),
                                f.getAvailableSeats(),
                                String.format("%,.0f", f.getPrice()),
                                f.getStatus() != null ? f.getStatus().getDisplayName() : ""
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(FlightPanel.this, "Lỗi tải dữ liệu: " + e.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        int type = cmbSearchType.getSelectedIndex();
        String dep = type == 0 ? keyword : null;
        String arr = type == 1 ? keyword : null;
        String air = type == 2 ? keyword : null;
        loadData(dep, arr, air);
    }

    private Flight getSelectedFlight() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một chuyến bay!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        Long id = (Long) tableModel.getValueAt(row, 0);
        try {
            return flightService.getById(id).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private void openAddDialog() {
        FlightDialog dialog = new FlightDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) loadData(null, null, null);
    }

    private void openEditDialog() {
        Flight flight = getSelectedFlight();
        if (flight == null) return;
        FlightDialog dialog = new FlightDialog((Frame) SwingUtilities.getWindowAncestor(this), flight);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) loadData(null, null, null);
    }

    private void openStatusDialog() {
        Flight flight = getSelectedFlight();
        if (flight == null) return;

        FlightStatus[] statuses = FlightStatus.values();
        String[] names = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) names[i] = statuses[i].getDisplayName();

        String selected = (String) JOptionPane.showInputDialog(this,
                "Chọn trạng thái mới cho chuyến bay " + flight.getFlightCode() + ":",
                "Đổi trạng thái", JOptionPane.QUESTION_MESSAGE, null, names,
                flight.getStatus() != null ? flight.getStatus().getDisplayName() : names[0]);

        if (selected != null) {
            FlightStatus newStatus = null;
            for (FlightStatus s : statuses) {
                if (s.getDisplayName().equals(selected)) { newStatus = s; break; }
            }
            if (newStatus != null) {
                try {
                    flightService.updateStatus(flight.getId(), newStatus);
                    loadData(null, null, null);
                    JOptionPane.showMessageDialog(this, "Cập nhật trạng thái thành công!");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void deleteSelected() {
        Flight flight = getSelectedFlight();
        if (flight == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Xóa chuyến bay " + flight.getFlightCode() + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                flightService.delete(flight.getId());
                loadData(null, null, null);
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
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
