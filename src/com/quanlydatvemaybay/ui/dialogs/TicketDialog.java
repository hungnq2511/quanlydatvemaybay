package com.quanlydatvemaybay.ui.dialogs;

import com.quanlydatvemaybay.entity.Flight;
import com.quanlydatvemaybay.entity.Ticket;
import com.quanlydatvemaybay.enums.TicketClass;
import com.quanlydatvemaybay.service.TicketService;
import com.quanlydatvemaybay.ui.UIConstants;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class TicketDialog extends JDialog {

    private boolean confirmed = false;
    private boolean bulkMode = false;
    private final Ticket ticket;
    private final List<Flight> flightList;
    private final TicketService ticketService = new TicketService();

    private JComboBox<String> cmbFlight;
    private JTextField txtSeat, txtPrice, txtCount, txtPrefix;
    private JComboBox<String> cmbClass;
    private JLabel lblCount, lblPrefix;

    public TicketDialog(Frame parent, Ticket ticket, List<Flight> flightList) {
        super(parent, ticket == null ? "Thêm vé" : "Sửa vé", true);
        this.ticket = ticket;
        this.flightList = flightList;
        setSize(460, 420);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
        if (ticket != null) fillData();
    }

    public void setBulkMode(boolean bulkMode) {
        this.bulkMode = bulkMode;
        setTitle("Thêm vé hàng loạt");
        lblCount.setVisible(true);
        txtCount.setVisible(true);
        lblPrefix.setVisible(true);
        txtPrefix.setVisible(true);
        txtSeat.setVisible(false);
        // find seat label
        for (Component c : txtSeat.getParent().getComponents()) {
            if (c instanceof JLabel && ((JLabel)c).getText().equals("Số ghế *")) {
                c.setVisible(false);
                break;
            }
        }
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIConstants.BG_COLOR);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.SECONDARY_COLOR);
        header.setPreferredSize(new Dimension(0, 55));
        header.setBorder(new EmptyBorder(0, 20, 0, 20));
        JLabel title = new JLabel(ticket == null ? "🎫  Thêm vé mới" : "🎫  Cập nhật vé");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 30, 10, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 4, 7, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Flight
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblFlight = new JLabel("Chuyến bay *");
        lblFlight.setFont(UIConstants.NORMAL_FONT);
        form.add(lblFlight, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        cmbFlight = new JComboBox<>();
        cmbFlight.setFont(UIConstants.NORMAL_FONT);
        cmbFlight.setPreferredSize(new Dimension(250, UIConstants.INPUT_HEIGHT));
        if (flightList != null) {
            for (Flight f : flightList) cmbFlight.addItem(f.getFlightCode() + " - " + f.getDepartureAirport() + "→" + f.getArrivalAirport());
        }
        form.add(cmbFlight, gbc);

        // Seat number
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblSeat = new JLabel("Số ghế *");
        lblSeat.setFont(UIConstants.NORMAL_FONT);
        form.add(lblSeat, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        txtSeat = createTextField();
        form.add(txtSeat, gbc);

        // Count (bulk)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        lblCount = new JLabel("Số lượng *");
        lblCount.setFont(UIConstants.NORMAL_FONT);
        lblCount.setVisible(false);
        form.add(lblCount, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        txtCount = createTextField();
        txtCount.setVisible(false);
        txtCount.setText("10");
        form.add(txtCount, gbc);

        // Prefix (bulk)
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        lblPrefix = new JLabel("Tiền tố ghế");
        lblPrefix.setFont(UIConstants.NORMAL_FONT);
        lblPrefix.setVisible(false);
        form.add(lblPrefix, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        txtPrefix = createTextField();
        txtPrefix.setText("S");
        txtPrefix.setVisible(false);
        form.add(txtPrefix, gbc);

        // Class
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        JLabel lblClass = new JLabel("Hạng ghế *");
        lblClass.setFont(UIConstants.NORMAL_FONT);
        form.add(lblClass, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        TicketClass[] classes = TicketClass.values();
        String[] classNames = new String[classes.length];
        for (int i = 0; i < classes.length; i++) classNames[i] = classes[i].getDisplayName();
        cmbClass = new JComboBox<>(classNames);
        cmbClass.setFont(UIConstants.NORMAL_FONT);
        cmbClass.setPreferredSize(new Dimension(250, UIConstants.INPUT_HEIGHT));
        form.add(cmbClass, gbc);

        // Price
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0;
        JLabel lblPrice = new JLabel("Giá vé (VNĐ) *");
        lblPrice.setFont(UIConstants.NORMAL_FONT);
        form.add(lblPrice, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        txtPrice = createTextField();
        form.add(txtPrice, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JButton btnCancel = createBtn("Hủy", new Color(150, 150, 150));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = createBtn("Lưu", UIConstants.SECONDARY_COLOR);
        btnSave.addActionListener(e -> save());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        main.add(header, BorderLayout.NORTH);
        main.add(form, BorderLayout.CENTER);
        main.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(main);
    }

    private void fillData() {
        if (flightList != null) {
            for (int i = 0; i < flightList.size(); i++) {
                if (flightList.get(i).getId().equals(ticket.getFlightId())) {
                    cmbFlight.setSelectedIndex(i);
                    break;
                }
            }
        }
        txtSeat.setText(ticket.getSeatNumber());
        if (ticket.getPrice() != null) txtPrice.setText(ticket.getPrice().toPlainString());
        if (ticket.getTicketClass() != null) cmbClass.setSelectedItem(ticket.getTicketClass().getDisplayName());
    }

    private void save() {
        int flightIdx = cmbFlight.getSelectedIndex();
        if (flightIdx < 0 || flightList == null || flightList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn chuyến bay!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Long flightId = flightList.get(flightIdx).getId();

        String priceStr = txtPrice.getText().trim().replace(",", "");
        if (priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập giá vé!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BigDecimal price;
        try { price = new BigDecimal(priceStr); }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá vé phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedClass = (String) cmbClass.getSelectedItem();
        TicketClass ticketClass = TicketClass.ECONOMY;
        for (TicketClass c : TicketClass.values()) {
            if (c.getDisplayName().equals(selectedClass)) { ticketClass = c; break; }
        }

        try {
            if (bulkMode) {
                String countStr = txtCount.getText().trim();
                int count;
                try { count = Integer.parseInt(countStr); }
                catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Số lượng phải là số nguyên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String prefix = txtPrefix.getText().trim();
                ticketService.createBulk(flightId, count, ticketClass, price, prefix);
                JOptionPane.showMessageDialog(this, "Tạo " + count + " vé thành công!");
            } else if (ticket == null) {
                String seat = txtSeat.getText().trim();
                if (seat.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập số ghế!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                ticketService.create(flightId, seat, ticketClass, price);
                JOptionPane.showMessageDialog(this, "Thêm vé thành công!");
            } else {
                String seat = txtSeat.getText().trim();
                if (seat.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập số ghế!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                ticketService.update(ticket.getId(), flightId, seat, ticketClass, price);
                JOptionPane.showMessageDialog(this, "Cập nhật vé thành công!");
            }
            confirmed = true;
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(UIConstants.NORMAL_FONT);
        tf.setPreferredSize(new Dimension(250, UIConstants.INPUT_HEIGHT));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return tf;
    }

    private JButton createBtn(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(UIConstants.BUTTON_FONT);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(100, UIConstants.BUTTON_HEIGHT));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public boolean isConfirmed() { return confirmed; }
}
