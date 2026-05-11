package com.quanlydatvemaybay.ui.dialogs;

import com.quanlydatvemaybay.dao.AirlineDAO;
import com.quanlydatvemaybay.dao.AirportDAO;
import com.quanlydatvemaybay.dao.FlightDAO;
import com.quanlydatvemaybay.entity.Airline;
import com.quanlydatvemaybay.entity.Airport;
import com.quanlydatvemaybay.entity.Flight;
import com.quanlydatvemaybay.enums.FlightStatus;
import com.quanlydatvemaybay.service.FlightService;
import com.quanlydatvemaybay.ui.UIConstants;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class FlightDialog extends JDialog {

    private boolean confirmed = false;
    private final Flight flight;
    private final FlightService flightService = new FlightService();
    private final FlightDAO flightDAO = new FlightDAO();
    private final AirlineDAO airlineDAO = new AirlineDAO();
    private final AirportDAO airportDAO = new AirportDAO();

    private JTextField txtCode;
    private JComboBox<Airline> cmbAirline;
    private JComboBox<Airport> cmbDeparture;
    private JComboBox<Airport> cmbArrival;
    private JSpinner spnDepTime;
    private JSpinner spnArrTime;
    private JTextField txtSeats;
    private JComboBox<String> cmbStatus;

    public FlightDialog(Frame parent, Flight flight) {
        super(parent, flight == null ? "Thêm chuyến bay mới" : "Sửa chuyến bay", true);
        this.flight = flight;
        setSize(600, 580);
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
        gbc.insets = new Insets(4, 4, 4, 4);

        List<Airline> airlines = loadAirlines();
        List<Airport> airports = loadAirports();

        int row = 0;

        // Row: Hãng bay + Mã chuyến bay
        gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 0.5;
        gbc.gridx = 0; form.add(makeLabel("Hãng bay *"), gbc);
        gbc.gridx = 2; form.add(makeLabel("Mã chuyến bay (tự sinh)"), gbc);
        row++;

        cmbAirline = new JComboBox<>(airlines.toArray(new Airline[0]));
        styleCombo(cmbAirline);
        txtCode = createField("");
        txtCode.setEditable(false);
        txtCode.setBackground(new Color(245, 245, 245));

        gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 0.5;
        gbc.gridx = 0; form.add(cmbAirline, gbc);
        gbc.gridx = 2; form.add(txtCode, gbc);
        row++;

        // Row: Điểm đi + Điểm đến
        gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 0.5;
        gbc.gridx = 0; form.add(makeLabel("Điểm đi *"), gbc);
        gbc.gridx = 2; form.add(makeLabel("Điểm đến *"), gbc);
        row++;

        cmbDeparture = new JComboBox<>(airports.toArray(new Airport[0]));
        styleCombo(cmbDeparture);
        cmbArrival = new JComboBox<>(airports.toArray(new Airport[0]));
        styleCombo(cmbArrival);

        gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 0.5;
        gbc.gridx = 0; form.add(cmbDeparture, gbc);
        gbc.gridx = 2; form.add(cmbArrival, gbc);
        row++;

        // Row: Giờ bay + Giờ đến
        gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 0.5;
        gbc.gridx = 0; form.add(makeLabel("Giờ bay *"), gbc);
        gbc.gridx = 2; form.add(makeLabel("Giờ đến *"), gbc);
        row++;

        spnDepTime = createDateSpinner();
        spnArrTime = createDateSpinner();

        gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 0.5;
        gbc.gridx = 0; form.add(spnDepTime, gbc);
        gbc.gridx = 2; form.add(spnArrTime, gbc);
        row++;

        // Row: Tổng ghế
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 4; gbc.weightx = 1;
        form.add(makeLabel("Tổng số ghế *"), gbc);
        row++;

        txtSeats = createField("150");
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 4;
        form.add(txtSeats, gbc);
        row++;

        // Trạng thái chỉ hiện khi sửa
        if (flight != null) {
            gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 4; gbc.weightx = 1;
            form.add(makeLabel("Trạng thái"), gbc);
            row++;

            String[] statusNames = new String[FlightStatus.values().length];
            for (int i = 0; i < FlightStatus.values().length; i++) statusNames[i] = FlightStatus.values()[i].getDisplayName();
            cmbStatus = new JComboBox<>(statusNames);
            cmbStatus.setFont(UIConstants.NORMAL_FONT);
            cmbStatus.setPreferredSize(new Dimension(0, UIConstants.INPUT_HEIGHT));
            gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 4;
            form.add(cmbStatus, gbc);
        }

        // Auto-generate code when airline changes (only for new flight)
        if (flight == null) {
            generateCode();
            cmbAirline.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) generateCode();
            });
        }

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

    private void generateCode() {
        Airline selected = (Airline) cmbAirline.getSelectedItem();
        if (selected == null) return;
        try {
            txtCode.setText(flightDAO.generateNextCode(selected.getCode()));
        } catch (SQLException e) {
            txtCode.setText(selected.getCode() + "001");
        }
    }

    private List<Airline> loadAirlines() {
        try { return airlineDAO.findAll(); } catch (SQLException e) { return new ArrayList<>(); }
    }

    private List<Airport> loadAirports() {
        try { return airportDAO.findAll(); } catch (SQLException e) { return new ArrayList<>(); }
    }

    private JSpinner createDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd/MM/yyyy HH:mm");
        spinner.setEditor(editor);
        spinner.setFont(UIConstants.NORMAL_FONT);
        spinner.setPreferredSize(new Dimension(0, UIConstants.INPUT_HEIGHT));
        JTextComponent tf = ((JSpinner.DefaultEditor) editor).getTextField();
        tf.setFont(UIConstants.NORMAL_FONT);
        return spinner;
    }

    private <T> void styleCombo(JComboBox<T> combo) {
        combo.setFont(UIConstants.NORMAL_FONT);
        combo.setPreferredSize(new Dimension(0, UIConstants.INPUT_HEIGHT));
    }

    private void fillData() {
        // Airline: match by name
        for (int i = 0; i < cmbAirline.getItemCount(); i++) {
            if (cmbAirline.getItemAt(i).getName().equalsIgnoreCase(flight.getAirline())) {
                cmbAirline.setSelectedIndex(i);
                break;
            }
        }

        // Code editable when editing
        txtCode.setText(flight.getFlightCode());
        txtCode.setEditable(true);
        txtCode.setBackground(Color.WHITE);

        // Airports: stored as "City (CODE)"
        selectAirport(cmbDeparture, flight.getDepartureAirport());
        selectAirport(cmbArrival, flight.getArrivalAirport());

        // Times
        if (flight.getDepartureTime() != null) {
            spnDepTime.setValue(Date.from(flight.getDepartureTime().atZone(ZoneId.systemDefault()).toInstant()));
        }
        if (flight.getArrivalTime() != null) {
            spnArrTime.setValue(Date.from(flight.getArrivalTime().atZone(ZoneId.systemDefault()).toInstant()));
        }

        txtSeats.setText(String.valueOf(flight.getTotalSeats()));
        if (flight.getStatus() != null) cmbStatus.setSelectedItem(flight.getStatus().getDisplayName());
    }

    private void selectAirport(JComboBox<Airport> cmb, String stored) {
        if (stored == null) return;
        for (int i = 0; i < cmb.getItemCount(); i++) {
            Airport ap = cmb.getItemAt(i);
            String fmt = ap.getCity() + " (" + ap.getCode() + ")";
            if (fmt.equalsIgnoreCase(stored) || ap.getCode().equalsIgnoreCase(stored)) {
                cmb.setSelectedIndex(i);
                return;
            }
        }
    }

    private void save() {
        String code   = txtCode.getText().trim();
        Airline airline = (Airline) cmbAirline.getSelectedItem();
        Airport dep   = (Airport) cmbDeparture.getSelectedItem();
        Airport arr   = (Airport) cmbArrival.getSelectedItem();
        String seatsStr = txtSeats.getText().trim();

        if (code.isEmpty() || airline == null || dep == null || arr == null || seatsStr.isEmpty()) {
            showError("Vui lòng điền đầy đủ thông tin bắt buộc (*)");
            return;
        }

        LocalDateTime depTime = ((Date) spnDepTime.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime arrTime = ((Date) spnArrTime.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        if (!depTime.isBefore(arrTime)) {
            showError("Giờ bay phải trước giờ đến!");
            return;
        }

        int seats;
        try {
            seats = Integer.parseInt(seatsStr);
            if (seats <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("Số ghế phải là số nguyên dương!");
            return;
        }

        FlightStatus status = FlightStatus.SCHEDULED;
        if (cmbStatus != null) {
            String sel = (String) cmbStatus.getSelectedItem();
            for (FlightStatus s : FlightStatus.values()) {
                if (s.getDisplayName().equals(sel)) { status = s; break; }
            }
        }

        Flight f = flight != null ? flight : new Flight();
        f.setFlightCode(code);
        f.setAirline(airline.getName());
        f.setDepartureAirport(dep.getCity() + " (" + dep.getCode() + ")");
        f.setArrivalAirport(arr.getCity() + " (" + arr.getCode() + ")");
        f.setDepartureTime(depTime);
        f.setArrivalTime(arrTime);
        f.setTotalSeats(seats);
        if (f.getAvailableSeats() == null) f.setAvailableSeats(seats);
        f.setPrice(BigDecimal.ZERO);
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

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.SMALL_FONT);
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    private JTextField createField(String tooltip) {
        JTextField tf = new JTextField();
        tf.setFont(UIConstants.NORMAL_FONT);
        tf.setPreferredSize(new Dimension(0, UIConstants.INPUT_HEIGHT));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        tf.setToolTipText(tooltip);
        return tf;
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
