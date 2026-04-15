package com.quanlydatvemaybay.ui.panels;

import com.quanlydatvemaybay.entity.Booking;
import com.quanlydatvemaybay.entity.Flight;
import com.quanlydatvemaybay.enums.BookingStatus;
import com.quanlydatvemaybay.service.AuthService;
import com.quanlydatvemaybay.service.BookingService;
import com.quanlydatvemaybay.service.FlightService;
import com.quanlydatvemaybay.ui.UIConstants;
import com.quanlydatvemaybay.ui.dialogs.BookingDialog;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BookingPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbFlight;
    private JComboBox<String> cmbStatus;
    private JTextField txtPassenger;
    private List<Flight> flightList;
    private final BookingService bookingService = new BookingService();
    private final FlightService flightService = new FlightService();
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public BookingPanel() {
        setBackground(UIConstants.BG_COLOR);
        setLayout(new BorderLayout());
        initUI();
        loadFlights();
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
        JLabel titleLabel = new JLabel("   Quản Lý Đặt Vé");
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
        cmbFlight.setPreferredSize(new Dimension(180, UIConstants.INPUT_HEIGHT));

        JLabel lblStatus = new JLabel("Trạng thái:");
        lblStatus.setFont(UIConstants.NORMAL_FONT);
        cmbStatus = new JComboBox<>(new String[]{"-- Tất cả --", "Chờ xác nhận", "Đã xác nhận", "Đã hủy", "Hoàn thành"});
        cmbStatus.setFont(UIConstants.NORMAL_FONT);
        cmbStatus.setPreferredSize(new Dimension(130, UIConstants.INPUT_HEIGHT));

        JLabel lblPassenger = new JLabel("Hành khách:");
        lblPassenger.setFont(UIConstants.NORMAL_FONT);
        txtPassenger = new JTextField();
        txtPassenger.setFont(UIConstants.NORMAL_FONT);
        txtPassenger.setPreferredSize(new Dimension(160, UIConstants.INPUT_HEIGHT));
        txtPassenger.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        JButton btnSearch = createButton("Lọc", UIConstants.PRIMARY_COLOR);
        JButton btnRefresh = createButton("Làm mới", new Color(100, 100, 100));
        JButton btnAdd = createButton("+ Đặt vé", UIConstants.SECONDARY_COLOR);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(lblFlight); searchPanel.add(cmbFlight);
        searchPanel.add(lblStatus); searchPanel.add(cmbStatus);
        searchPanel.add(lblPassenger); searchPanel.add(txtPassenger);
        searchPanel.add(btnSearch); searchPanel.add(btnRefresh);

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        addPanel.setBackground(Color.WHITE);
        addPanel.add(btnAdd);

        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(addPanel, BorderLayout.EAST);

        // Table
        String[] columns = {"ID", "Mã đặt vé", "Mã CB", "Điểm đi", "Điểm đến", "Giờ bay",
                "Số ghế", "Hành khách", "SĐT", "Ngày đặt", "Giá (VNĐ)", "Trạng thái"};
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
        btnRefresh.addActionListener(e -> {
            cmbFlight.setSelectedIndex(0);
            cmbStatus.setSelectedIndex(0);
            txtPassenger.setText("");
            loadData(null, null, null);
        });
        btnAdd.addActionListener(e -> openAddDialog());

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                boolean admin = AuthService.getCurrentUser() != null && AuthService.getCurrentUser().isAdmin();
                if (e.getClickCount() == 2 && admin) openEditDialog();
            }
        });
    }

    private JPanel createBottomBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(235, 235, 235)));

        boolean isAdmin = AuthService.getCurrentUser() != null && AuthService.getCurrentUser().isAdmin();

        if (isAdmin) {
            JButton btnEdit = createButton("Sửa", UIConstants.PRIMARY_COLOR);
            JButton btnStatus = createButton("Đổi TT", UIConstants.WARNING_COLOR);
            btnEdit.addActionListener(e -> openEditDialog());
            btnStatus.addActionListener(e -> openStatusDialog());
            panel.add(btnEdit);
            panel.add(btnStatus);
        }

        JButton btnCancel = createButton("Hủy vé", UIConstants.DANGER_COLOR);
        btnCancel.addActionListener(e -> cancelSelected());
        panel.add(btnCancel);

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

        int[] widths = {50, 110, 80, 110, 110, 120, 80, 130, 100, 120, 100, 120};
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? Color.WHITE : UIConstants.TABLE_ROW_ALT);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                if (!sel) {
                    String val = v != null ? v.toString() : "";
                    if (val.equals("Đã xác nhận")) setForeground(UIConstants.SECONDARY_COLOR);
                    else if (val.equals("Đã hủy")) setForeground(UIConstants.DANGER_COLOR);
                    else if (val.equals("Chờ xác nhận")) setForeground(UIConstants.WARNING_COLOR);
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
        } catch (Exception e) { /* ignore */ }
    }

    private void loadData(Long flightId, BookingStatus status, String passengerName) {
        SwingWorker<List<Booking>, Void> worker = new SwingWorker<>() {
            protected List<Booking> doInBackground() throws Exception {
                return bookingService.search(flightId, status, passengerName);
            }
            protected void done() {
                try {
                    List<Booking> bookings = get();
                    tableModel.setRowCount(0);
                    for (Booking b : bookings) {
                        tableModel.addRow(new Object[]{
                                b.getId(),
                                b.getBookingCode(),
                                b.getFlightCode(),
                                b.getDepartureAirport(),
                                b.getArrivalAirport(),
                                b.getDepartureTime() != null ? b.getDepartureTime().format(DTF) : "",
                                b.getSeatNumber(),
                                b.getPassengerName(),
                                b.getPassengerPhone(),
                                b.getBookingDate() != null ? b.getBookingDate().format(DTF) : "",
                                b.getTicketPrice() != null ? String.format("%,.0f", b.getTicketPrice()) : "",
                                b.getStatus() != null ? b.getStatus().getDisplayName() : ""
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BookingPanel.this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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

        BookingStatus status = null;
        int statusIdx = cmbStatus.getSelectedIndex();
        if (statusIdx == 1) status = BookingStatus.PENDING;
        else if (statusIdx == 2) status = BookingStatus.CONFIRMED;
        else if (statusIdx == 3) status = BookingStatus.CANCELLED;
        else if (statusIdx == 4) status = BookingStatus.COMPLETED;

        String passenger = txtPassenger.getText().trim();
        loadData(flightId, status, passenger.isEmpty() ? null : passenger);
    }

    private Booking getSelectedBooking() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đặt vé!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        Long id = (Long) tableModel.getValueAt(row, 0);
        try {
            return bookingService.getById(id).orElse(null);
        } catch (Exception e) { return null; }
    }

    private void openAddDialog() {
        BookingDialog dialog = new BookingDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) loadData(null, null, null);
    }

    private void openEditDialog() {
        Booking booking = getSelectedBooking();
        if (booking == null) return;
        BookingDialog dialog = new BookingDialog((Frame) SwingUtilities.getWindowAncestor(this), booking);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) loadData(null, null, null);
    }

    private void openStatusDialog() {
        Booking booking = getSelectedBooking();
        if (booking == null) return;

        BookingStatus[] statuses = BookingStatus.values();
        String[] names = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) names[i] = statuses[i].getDisplayName();

        String selected = (String) JOptionPane.showInputDialog(this,
                "Chọn trạng thái mới cho đặt vé " + booking.getBookingCode() + ":",
                "Đổi trạng thái", JOptionPane.QUESTION_MESSAGE, null, names,
                booking.getStatus() != null ? booking.getStatus().getDisplayName() : names[0]);

        if (selected != null) {
            BookingStatus newStatus = null;
            for (BookingStatus s : statuses) {
                if (s.getDisplayName().equals(selected)) { newStatus = s; break; }
            }
            if (newStatus != null) {
                try {
                    bookingService.updateStatus(booking.getId(), newStatus);
                    loadData(null, null, null);
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void cancelSelected() {
        Booking booking = getSelectedBooking();
        if (booking == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Hủy đặt vé " + booking.getBookingCode() + " của " + booking.getPassengerName() + "?",
                "Xác nhận hủy vé", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                bookingService.cancel(booking.getId());
                loadData(null, null, null);
                JOptionPane.showMessageDialog(this, "Hủy vé thành công!");
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
