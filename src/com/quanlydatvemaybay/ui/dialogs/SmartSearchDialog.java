package com.quanlydatvemaybay.ui.dialogs;

import com.quanlydatvemaybay.entity.Flight;
import com.quanlydatvemaybay.service.FlightService;
import com.quanlydatvemaybay.ui.UIConstants;

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
 * Tìm chuyến bay thông minh:
 *  - Theo điểm đi / điểm đến / ngày khởi hành mong muốn.
 *  - Tự động đề xuất chuyến bay trong khoảng ±3 ngày nếu không có chuyến đúng ngày.
 *  - Double-click 1 chuyến → mở BookingDialog với chuyến đó được chọn sẵn.
 */
public class SmartSearchDialog extends JDialog {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DTF_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JComboBox<String> cmbDeparture;
    private JComboBox<String> cmbArrival;
    private JSpinner spnDate;
    private JSpinner spnRangeBefore, spnRangeAfter;
    private JTable resultTable;
    private DefaultTableModel resultModel;
    private JLabel lblSummary;

    private final FlightService flightService = new FlightService();
    private List<Flight> currentResults = new ArrayList<>();
    private final Frame owner;

    public SmartSearchDialog(Frame owner) {
        super(owner, "Tìm chuyến bay thông minh", true);
        this.owner = owner;
        setSize(900, 600);
        setLocationRelativeTo(owner);
        initUI();
        loadAirportSuggestions();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIConstants.BG_COLOR);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.PRIMARY_COLOR);
        header.setPreferredSize(new Dimension(0, 55));
        header.setBorder(new EmptyBorder(0, 20, 0, 20));
        JLabel title = new JLabel("  Tìm chuyến bay thông minh");
        title.setFont(UIConstants.TITLE_FONT);
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(14, 20, 14, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1: điểm đi / điểm đến
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

        // Row 2: ngày & range
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(label("Ngày khởi hành:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        spnDate = new JSpinner(new SpinnerDateModel());
        spnDate.setEditor(new JSpinner.DateEditor(spnDate, "dd/MM/yyyy"));
        spnDate.setFont(UIConstants.NORMAL_FONT);
        form.add(spnDate, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        form.add(label("Linh hoạt ±:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        JPanel rangePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        rangePanel.setBackground(Color.WHITE);
        spnRangeBefore = new JSpinner(new SpinnerNumberModel(3, 0, 30, 1));
        spnRangeAfter = new JSpinner(new SpinnerNumberModel(3, 0, 30, 1));
        spnRangeBefore.setPreferredSize(new Dimension(60, UIConstants.INPUT_HEIGHT));
        spnRangeAfter.setPreferredSize(new Dimension(60, UIConstants.INPUT_HEIGHT));
        rangePanel.add(new JLabel("Trước:"));
        rangePanel.add(spnRangeBefore);
        rangePanel.add(new JLabel("ngày  Sau:"));
        rangePanel.add(spnRangeAfter);
        rangePanel.add(new JLabel("ngày"));
        form.add(rangePanel, gbc);

        // Row 3: nút tìm + summary
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1;
        lblSummary = new JLabel(" ");
        lblSummary.setFont(UIConstants.SMALL_FONT);
        lblSummary.setForeground(new Color(80, 80, 80));
        form.add(lblSummary, gbc);

        gbc.gridx = 2; gbc.gridwidth = 2; gbc.weightx = 0;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(Color.WHITE);
        JButton btnReset = button("Đặt lại", new Color(120, 120, 120));
        JButton btnSearch = button("Tìm kiếm", UIConstants.PRIMARY_COLOR);
        btnReset.addActionListener(e -> reset());
        btnSearch.addActionListener(e -> doSearch());
        btnPanel.add(btnReset);
        btnPanel.add(btnSearch);
        form.add(btnPanel, gbc);

        // Result table
        String[] cols = {"ID", "Mã CB", "Hãng", "Điểm đi", "Điểm đến", "Khởi hành", "Đến nơi", "Ghế trống", "Giá (VNĐ)"};
        resultModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        resultTable = new JTable(resultModel);
        resultTable.setFont(UIConstants.NORMAL_FONT);
        resultTable.setRowHeight(32);
        resultTable.setShowGrid(false);
        UIConstants.applyTableHeaderStyle(resultTable);
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] widths = {40, 80, 130, 150, 150, 130, 130, 80, 110};
        for (int i = 0; i < widths.length; i++) {
            resultTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        // Ẩn cột ID
        resultTable.getColumnModel().getColumn(0).setMinWidth(0);
        resultTable.getColumnModel().getColumn(0).setMaxWidth(0);
        resultTable.getColumnModel().getColumn(0).setWidth(0);

        resultTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) bookSelected();
            }
        });

        JScrollPane sp = new JScrollPane(resultTable);
        sp.setBorder(BorderFactory.createEmptyBorder());

        // Bottom buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));
        JButton btnBook = button("Đặt vé chuyến đã chọn", UIConstants.SECONDARY_COLOR);
        JButton btnClose = button("Đóng", new Color(120, 120, 120));
        btnBook.addActionListener(e -> bookSelected());
        btnClose.addActionListener(e -> dispose());
        bottom.add(btnBook);
        bottom.add(btnClose);

        main.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.add(form, BorderLayout.NORTH);
        center.add(sp, BorderLayout.CENTER);
        main.add(center, BorderLayout.CENTER);
        main.add(bottom, BorderLayout.SOUTH);
        setContentPane(main);
    }

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

    /** Gợi ý các điểm đi/đến từ DB các chuyến bay đang lên lịch. */
    private void loadAirportSuggestions() {
        try {
            List<Flight> all = flightService.getAll();
            Set<String> dep = new TreeSet<>();
            Set<String> arr = new TreeSet<>();
            for (Flight f : all) {
                if (f.getDepartureAirport() != null) dep.add(f.getDepartureAirport());
                if (f.getArrivalAirport() != null) arr.add(f.getArrivalAirport());
            }
            cmbDeparture.addItem("");
            for (String s : dep) cmbDeparture.addItem(s);
            cmbArrival.addItem("");
            for (String s : arr) cmbArrival.addItem(s);
        } catch (Exception ignored) {}
    }

    private void reset() {
        cmbDeparture.setSelectedIndex(0);
        cmbArrival.setSelectedIndex(0);
        spnDate.setValue(new Date());
        spnRangeBefore.setValue(3);
        spnRangeAfter.setValue(3);
        resultModel.setRowCount(0);
        currentResults.clear();
        lblSummary.setText(" ");
    }

    private void doSearch() {
        String dep = ((String) cmbDeparture.getEditor().getItem()).trim();
        String arr = ((String) cmbArrival.getEditor().getItem()).trim();
        Date d = (Date) spnDate.getValue();
        int rb = (int) spnRangeBefore.getValue();
        int ra = (int) spnRangeAfter.getValue();

        LocalDateTime target = LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault())
                .toLocalDate().atStartOfDay();

        SwingWorker<List<Flight>, Void> worker = new SwingWorker<>() {
            protected List<Flight> doInBackground() throws Exception {
                return flightService.searchSmart(
                        dep.isEmpty() ? null : dep,
                        arr.isEmpty() ? null : arr,
                        target, rb, ra);
            }
            protected void done() {
                try {
                    currentResults = get();
                    fillTable(currentResults, target);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SmartSearchDialog.this,
                            "Lỗi tìm kiếm: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void fillTable(List<Flight> flights, LocalDateTime target) {
        resultModel.setRowCount(0);
        LocalDate targetDate = target.toLocalDate();

        long exactCount = flights.stream()
                .filter(f -> f.getDepartureTime() != null
                        && f.getDepartureTime().toLocalDate().equals(targetDate))
                .count();

        for (Flight f : flights) {
            boolean isExact = f.getDepartureTime() != null
                    && f.getDepartureTime().toLocalDate().equals(targetDate);
            String code = f.getFlightCode() + (isExact ? "" : "  (gợi ý)");
            resultModel.addRow(new Object[]{
                    f.getId(), code, f.getAirline(),
                    f.getDepartureAirport(), f.getArrivalAirport(),
                    f.getDepartureTime() != null ? f.getDepartureTime().format(DTF) : "",
                    f.getArrivalTime() != null ? f.getArrivalTime().format(DTF) : "",
                    f.getAvailableSeats(),
                    f.getPrice() != null ? String.format("%,.0f", f.getPrice()) : ""
            });
        }

        if (flights.isEmpty()) {
            lblSummary.setText("Không tìm thấy chuyến bay nào phù hợp. Hãy mở rộng khoảng ngày hoặc đổi tuyến.");
            lblSummary.setForeground(UIConstants.DANGER_COLOR);
        } else if (exactCount == 0) {
            lblSummary.setText(String.format(
                    "Không có chuyến vào %s. Hiển thị %d chuyến gợi ý trong khoảng ±%d/%d ngày.",
                    targetDate.format(DTF_DATE), flights.size(),
                    (int) spnRangeBefore.getValue(), (int) spnRangeAfter.getValue()));
            lblSummary.setForeground(UIConstants.WARNING_COLOR);
        } else {
            lblSummary.setText(String.format(
                    "Tìm thấy %d chuyến (%d đúng ngày %s).",
                    flights.size(), exactCount, targetDate.format(DTF_DATE)));
            lblSummary.setForeground(new Color(39, 174, 96));
        }
    }

    private void bookSelected() {
        int row = resultTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một chuyến bay!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Long flightId = (Long) resultModel.getValueAt(row, 0);
        BookingDialog dialog = new BookingDialog(owner, null, flightId);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            // Sau khi đặt thành công thì refresh kết quả để cập nhật số ghế còn
            doSearch();
        }
    }
}
