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
    private final Ticket ticket;
    private final List<Flight> flightList;
    private final TicketService ticketService = new TicketService();

    // Single-edit fields
    private JComboBox<String> cmbFlight;
    private JTextField txtSeat, txtPrice;
    private JComboBox<String> cmbClass;

    // Bulk-mode fields (one row per TicketClass)
    private JComboBox<String> cmbBulkFlight;
    private JLabel lblAvailable;
    private JTextField[] bulkPrefix  = new JTextField[3];
    private JTextField[] bulkCount   = new JTextField[3];
    private JTextField[] bulkPrice   = new JTextField[3];
    private JLabel lblTotal;

    private static final TicketClass[] CLASSES = TicketClass.values();
    private static final String[] DEFAULT_PREFIX = {"PT", "TG", "HN"};

    public TicketDialog(Frame parent, Ticket ticket, List<Flight> flightList) {
        super(parent, ticket == null ? "Thêm vé" : "Sửa vé", true);
        this.ticket = ticket;
        this.flightList = flightList;
        setSize(460, 380);
        setLocationRelativeTo(parent);
        setResizable(false);
        initSingleUI();
        if (ticket != null) fillData();
    }

    // ─── Single / Edit mode ──────────────────────────────────────────────────

    private void initSingleUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIConstants.BG_COLOR);
        main.add(makeHeader(ticket == null ? "🎫  Thêm vé mới" : "🎫  Cập nhật vé"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 30, 10, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 4, 7, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        // Flight
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; form.add(lbl("Chuyến bay *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        cmbFlight = new JComboBox<>();
        cmbFlight.setFont(UIConstants.NORMAL_FONT);
        cmbFlight.setPreferredSize(new Dimension(250, UIConstants.INPUT_HEIGHT));
        if (flightList != null) flightList.forEach(f ->
            cmbFlight.addItem(f.getFlightCode() + " - " + f.getDepartureAirport() + "→" + f.getArrivalAirport()));
        form.add(cmbFlight, gbc);
        row++;

        // Seat
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; form.add(lbl("Số ghế *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtSeat = createTextField();
        form.add(txtSeat, gbc);
        row++;

        // Class
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; form.add(lbl("Hạng ghế *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        String[] classNames = new String[CLASSES.length];
        for (int i = 0; i < CLASSES.length; i++) classNames[i] = CLASSES[i].getDisplayName();
        cmbClass = new JComboBox<>(classNames);
        cmbClass.setFont(UIConstants.NORMAL_FONT);
        cmbClass.setPreferredSize(new Dimension(250, UIConstants.INPUT_HEIGHT));
        form.add(cmbClass, gbc);
        row++;

        // Price
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; form.add(lbl("Giá vé (VNĐ) *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtPrice = createTextField();
        form.add(txtPrice, gbc);

        main.add(form, BorderLayout.CENTER);
        main.add(makeBtnPanel("Lưu", e -> saveSingle()), BorderLayout.SOUTH);
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

    private void saveSingle() {
        int idx = cmbFlight.getSelectedIndex();
        if (idx < 0 || flightList == null || flightList.isEmpty()) {
            err("Vui lòng chọn chuyến bay!"); return;
        }
        Long flightId = flightList.get(idx).getId();
        String seat = txtSeat.getText().trim();
        if (seat.isEmpty()) { err("Vui lòng nhập số ghế!"); return; }
        String priceStr = txtPrice.getText().trim().replace(",", "");
        if (priceStr.isEmpty()) { err("Vui lòng nhập giá vé!"); return; }
        BigDecimal price;
        try { price = new BigDecimal(priceStr); } catch (NumberFormatException e) { err("Giá vé phải là số!"); return; }
        TicketClass tc = resolveClass((String) cmbClass.getSelectedItem());
        try {
            if (ticket == null) ticketService.create(flightId, seat, tc, price);
            else ticketService.update(ticket.getId(), flightId, seat, tc, price);
            JOptionPane.showMessageDialog(this, ticket == null ? "Thêm vé thành công!" : "Cập nhật vé thành công!");
            confirmed = true;
            dispose();
        } catch (Exception e) { err("Lỗi: " + e.getMessage()); }
    }

    // ─── Bulk mode ────────────────────────────────────────────────────────────

    public void setBulkMode(boolean bulk) {
        if (!bulk) return;
        setTitle("Thêm vé hàng loạt");
        setSize(580, 430);
        setLocationRelativeTo(getOwner());

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIConstants.BG_COLOR);
        main.add(makeHeader("🎫  Thêm vé hàng loạt"), BorderLayout.NORTH);
        main.add(buildBulkForm(), BorderLayout.CENTER);
        main.add(makeBtnPanel("Lưu", e -> saveBulk()), BorderLayout.SOUTH);
        setContentPane(main);
        revalidate();
    }

    private JPanel buildBulkForm() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(16, 24, 8, 24));

        // Flight row
        JPanel flightRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        flightRow.setBackground(Color.WHITE);
        JLabel flightLbl = lbl("Chuyến bay *");
        flightLbl.setPreferredSize(new Dimension(90, UIConstants.INPUT_HEIGHT));
        cmbBulkFlight = new JComboBox<>();
        cmbBulkFlight.setFont(UIConstants.NORMAL_FONT);
        cmbBulkFlight.setPreferredSize(new Dimension(260, UIConstants.INPUT_HEIGHT));
        if (flightList != null) flightList.forEach(f ->
            cmbBulkFlight.addItem(f.getFlightCode() + " - " + f.getDepartureAirport() + "→" + f.getArrivalAirport()));
        lblAvailable = new JLabel();
        lblAvailable.setFont(UIConstants.SMALL_FONT);
        lblAvailable.setForeground(new Color(39, 174, 96));
        flightRow.add(flightLbl);
        flightRow.add(cmbBulkFlight);
        flightRow.add(lblAvailable);
        updateAvailableLabel();
        cmbBulkFlight.addActionListener(e -> { updateAvailableLabel(); updateTotal(); });

        // Header row of table
        JPanel tablePanel = new JPanel(new GridBagLayout());
        tablePanel.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(3, 4, 3, 4);

        // Column headers
        String[] headers = {"Hạng ghế", "Tiền tố", "Số lượng", "Giá vé (VNĐ)"};
        int[] widths = {100, 70, 80, 120};
        for (int col = 0; col < headers.length; col++) {
            g.gridx = col; g.gridy = 0; g.weightx = col == 3 ? 1.0 : 0;
            JLabel h = new JLabel(headers[col], SwingConstants.CENTER);
            h.setFont(UIConstants.SMALL_FONT.deriveFont(Font.BOLD));
            h.setForeground(new Color(60, 60, 60));
            h.setPreferredSize(new Dimension(widths[col], 24));
            tablePanel.add(h, g);
        }

        // Separator
        g.gridx = 0; g.gridy = 1; g.gridwidth = 4; g.weightx = 1;
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(220, 220, 220));
        tablePanel.add(sep, g);
        g.gridwidth = 1;

        // Data rows: one per TicketClass
        for (int i = 0; i < CLASSES.length; i++) {
            int row = i + 2;
            g.gridy = row; g.weightx = 0;

            // Hạng ghế label
            g.gridx = 0;
            JLabel classLbl = new JLabel(CLASSES[i].getDisplayName());
            classLbl.setFont(UIConstants.NORMAL_FONT);
            tablePanel.add(classLbl, g);

            // Tiền tố
            g.gridx = 1;
            bulkPrefix[i] = smallField(DEFAULT_PREFIX[i], 70);
            tablePanel.add(bulkPrefix[i], g);

            // Số lượng
            g.gridx = 2;
            bulkCount[i] = smallField("0", 80);
            final int fi = i;
            bulkCount[i].getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { updateTotal(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { updateTotal(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { updateTotal(); }
            });
            tablePanel.add(bulkCount[i], g);

            // Giá vé
            g.gridx = 3; g.weightx = 1;
            bulkPrice[i] = smallField("", 120);
            tablePanel.add(bulkPrice[i], g);
        }

        // Total
        JPanel totalRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        totalRow.setBackground(Color.WHITE);
        lblTotal = new JLabel();
        lblTotal.setFont(UIConstants.SMALL_FONT);
        totalRow.add(lblTotal);
        updateTotal();

        wrapper.add(flightRow, BorderLayout.NORTH);
        wrapper.add(tablePanel, BorderLayout.CENTER);
        wrapper.add(totalRow, BorderLayout.SOUTH);
        return wrapper;
    }

    private void updateAvailableLabel() {
        int idx = cmbBulkFlight.getSelectedIndex();
        if (idx >= 0 && flightList != null && idx < flightList.size()) {
            int avail = flightList.get(idx).getAvailableSeats();
            lblAvailable.setText("  Còn " + avail + " ghế trống");
        } else {
            lblAvailable.setText("");
        }
    }

    private void updateTotal() {
        int total = 0;
        for (JTextField f : bulkCount) {
            try { total += Integer.parseInt(f.getText().trim()); } catch (NumberFormatException ignored) {}
        }
        int avail = getAvailableSeats();
        lblTotal.setText("Tổng: " + total + " / " + avail + " ghế trống");
        lblTotal.setForeground(total > avail ? new Color(192, 57, 43) : new Color(39, 174, 96));
    }

    private int getAvailableSeats() {
        int idx = cmbBulkFlight.getSelectedIndex();
        if (idx >= 0 && flightList != null && idx < flightList.size())
            return flightList.get(idx).getAvailableSeats();
        return 0;
    }

    private void saveBulk() {
        int idx = cmbBulkFlight.getSelectedIndex();
        if (idx < 0 || flightList == null || flightList.isEmpty()) { err("Vui lòng chọn chuyến bay!"); return; }
        Long flightId = flightList.get(idx).getId();
        int avail = getAvailableSeats();

        // Parse all rows
        int[] counts = new int[3];
        BigDecimal[] prices = new BigDecimal[3];
        String[] prefixes = new String[3];
        int total = 0;

        for (int i = 0; i < CLASSES.length; i++) {
            String cntStr = bulkCount[i].getText().trim();
            try { counts[i] = Integer.parseInt(cntStr); }
            catch (NumberFormatException e) { err("Số lượng hạng \"" + CLASSES[i].getDisplayName() + "\" không hợp lệ!"); return; }
            if (counts[i] < 0) { err("Số lượng không được âm!"); return; }
            total += counts[i];

            if (counts[i] > 0) {
                String priceStr = bulkPrice[i].getText().trim().replace(",", "");
                if (priceStr.isEmpty()) { err("Vui lòng nhập giá vé cho hạng \"" + CLASSES[i].getDisplayName() + "\"!"); return; }
                try { prices[i] = new BigDecimal(priceStr); }
                catch (NumberFormatException e) { err("Giá vé hạng \"" + CLASSES[i].getDisplayName() + "\" không hợp lệ!"); return; }
                if (prices[i].compareTo(BigDecimal.ZERO) <= 0) { err("Giá vé phải lớn hơn 0!"); return; }
            }
            prefixes[i] = bulkPrefix[i].getText().trim();
            if (prefixes[i].isEmpty()) prefixes[i] = DEFAULT_PREFIX[i];
        }

        if (total == 0) { err("Vui lòng nhập số lượng cho ít nhất một hạng ghế!"); return; }
        if (total > avail) {
            err("Tổng số vé (" + total + ") vượt quá số ghế trống (" + avail + ") của chuyến bay!");
            return;
        }

        try {
            int created = 0;
            for (int i = 0; i < CLASSES.length; i++) {
                if (counts[i] > 0) {
                    ticketService.createBulk(flightId, counts[i], CLASSES[i], prices[i], prefixes[i]);
                    created += counts[i];
                }
            }
            JOptionPane.showMessageDialog(this, "Tạo " + created + " vé thành công!");
            confirmed = true;
            dispose();
        } catch (Exception e) {
            err("Lỗi: " + e.getMessage());
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private JPanel makeHeader(String text) {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(UIConstants.SECONDARY_COLOR);
        h.setPreferredSize(new Dimension(0, 55));
        h.setBorder(new EmptyBorder(0, 20, 0, 20));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(Color.WHITE);
        h.add(lbl, BorderLayout.WEST);
        return h;
    }

    private JPanel makeBtnPanel(String saveLabel, java.awt.event.ActionListener onSave) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        p.setBackground(Color.WHITE);
        p.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));
        JButton btnCancel = createBtn("Hủy", new Color(150, 150, 150));
        btnCancel.addActionListener(e -> dispose());
        JButton btnSave = createBtn(saveLabel, UIConstants.SECONDARY_COLOR);
        btnSave.addActionListener(onSave);
        p.add(btnCancel);
        p.add(btnSave);
        return p;
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIConstants.NORMAL_FONT);
        return l;
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

    private JTextField smallField(String defaultText, int width) {
        JTextField tf = new JTextField(defaultText);
        tf.setFont(UIConstants.NORMAL_FONT);
        tf.setPreferredSize(new Dimension(width, UIConstants.INPUT_HEIGHT));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        tf.setHorizontalAlignment(JTextField.CENTER);
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

    private void err(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private TicketClass resolveClass(String displayName) {
        for (TicketClass c : CLASSES) if (c.getDisplayName().equals(displayName)) return c;
        return TicketClass.ECONOMY;
    }

    public boolean isConfirmed() { return confirmed; }
}
