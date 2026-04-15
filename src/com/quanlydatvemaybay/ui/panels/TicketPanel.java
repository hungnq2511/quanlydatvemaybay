package com.quanlydatvemaybay.ui.panels;

import com.quanlydatvemaybay.entity.Flight;
import com.quanlydatvemaybay.entity.Ticket;
import com.quanlydatvemaybay.enums.TicketStatus;
import com.quanlydatvemaybay.service.FlightService;
import com.quanlydatvemaybay.service.TicketService;
import com.quanlydatvemaybay.ui.UIConstants;
import com.quanlydatvemaybay.ui.dialogs.TicketDialog;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class TicketPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbFlight;
    private JComboBox<String> cmbStatus;
    private List<Flight> flightList;
    private final TicketService ticketService = new TicketService();
    private final FlightService flightService = new FlightService();

    public TicketPanel() {
        setBackground(UIConstants.BG_COLOR);
        setLayout(new BorderLayout());
        initUI();
        loadFlights();
        loadData(null, null);
    }

    public void refresh() {
        loadData(null, null);
    }

    private void initUI() {
        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        topBar.setPreferredSize(new Dimension(0, 60));
        JLabel titleLabel = new JLabel("   Quản Lý Vé Máy Bay");
        titleLabel.setFont(UIConstants.TITLE_FONT);
        titleLabel.setForeground(UIConstants.PRIMARY_DARK);
        topBar.add(titleLabel, BorderLayout.WEST);

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(235, 235, 235)));

        JLabel lblFlight = new JLabel("Chuyến bay:");
        lblFlight.setFont(UIConstants.NORMAL_FONT);
        cmbFlight = new JComboBox<>(new String[]{"-- Tất cả --"});
        cmbFlight.setFont(UIConstants.NORMAL_FONT);
        cmbFlight.setPreferredSize(new Dimension(200, UIConstants.INPUT_HEIGHT));

        JLabel lblStatus = new JLabel("Trạng thái:");
        lblStatus.setFont(UIConstants.NORMAL_FONT);
        String[] statusOptions = {"-- Tất cả --", "Còn trống", "Đã đặt", "Đã hủy"};
        cmbStatus = new JComboBox<>(statusOptions);
        cmbStatus.setFont(UIConstants.NORMAL_FONT);
        cmbStatus.setPreferredSize(new Dimension(120, UIConstants.INPUT_HEIGHT));

        JButton btnSearch = createButton("Lọc", UIConstants.PRIMARY_COLOR);
        JButton btnRefresh = createButton("Làm mới", new Color(100, 100, 100));
        JButton btnAdd = createButton("+ Thêm vé", UIConstants.SECONDARY_COLOR);
        JButton btnBulk = createButton("Thêm hàng loạt", new Color(142, 68, 173));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(lblFlight);
        searchPanel.add(cmbFlight);
        searchPanel.add(lblStatus);
        searchPanel.add(cmbStatus);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        addPanel.setBackground(Color.WHITE);
        addPanel.add(btnAdd);
        addPanel.add(btnBulk);

        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(addPanel, BorderLayout.EAST);

        // Table
        String[] columns = {"ID", "Chuyến bay", "Số ghế", "Hạng ghế", "Giá (VNĐ)", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        styleTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.add(topBar, BorderLayout.NORTH);
        headerContainer.add(toolbar, BorderLayout.SOUTH);

        add(headerContainer, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(createBottomBar(), BorderLayout.SOUTH);

        btnSearch.addActionListener(e -> performSearch());
        btnRefresh.addActionListener(e -> { cmbFlight.setSelectedIndex(0); cmbStatus.setSelectedIndex(0); loadData(null, null); });
        btnAdd.addActionListener(e -> openAddDialog());
        btnBulk.addActionListener(e -> openBulkDialog());

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

        int[] widths = {60, 160, 100, 130, 130, 120};
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
                    if (val.equals("Còn trống")) setForeground(UIConstants.SECONDARY_COLOR);
                    else if (val.equals("Đã đặt")) setForeground(UIConstants.PRIMARY_COLOR);
                    else if (val.equals("Đã hủy")) setForeground(UIConstants.DANGER_COLOR);
                    else setForeground(Color.DARK_GRAY);
                } else setForeground(Color.WHITE);
                return c;
            }
        });
    }

    private void loadFlights() {
        try {
            flightList = flightService.getAll();
            for (Flight f : flightList) {
                cmbFlight.addItem(f.getFlightCode() + " - " + f.getDepartureAirport() + "→" + f.getArrivalAirport());
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void loadData(Long flightId, TicketStatus status) {
        SwingWorker<List<Ticket>, Void> worker = new SwingWorker<>() {
            protected List<Ticket> doInBackground() throws Exception {
                return ticketService.search(flightId, status);
            }
            protected void done() {
                try {
                    List<Ticket> tickets = get();
                    tableModel.setRowCount(0);
                    for (Ticket t : tickets) {
                        tableModel.addRow(new Object[]{
                                t.getId(),
                                t.getFlightCode(),
                                t.getSeatNumber(),
                                t.getTicketClass() != null ? t.getTicketClass().getDisplayName() : "",
                                String.format("%,.0f", t.getPrice()),
                                t.getStatus() != null ? t.getStatus().getDisplayName() : ""
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(TicketPanel.this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void performSearch() {
        Long flightId = null;
        int flightIdx = cmbFlight.getSelectedIndex();
        if (flightIdx > 0 && flightList != null && flightIdx - 1 < flightList.size()) {
            flightId = flightList.get(flightIdx - 1).getId();
        }

        TicketStatus status = null;
        int statusIdx = cmbStatus.getSelectedIndex();
        if (statusIdx == 1) status = TicketStatus.AVAILABLE;
        else if (statusIdx == 2) status = TicketStatus.BOOKED;
        else if (statusIdx == 3) status = TicketStatus.CANCELLED;

        loadData(flightId, status);
    }

    private Ticket getSelectedTicket() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một vé!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        Long id = (Long) tableModel.getValueAt(row, 0);
        try {
            return ticketService.getById(id).orElse(null);
        } catch (Exception e) { return null; }
    }

    private void openAddDialog() {
        TicketDialog dialog = new TicketDialog((Frame) SwingUtilities.getWindowAncestor(this), null, flightList);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) loadData(null, null);
    }

    private void openBulkDialog() {
        TicketDialog dialog = new TicketDialog((Frame) SwingUtilities.getWindowAncestor(this), null, flightList);
        dialog.setBulkMode(true);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) loadData(null, null);
    }

    private void openEditDialog() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) return;
        TicketDialog dialog = new TicketDialog((Frame) SwingUtilities.getWindowAncestor(this), ticket, flightList);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) loadData(null, null);
    }

    private void openStatusDialog() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) return;

        TicketStatus[] statuses = TicketStatus.values();
        String[] names = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) names[i] = statuses[i].getDisplayName();

        String selected = (String) JOptionPane.showInputDialog(this,
                "Chọn trạng thái mới cho vé " + ticket.getSeatNumber() + ":",
                "Đổi trạng thái", JOptionPane.QUESTION_MESSAGE, null, names,
                ticket.getStatus() != null ? ticket.getStatus().getDisplayName() : names[0]);

        if (selected != null) {
            TicketStatus newStatus = null;
            for (TicketStatus s : statuses) {
                if (s.getDisplayName().equals(selected)) { newStatus = s; break; }
            }
            if (newStatus != null) {
                try {
                    ticketService.updateStatus(ticket.getId(), newStatus);
                    loadData(null, null);
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void deleteSelected() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xóa vé " + ticket.getSeatNumber() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                ticketService.delete(ticket.getId());
                loadData(null, null);
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
