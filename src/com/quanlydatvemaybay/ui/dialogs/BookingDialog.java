package com.quanlydatvemaybay.ui.dialogs;

import com.quanlydatvemaybay.entity.Booking;
import com.quanlydatvemaybay.entity.Flight;
import com.quanlydatvemaybay.entity.Ticket;
import com.quanlydatvemaybay.enums.BookingStatus;
import com.quanlydatvemaybay.enums.FlightStatus;
import com.quanlydatvemaybay.enums.TicketStatus;
import com.quanlydatvemaybay.service.BookingService;
import com.quanlydatvemaybay.service.FlightService;
import com.quanlydatvemaybay.service.TicketService;
import com.quanlydatvemaybay.ui.UIConstants;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BookingDialog extends JDialog {

    private boolean confirmed = false;
    private final Booking booking;
    private final BookingService bookingService = new BookingService();
    private final FlightService flightService = new FlightService();
    private final TicketService ticketService = new TicketService();
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private JComboBox<String> cmbFlight;
    private JComboBox<String> cmbTicket;
    private JLabel lblFlightInfo;
    private JTextField txtName, txtEmail, txtPhone, txtIdCard;
    private JComboBox<String> cmbStatus;
    private List<Flight> flightList = new ArrayList<>();
    private List<Ticket> ticketList = new ArrayList<>();

    public BookingDialog(Frame parent, Booking booking) {
        super(parent, booking == null ? "Đặt vé mới" : "Sửa đặt vé", true);
        this.booking = booking;
        setSize(520, booking == null ? 580 : 460);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
        if (booking != null) fillData();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIConstants.BG_COLOR);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(142, 68, 173));
        header.setPreferredSize(new Dimension(0, 55));
        header.setBorder(new EmptyBorder(0, 20, 0, 20));
        JLabel title = new JLabel(booking == null ? "  Đặt vé mới" : "  Cập nhật đặt vé");
        title.setFont(UIConstants.TITLE_FONT);
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 30, 10, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        if (booking == null) {
            // --- Chọn chuyến bay ---
            row = addLabel(form, gbc, row, "Chuyến bay *");
            gbc.gridx = 1; gbc.gridy = row - 1; gbc.weightx = 1;
            cmbFlight = new JComboBox<>();
            cmbFlight.setFont(UIConstants.NORMAL_FONT);
            cmbFlight.setPreferredSize(new Dimension(340, UIConstants.INPUT_HEIGHT));
            form.add(cmbFlight, gbc);

            // Thông tin chuyến bay
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 2;
            lblFlightInfo = new JLabel(" ");
            lblFlightInfo.setFont(UIConstants.SMALL_FONT);
            lblFlightInfo.setForeground(new Color(80, 120, 180));
            lblFlightInfo.setBorder(new EmptyBorder(0, 2, 4, 0));
            form.add(lblFlightInfo, gbc);
            gbc.gridwidth = 1;
            row++;

            // --- Chọn vé ---
            row = addLabel(form, gbc, row, "Vé / Ghế ngồi *");
            gbc.gridx = 1; gbc.gridy = row - 1; gbc.weightx = 1;
            cmbTicket = new JComboBox<>();
            cmbTicket.setFont(UIConstants.NORMAL_FONT);
            cmbTicket.setPreferredSize(new Dimension(340, UIConstants.INPUT_HEIGHT));
            form.add(cmbTicket, gbc);

            // Separator
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(220, 220, 220));
            form.add(sep, gbc);
            gbc.gridwidth = 1;
            row++;
        }

        // --- Thông tin hành khách ---
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JLabel lblSection = new JLabel("Thông tin hành khách");
        lblSection.setFont(UIConstants.HEADER_FONT);
        lblSection.setForeground(UIConstants.PRIMARY_DARK);
        form.add(lblSection, gbc);
        gbc.gridwidth = 1;
        row++;

        txtName   = addField(form, gbc, row++, "Họ tên *");
        txtPhone  = addField(form, gbc, row++, "Số điện thoại");
        txtEmail  = addField(form, gbc, row++, "Email");
        txtIdCard = addField(form, gbc, row++, "CCCD / Hộ chiếu");

        // --- Trạng thái (chỉ khi sửa) ---
        if (booking != null) {
            row = addLabel(form, gbc, row, "Trạng thái");
            gbc.gridx = 1; gbc.gridy = row - 1; gbc.weightx = 1;
            BookingStatus[] statuses = BookingStatus.values();
            String[] names = new String[statuses.length];
            for (int i = 0; i < statuses.length; i++) names[i] = statuses[i].getDisplayName();
            cmbStatus = new JComboBox<>(names);
            cmbStatus.setFont(UIConstants.NORMAL_FONT);
            cmbStatus.setPreferredSize(new Dimension(340, UIConstants.INPUT_HEIGHT));
            form.add(cmbStatus, gbc);
        }

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JButton btnCancel = createBtn("Hủy", new Color(150, 150, 150));
        JButton btnSave = createBtn(booking == null ? "Đặt vé" : "Lưu", new Color(142, 68, 173));
        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> save());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        main.add(header, BorderLayout.NORTH);
        main.add(new JScrollPane(form), BorderLayout.CENTER);
        main.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(main);

        if (booking == null) {
            loadFlights();
            cmbFlight.addActionListener(e -> onFlightSelected());
        }
    }

    private int addLabel(JPanel form, GridBagConstraints gbc, int row, String text) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.NORMAL_FONT);
        lbl.setForeground(new Color(60, 60, 60));
        form.add(lbl, gbc);
        return row + 1;
    }

    private JTextField addField(JPanel form, GridBagConstraints gbc, int row, String label) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.NORMAL_FONT);
        lbl.setForeground(new Color(60, 60, 60));
        form.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        JTextField tf = new JTextField();
        tf.setFont(UIConstants.NORMAL_FONT);
        tf.setPreferredSize(new Dimension(340, UIConstants.INPUT_HEIGHT));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        form.add(tf, gbc);
        return tf;
    }

    private void loadFlights() {
        try {
            flightList = flightService.getAll();
            cmbFlight.addItem("-- Chọn chuyến bay --");
            for (Flight f : flightList) {
                // Chỉ hiện chuyến bay có thể đặt vé
                if (f.getStatus() == FlightStatus.CANCELLED ||
                    f.getStatus() == FlightStatus.ARRIVED ||
                    f.getStatus() == FlightStatus.DEPARTED) continue;
                cmbFlight.addItem(f.getFlightCode() + " | " + f.getDepartureAirport()
                        + " → " + f.getArrivalAirport()
                        + " | " + (f.getDepartureTime() != null ? f.getDepartureTime().format(DTF) : ""));
            }
        } catch (Exception e) {
            cmbFlight.addItem("Lỗi tải danh sách");
        }
    }

    private void onFlightSelected() {
        cmbTicket.removeAllItems();
        ticketList.clear();
        lblFlightInfo.setText(" ");

        int idx = cmbFlight.getSelectedIndex();
        // idx 0 = "-- Chọn chuyến bay --", idx 1+ = flight (nhưng danh sách flight đã lọc)
        if (idx <= 0) return;

        // Tìm flight tương ứng — cần map lại vì đã bỏ các flight bị hủy
        String selected = (String) cmbFlight.getSelectedItem();
        if (selected == null) return;
        String code = selected.split("\\|")[0].trim();

        Flight flight = flightList.stream()
                .filter(f -> f.getFlightCode().equals(code))
                .findFirst().orElse(null);
        if (flight == null) return;

        lblFlightInfo.setText("Ghế còn trống: " + flight.getAvailableSeats()
                + "  |  Giá từ: " + String.format("%,.0f VNĐ", flight.getPrice()));

        try {
            ticketList = ticketService.search(flight.getId(), TicketStatus.AVAILABLE);
            if (ticketList.isEmpty()) {
                cmbTicket.addItem("-- Không còn vé trống --");
            } else {
                for (Ticket t : ticketList) {
                    cmbTicket.addItem("Ghế " + t.getSeatNumber()
                            + "  [" + (t.getTicketClass() != null ? t.getTicketClass().getDisplayName() : "") + "]"
                            + "  -  " + String.format("%,.0f VNĐ", t.getPrice()));
                }
            }
        } catch (Exception e) {
            cmbTicket.addItem("Lỗi tải danh sách vé");
        }
    }

    private void fillData() {
        txtName.setText(booking.getPassengerName());
        txtPhone.setText(booking.getPassengerPhone() != null ? booking.getPassengerPhone() : "");
        txtEmail.setText(booking.getPassengerEmail() != null ? booking.getPassengerEmail() : "");
        txtIdCard.setText(booking.getPassengerIdCard() != null ? booking.getPassengerIdCard() : "");
        if (cmbStatus != null && booking.getStatus() != null) {
            cmbStatus.setSelectedItem(booking.getStatus().getDisplayName());
        }
    }

    private void save() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên hành khách!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String email  = txtEmail.getText().trim();
        String phone  = txtPhone.getText().trim();
        String idCard = txtIdCard.getText().trim();

        try {
            if (booking == null) {
                if (cmbFlight.getSelectedIndex() <= 0) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn chuyến bay!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (ticketList.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Không có vé trống cho chuyến bay này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int ticketIdx = cmbTicket.getSelectedIndex();
                if (ticketIdx < 0 || ticketIdx >= ticketList.size()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn ghế!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Long ticketId = ticketList.get(ticketIdx).getId();
                bookingService.create(ticketId, name,
                        email.isEmpty() ? null : email,
                        phone.isEmpty() ? null : phone,
                        idCard.isEmpty() ? null : idCard);
                JOptionPane.showMessageDialog(this, "Đặt vé thành công!");
            } else {
                BookingStatus status = booking.getStatus();
                if (cmbStatus != null) {
                    String sel = (String) cmbStatus.getSelectedItem();
                    for (BookingStatus s : BookingStatus.values()) {
                        if (s.getDisplayName().equals(sel)) { status = s; break; }
                    }
                }
                bookingService.update(booking.getId(), name,
                        email.isEmpty() ? null : email,
                        phone.isEmpty() ? null : phone,
                        idCard.isEmpty() ? null : idCard, status);
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            }
            confirmed = true;
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton createBtn(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(UIConstants.BUTTON_FONT);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(110, UIConstants.BUTTON_HEIGHT));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public boolean isConfirmed() { return confirmed; }
}
