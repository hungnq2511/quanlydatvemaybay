package com.quanlydatvemaybay.ui.panels;

import com.quanlydatvemaybay.entity.Flight;
import com.quanlydatvemaybay.enums.FlightStatus;
import com.quanlydatvemaybay.service.FlightService;
import com.quanlydatvemaybay.ui.UIConstants;
import com.quanlydatvemaybay.ui.dialogs.BookingDialog;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.List;

/**
 * Panel "Tìm chuyến bay" cho USER.
 *  - Form đơn giản: điểm đi / điểm đến / ngày (tùy chọn) / hãng (tùy chọn).
 *  - Bảng kết quả: chỉ chuyến SCHEDULED, còn ghế, ở tương lai.
 *  - Nút "Đặt vé" trên mỗi dòng → mở BookingDialog (user vẫn phải chọn lại
 *    chuyến trong dialog, nhưng đây là cách tự nhiên với UI hiện có).
 */
public class UserSearchPanel extends JPanel {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private JComboBox<String> cmbDeparture, cmbArrival, cmbAirline;
    private JCheckBox chkUseDate;
    private JSpinner spnDate;
    private JTable resultTable;
    private DefaultTableModel resultModel;
    private JLabel lblSummary;

    private final FlightService flightService = new FlightService();
    private List<Flight> currentResults = new ArrayList<>();

    public UserSearchPanel() {
        setBackground(UIConstants.BG_COLOR);
        setLayout(new BorderLayout());
        initUI();
        loadSuggestions();
        doSearch();
    }

    public void refresh() {
        loadSuggestions();
        doSearch();
    }

    private void initUI() {
        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        topBar.setPreferredSize(new Dimension(0, 60));
        JLabel title = new JLabel("   Tìm Chuyến Bay");
        title.setFont(UIConstants.TITLE_FONT);
        title.setForeground(UIConstants.PRIMARY_DARK);
        topBar.add(title, BorderLayout.WEST);

        // Search form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(16, 24, 16, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(label("Điểm đi:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        cmbDeparture = editableCombo();
        form.add(cmbDeparture, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        form.add(label("Điểm đến:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        cmbArrival = editableCombo();
        form.add(cmbArrival, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        chkUseDate = new JCheckBox("Ngày đi:");
        chkUseDate.setFont(UIConstants.NORMAL_FONT);
        chkUseDate.setBackground(Color.WHITE);
        form.add(chkUseDate, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        spnDate = new JSpinner(new SpinnerDateModel());
        spnDate.setEditor(new JSpinner.DateEditor(spnDate, "dd/MM/yyyy"));
        spnDate.setFont(UIConstants.NORMAL_FONT);
        spnDate.setEnabled(false);
        form.add(spnDate, gbc);
        chkUseDate.addActionListener(e -> spnDate.setEnabled(chkUseDate.isSelected()));

        gbc.gridx = 2; gbc.weightx = 0;
        form.add(label("Hãng bay:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        cmbAirline = editableCombo();
        form.add(cmbAirline, gbc);

        // Row 3: buttons
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4; gbc.weightx = 1;
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(Color.WHITE);
        JButton btnReset = button("Đặt lại", new Color(120, 120, 120));
        JButton btnSearch = button("Tìm kiếm", UIConstants.PRIMARY_COLOR);
        btnReset.addActionListener(e -> reset());
        btnSearch.addActionListener(e -> doSearch());
        actions.add(btnReset);
        actions.add(btnSearch);
        form.add(actions, gbc);

        // Summary
        lblSummary = new JLabel(" ");
        lblSummary.setFont(UIConstants.SMALL_FONT);
        lblSummary.setBorder(new EmptyBorder(0, 24, 8, 24));
        lblSummary.setOpaque(true);
        lblSummary.setBackground(Color.WHITE);

        // Result table
        String[] cols = {"ID", "Mã CB", "Hãng", "Điểm đi", "Điểm đến", "Khởi hành", "Đến nơi", "Ghế trống", ""};
        resultModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 8; }
        };
        resultTable = new JTable(resultModel);
        resultTable.setFont(UIConstants.NORMAL_FONT);
        resultTable.setRowHeight(38);
        resultTable.setShowGrid(false);
        UIConstants.applyTableHeaderStyle(resultTable);
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] widths = {40, 80, 140, 180, 180, 140, 140, 100, 120};
        for (int i = 0; i < widths.length; i++) {
            resultTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        // Ẩn cột ID
        resultTable.getColumnModel().getColumn(0).setMinWidth(0);
        resultTable.getColumnModel().getColumn(0).setMaxWidth(0);
        resultTable.getColumnModel().getColumn(0).setWidth(0);

        // Cột "Đặt vé" – button
        resultTable.getColumnModel().getColumn(8).setCellRenderer(new ButtonRenderer());
        resultTable.getColumnModel().getColumn(8).setCellEditor(new ButtonEditor(new JCheckBox()));

        // Alternating rows
        resultTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? Color.WHITE : UIConstants.TABLE_ROW_ALT);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(resultTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel headerWrap = new JPanel(new BorderLayout());
        headerWrap.add(topBar, BorderLayout.NORTH);
        headerWrap.add(form, BorderLayout.CENTER);
        headerWrap.add(lblSummary, BorderLayout.SOUTH);

        add(headerWrap, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    // ── UI helpers ──────────────────────────────────────────────────────────
    private JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setFont(UIConstants.NORMAL_FONT);
        return l;
    }

    private JComboBox<String> editableCombo() {
        JComboBox<String> c = new JComboBox<>();
        c.setEditable(true);
        c.setFont(UIConstants.NORMAL_FONT);
        c.setPreferredSize(new Dimension(220, UIConstants.INPUT_HEIGHT));
        return c;
    }

    private JButton button(String text, Color color) {
        JButton b = new JButton(text);
        b.setFont(UIConstants.BUTTON_FONT);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(b.getPreferredSize().width + 30, UIConstants.BUTTON_HEIGHT));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ── Data ────────────────────────────────────────────────────────────────
    private void loadSuggestions() {
        try {
            List<Flight> all = flightService.getAll();
            Set<String> dep = new TreeSet<>(), arr = new TreeSet<>(), air = new TreeSet<>();
            for (Flight f : all) {
                if (f.getDepartureAirport() != null) dep.add(f.getDepartureAirport());
                if (f.getArrivalAirport() != null) arr.add(f.getArrivalAirport());
                if (f.getAirline() != null) air.add(f.getAirline());
            }
            cmbDeparture.removeAllItems(); cmbDeparture.addItem("");
            for (String s : dep) cmbDeparture.addItem(s);
            cmbArrival.removeAllItems(); cmbArrival.addItem("");
            for (String s : arr) cmbArrival.addItem(s);
            cmbAirline.removeAllItems(); cmbAirline.addItem("");
            for (String s : air) cmbAirline.addItem(s);
        } catch (Exception ignored) {}
    }

    private void reset() {
        cmbDeparture.setSelectedIndex(0);
        cmbArrival.setSelectedIndex(0);
        cmbAirline.setSelectedIndex(0);
        chkUseDate.setSelected(false);
        spnDate.setEnabled(false);
        spnDate.setValue(new Date());
        doSearch();
    }

    private void doSearch() {
        String dep = ((String) cmbDeparture.getEditor().getItem()).trim();
        String arr = ((String) cmbArrival.getEditor().getItem()).trim();
        String airline = ((String) cmbAirline.getEditor().getItem()).trim();
        boolean useDate = chkUseDate.isSelected();
        Date d = useDate ? (Date) spnDate.getValue() : null;
        LocalDate filterDate = d != null
                ? d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;

        SwingWorker<List<Flight>, Void> worker = new SwingWorker<>() {
            protected List<Flight> doInBackground() throws Exception {
                List<Flight> list = flightService.search(
                        dep.isEmpty() ? null : dep,
                        arr.isEmpty() ? null : arr,
                        airline.isEmpty() ? null : airline);
                LocalDateTime now = LocalDateTime.now();
                return list.stream()
                        .filter(f -> f.getStatus() == FlightStatus.SCHEDULED)
                        .filter(f -> f.getAvailableSeats() > 0)
                        .filter(f -> f.getDepartureTime() != null && f.getDepartureTime().isAfter(now))
                        .filter(f -> filterDate == null
                                || f.getDepartureTime().toLocalDate().equals(filterDate))
                        .sorted(Comparator.comparing(Flight::getDepartureTime))
                        .collect(java.util.stream.Collectors.toList());
            }
            protected void done() {
                try {
                    currentResults = get();
                    fillTable(currentResults, filterDate);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(UserSearchPanel.this,
                            "Lỗi tìm kiếm: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void fillTable(List<Flight> flights, LocalDate filterDate) {
        resultModel.setRowCount(0);
        for (Flight f : flights) {
            resultModel.addRow(new Object[]{
                    f.getId(), f.getFlightCode(), f.getAirline(),
                    f.getDepartureAirport(), f.getArrivalAirport(),
                    f.getDepartureTime() != null ? f.getDepartureTime().format(DTF) : "",
                    f.getArrivalTime() != null ? f.getArrivalTime().format(DTF) : "",
                    f.getAvailableSeats(),
                    "Đặt vé"
            });
        }

        if (flights.isEmpty()) {
            lblSummary.setText("  Không tìm thấy chuyến bay nào phù hợp.");
            lblSummary.setForeground(UIConstants.DANGER_COLOR);
        } else {
            String dateStr = filterDate != null
                    ? " vào " + filterDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : "";
            lblSummary.setText(String.format("  Tìm thấy %d chuyến bay%s.", flights.size(), dateStr));
            lblSummary.setForeground(new Color(39, 174, 96));
        }
    }

    private void openBookingDialog() {
        BookingDialog dlg = new BookingDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) doSearch();
    }

    // ── Inline button cell ──────────────────────────────────────────────────
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        ButtonRenderer() {
            setOpaque(true);
            setFont(UIConstants.BUTTON_FONT);
            setBackground(UIConstants.SECONDARY_COLOR);
            setForeground(Color.WHITE);
            setBorderPainted(false);
            setFocusPainted(false);
        }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            setText(v == null ? "" : v.toString());
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private final JButton btn;
        ButtonEditor(JCheckBox cb) {
            super(cb);
            btn = new JButton();
            btn.setOpaque(true);
            btn.setFont(UIConstants.BUTTON_FONT);
            btn.setBackground(UIConstants.SECONDARY_COLOR);
            btn.setForeground(Color.WHITE);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.addActionListener(e -> {
                fireEditingStopped();
                openBookingDialog();
            });
        }
        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int row, int col) {
            btn.setText(v == null ? "Đặt vé" : v.toString());
            return btn;
        }
        @Override public Object getCellEditorValue() { return btn.getText(); }
    }
}
