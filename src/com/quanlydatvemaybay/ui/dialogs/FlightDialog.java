package com.quanlydatvemaybay.ui.dialogs;

import com.quanlydatvemaybay.entity.Flight;
import com.quanlydatvemaybay.enums.FlightStatus;
import com.quanlydatvemaybay.service.FlightService;
import com.quanlydatvemaybay.ui.UIConstants;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class FlightDialog extends JDialog {

    private boolean confirmed = false;
    private final Flight flight;
    private final FlightService flightService = new FlightService();
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private JTextField txtCode, txtAirline, txtDeparture, txtArrival;
    private JTextField txtDepTime, txtArrTime, txtSeats, txtPrice;
    private JComboBox<String> cmbStatus;

    public FlightDialog(Frame parent, Flight flight) {
        super(parent, flight == null ? "Thêm chuyến bay mới" : "Sửa chuyến bay", true);
        this.flight = flight;
        setSize(560, 600);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
        if (flight != null) fillData();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIConstants.BG_COLOR);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.PRIMARY_COLOR);
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(new EmptyBorder(0, 25, 0, 25));
        JLabel titleLbl = new JLabel(flight == null ? "  Thêm chuyến bay mới" : "  Cập nhật chuyến bay");
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

        int row = 0;

        // Row 1: Mã chuyến bay + Hãng bay
        row = addRowTwoFields(form, gbc, row,
                "Mã chuyến bay *", "Hãng bay *",
                txtCode = createField("VN001"), txtAirline = createField("Vietnam Airlines"));

        // Row 2: Điểm đi + Điểm đến
        row = addRowTwoFields(form, gbc, row,
                "Điểm đi *", "Điểm đến *",
                txtDeparture = createField("Hà Nội (HAN)"), txtArrival = createField("TP.HCM (SGN)"));

        // Row 3: Giờ bay + Giờ đến
        row = addRowTwoFields(form, gbc, row,
                "Giờ bay * (dd/MM/yyyy HH:mm)", "Giờ đến * (dd/MM/yyyy HH:mm)",
                txtDepTime = createField("20/04/2026 07:00"), txtArrTime = createField("20/04/2026 09:00"));

        // Row 4: Tổng ghế + Giá vé
        row = addRowTwoFields(form, gbc, row,
                "Tổng số ghế *", "Giá vé (VNĐ) *",
                txtSeats = createField("150"), txtPrice = createField("1200000"));

        // Row 5: Trạng thái (full width)
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel lblStatus = new JLabel("Trạng thái");
        lblStatus.setFont(UIConstants.NORMAL_FONT);
        lblStatus.setForeground(new Color(60, 60, 60));
        form.add(lblStatus, gbc);

        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1;
        FlightStatus[] allowedStatuses = flight == null
                ? new FlightStatus[]{FlightStatus.SCHEDULED, FlightStatus.DELAYED}
                : FlightStatus.values();
        String[] statusNames = new String[allowedStatuses.length];
        for (int i = 0; i < allowedStatuses.length; i++) statusNames[i] = allowedStatuses[i].getDisplayName();
        cmbStatus = new JComboBox<>(statusNames);
        cmbStatus.setFont(UIConstants.NORMAL_FONT);
        cmbStatus.setPreferredSize(new Dimension(0, UIConstants.INPUT_HEIGHT));
        form.add(cmbStatus, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JButton btnCancel = makeBtn("Hủy", new Color(150, 150, 150));
        JButton btnSave   = makeBtn(flight == null ? "Thêm mới" : "Lưu thay đổi", UIConstants.PRIMARY_COLOR);
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

    /** Thêm 1 hàng gồm 2 label + 2 field song song */
    private int addRowTwoFields(JPanel form, GridBagConstraints gbc, int row,
                                String lbl1, String lbl2, JTextField f1, JTextField f2) {
        // Labels
        gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        gbc.gridx = 0;
        form.add(makeLabel(lbl1), gbc);
        gbc.gridx = 2;
        form.add(makeLabel(lbl2), gbc);

        // Fields
        row++;
        gbc.gridy = row; gbc.weightx = 1;
        gbc.gridx = 0; gbc.gridwidth = 2;
        form.add(f1, gbc);
        gbc.gridx = 2;
        form.add(f2, gbc);

        return row + 1;
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.SMALL_FONT);
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    private JTextField createField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(UIConstants.NORMAL_FONT);
        tf.setPreferredSize(new Dimension(0, UIConstants.INPUT_HEIGHT));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        tf.setToolTipText(placeholder);
        return tf;
    }

    private void fillData() {
        txtCode.setText(flight.getFlightCode());
        txtAirline.setText(flight.getAirline());
        txtDeparture.setText(flight.getDepartureAirport());
        txtArrival.setText(flight.getArrivalAirport());
        if (flight.getDepartureTime() != null) txtDepTime.setText(flight.getDepartureTime().format(DTF));
        if (flight.getArrivalTime() != null)   txtArrTime.setText(flight.getArrivalTime().format(DTF));
        txtSeats.setText(String.valueOf(flight.getTotalSeats()));
        if (flight.getPrice() != null) txtPrice.setText(flight.getPrice().toPlainString());
        if (flight.getStatus() != null) cmbStatus.setSelectedItem(flight.getStatus().getDisplayName());
    }

    private void save() {
        String code      = txtCode.getText().trim();
        String airline   = txtAirline.getText().trim();
        String dep       = txtDeparture.getText().trim();
        String arr       = txtArrival.getText().trim();
        String depTimeStr = txtDepTime.getText().trim();
        String arrTimeStr = txtArrTime.getText().trim();
        String seatsStr  = txtSeats.getText().trim();
        String priceStr  = txtPrice.getText().trim();

        if (code.isEmpty() || airline.isEmpty() || dep.isEmpty() || arr.isEmpty()
                || depTimeStr.isEmpty() || arrTimeStr.isEmpty()
                || seatsStr.isEmpty() || priceStr.isEmpty()) {
            showError("Vui lòng điền đầy đủ thông tin bắt buộc (*)");
            return;
        }

        LocalDateTime depTime, arrTime;
        try {
            depTime = LocalDateTime.parse(depTimeStr, DTF);
            arrTime = LocalDateTime.parse(arrTimeStr, DTF);
        } catch (DateTimeParseException e) {
            showError("Định dạng thời gian không hợp lệ!\nVui lòng dùng: dd/MM/yyyy HH:mm\nVí dụ: 20/04/2026 07:00");
            return;
        }

        if (!depTime.isBefore(arrTime)) {
            showError("Giờ bay phải trước giờ đến!");
            return;
        }

        int seats;
        BigDecimal price;
        try {
            seats = Integer.parseInt(seatsStr);
            if (seats <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("Số ghế phải là số nguyên dương!");
            return;
        }
        try {
            price = new BigDecimal(priceStr.replace(",", "").replace(".", ""));
            if (price.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("Giá vé phải là số dương!");
            return;
        }

        FlightStatus status = FlightStatus.SCHEDULED;
        String sel = (String) cmbStatus.getSelectedItem();
        for (FlightStatus s : FlightStatus.values()) {
            if (s.getDisplayName().equals(sel)) { status = s; break; }
        }

        Flight f = flight != null ? flight : new Flight();
        f.setFlightCode(code);
        f.setAirline(airline);
        f.setDepartureAirport(dep);
        f.setArrivalAirport(arr);
        f.setDepartureTime(depTime);
        f.setArrivalTime(arrTime);
        f.setTotalSeats(seats);
        if (f.getAvailableSeats() == null) f.setAvailableSeats(seats);
        f.setPrice(price);
        f.setStatus(status);

        try {
            if (flight == null) {
                flightService.create(f);
                JOptionPane.showMessageDialog(this, "Thêm chuyến bay thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                flightService.update(f.getId(), f);
                JOptionPane.showMessageDialog(this, "Cập nhật chuyến bay thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
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
