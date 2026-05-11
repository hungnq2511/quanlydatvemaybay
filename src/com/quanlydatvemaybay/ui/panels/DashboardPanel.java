package com.quanlydatvemaybay.ui.panels;

import com.quanlydatvemaybay.dao.BookingDAO;
import com.quanlydatvemaybay.dao.FlightDAO;
import com.quanlydatvemaybay.dao.TicketDAO;
import com.quanlydatvemaybay.entity.Booking;
import com.quanlydatvemaybay.entity.Flight;
import com.quanlydatvemaybay.entity.Ticket;
import com.quanlydatvemaybay.enums.BookingStatus;
import com.quanlydatvemaybay.enums.FlightStatus;
import com.quanlydatvemaybay.enums.TicketStatus;
import com.quanlydatvemaybay.ui.UIConstants;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class DashboardPanel extends JPanel {

    private JPanel cardsPanel;
    private JLabel lblUpdated;

    public DashboardPanel() {
        setBackground(UIConstants.BG_COLOR);
        setLayout(new BorderLayout());
        initUI();
        loadCards();
    }

    public void refresh() { loadCards(); }

    private void initUI() {
        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        topBar.setPreferredSize(new Dimension(0, 60));

        JLabel titleLabel = new JLabel("   Tổng Quan Hệ Thống");
        titleLabel.setFont(UIConstants.TITLE_FONT);
        titleLabel.setForeground(UIConstants.PRIMARY_DARK);

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        rightBar.setBackground(Color.WHITE);
        lblUpdated = new JLabel();
        lblUpdated.setFont(UIConstants.SMALL_FONT);
        lblUpdated.setForeground(new Color(160, 160, 160));

        JButton btnRefresh = new JButton("↻  Làm mới");
        btnRefresh.setFont(UIConstants.SMALL_FONT);
        btnRefresh.setBackground(UIConstants.PRIMARY_COLOR);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setOpaque(true);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setPreferredSize(new Dimension(100, 32));
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadCards());

        rightBar.add(lblUpdated);
        rightBar.add(btnRefresh);
        topBar.add(titleLabel, BorderLayout.WEST);
        topBar.add(rightBar, BorderLayout.EAST);

        // Cards grid: 3 columns × 2 rows
        cardsPanel = new JPanel(new GridLayout(2, 3, 18, 18));
        cardsPanel.setBackground(UIConstants.BG_COLOR);
        cardsPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        add(topBar, BorderLayout.NORTH);
        add(cardsPanel, BorderLayout.CENTER);
    }

    private void loadCards() {
        cardsPanel.removeAll();
        try {
            FlightDAO flightDAO   = new FlightDAO();
            TicketDAO ticketDAO   = new TicketDAO();
            BookingDAO bookingDAO = new BookingDAO();

            List<Flight>  flights  = flightDAO.findAll();
            List<Ticket>  tickets  = ticketDAO.findAll();
            List<Booking> bookings = bookingDAO.findAll();

            // Flights
            long totalFlights     = flights.size();
            long scheduledFlights = flights.stream().filter(f -> f.getStatus() == FlightStatus.SCHEDULED).count();
            long delayedFlights   = flights.stream().filter(f -> f.getStatus() == FlightStatus.DELAYED).count();
            long cancelledFlights = flights.stream().filter(f -> f.getStatus() == FlightStatus.CANCELLED).count();

            // Tickets
            long totalTickets     = tickets.size();
            long availableTickets = tickets.stream().filter(t -> t.getStatus() == TicketStatus.AVAILABLE).count();
            long bookedTickets    = tickets.stream().filter(t -> t.getStatus() == TicketStatus.BOOKED).count();
            int  fillPct          = totalTickets > 0 ? (int) (bookedTickets * 100 / totalTickets) : 0;

            // Bookings
            long totalBookings     = bookings.size();
            long pendingBookings   = bookings.stream().filter(b -> b.getStatus() == BookingStatus.PENDING).count();
            long confirmedBookings = bookings.stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count();
            long cancelledBookings = bookings.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();
            long todayBookings     = bookings.stream()
                    .filter(b -> b.getBookingDate() != null && b.getBookingDate().toLocalDate().equals(LocalDate.now()))
                    .count();

            // Card 1: Chuyến Bay
            cardsPanel.add(createCard(
                    "Chuyến Bay", String.valueOf(totalFlights), UIConstants.PRIMARY_COLOR,
                    "✈",
                    "Đã lên lịch", scheduledFlights,
                    "Bị trễ / Hủy", delayedFlights + cancelledFlights
            ));

            // Card 2: Vé Máy Bay
            cardsPanel.add(createCard(
                    "Vé Máy Bay", String.valueOf(totalTickets), UIConstants.SECONDARY_COLOR,
                    "🎫",
                    "Còn trống", availableTickets,
                    "Đã đặt", bookedTickets
            ));

            // Card 3: Tỷ lệ lấp đầy
            cardsPanel.add(createFillCard(fillPct, bookedTickets, totalTickets));

            // Card 4: Tổng Đặt Vé
            cardsPanel.add(createCard(
                    "Tổng Đặt Vé", String.valueOf(totalBookings), UIConstants.WARNING_COLOR,
                    "📋",
                    "Hôm nay", todayBookings,
                    "Chờ xác nhận", pendingBookings
            ));

            // Card 5: Đã Xác Nhận
            cardsPanel.add(createCard(
                    "Đã Xác Nhận", String.valueOf(confirmedBookings), new Color(39, 174, 96),
                    "✅",
                    "Tỷ lệ", confirmedBookings + "/" + totalBookings,
                    "Đã hủy", String.valueOf(cancelledBookings)
            ));

            // Card 6: Đã Hủy
            cardsPanel.add(createCard(
                    "Đã Hủy", String.valueOf(cancelledBookings), new Color(192, 57, 43),
                    "🚫",
                    "Chờ xử lý", pendingBookings,
                    "Tổng booking", totalBookings
            ));

            lblUpdated.setText("Cập nhật: " + java.time.LocalTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "  ");

        } catch (Exception e) {
            for (int i = 0; i < 6; i++)
                cardsPanel.add(createCard("Lỗi", "N/A", UIConstants.DANGER_COLOR, "⚠",
                        "Chi tiết", 0L, e.getMessage(), 0L));
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private JPanel createCard(String title, String value, Color accentColor, String icon,
                              String sub1Label, long sub1Val, String sub2Label, long sub2Val) {
        return createCard(title, value, accentColor, icon,
                sub1Label, String.valueOf(sub1Val),
                sub2Label, String.valueOf(sub2Val));
    }

    private JPanel createCard(String title, String value, Color accentColor, String icon,
                              String sub1Label, long sub1Val, String sub2Label, String sub2Val) {
        return createCard(title, value, accentColor, icon,
                sub1Label, String.valueOf(sub1Val), sub2Label, sub2Val);
    }

    private JPanel createCard(String title, String value, Color accentColor, String icon,
                              String sub1Label, String sub1Val, String sub2Label, String sub2Val) {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(0, 0, 0, 0)));

        // Colored top accent bar
        JPanel accent = new JPanel();
        accent.setBackground(accentColor);
        accent.setPreferredSize(new Dimension(0, 5));
        card.add(accent, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top row: title + icon
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(Color.WHITE);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UIConstants.NORMAL_FONT);
        titleLbl.setForeground(new Color(110, 110, 110));
        JLabel iconLbl = new JLabel(icon, SwingConstants.RIGHT);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        iconLbl.setForeground(new Color(200, 200, 200));
        topRow.add(titleLbl, BorderLayout.WEST);
        topRow.add(iconLbl, BorderLayout.EAST);

        // Big value
        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 38));
        valueLbl.setForeground(accentColor);
        valueLbl.setBorder(new EmptyBorder(6, 0, 8, 0));

        // Sub-stats
        JPanel subPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        subPanel.setBackground(Color.WHITE);
        subPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(240, 240, 240)));
        subPanel.setPreferredSize(new Dimension(0, 36));

        subPanel.add(subStat(sub1Label, sub1Val));
        subPanel.add(subStat(sub2Label, sub2Val));

        body.add(topRow,   BorderLayout.NORTH);
        body.add(valueLbl, BorderLayout.CENTER);
        body.add(subPanel, BorderLayout.SOUTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel createFillCard(int pct, long booked, long total) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(0, 0, 0, 0)));

        Color accentColor = pct >= 80 ? new Color(192, 57, 43)
                          : pct >= 50 ? new Color(230, 126, 34)
                          : new Color(39, 174, 96);

        JPanel accent = new JPanel();
        accent.setBackground(accentColor);
        accent.setPreferredSize(new Dimension(0, 5));
        card.add(accent, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(Color.WHITE);
        JLabel titleLbl = new JLabel("Tỷ lệ lấp đầy");
        titleLbl.setFont(UIConstants.NORMAL_FONT);
        titleLbl.setForeground(new Color(110, 110, 110));
        JLabel iconLbl = new JLabel("📊", SwingConstants.RIGHT);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        iconLbl.setForeground(new Color(200, 200, 200));
        topRow.add(titleLbl, BorderLayout.WEST);
        topRow.add(iconLbl, BorderLayout.EAST);

        JLabel valueLbl = new JLabel(pct + "%");
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 38));
        valueLbl.setForeground(accentColor);
        valueLbl.setBorder(new EmptyBorder(6, 0, 6, 0));

        // Progress bar
        JPanel progressWrap = new JPanel(new BorderLayout(0, 4));
        progressWrap.setBackground(Color.WHITE);
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(pct);
        bar.setStringPainted(false);
        bar.setForeground(accentColor);
        bar.setBackground(new Color(235, 235, 235));
        bar.setPreferredSize(new Dimension(0, 8));
        bar.setBorderPainted(false);
        JLabel subLbl = new JLabel(booked + " / " + total + " ghế đã đặt");
        subLbl.setFont(UIConstants.SMALL_FONT);
        subLbl.setForeground(new Color(130, 130, 130));
        progressWrap.add(bar, BorderLayout.CENTER);
        progressWrap.add(subLbl, BorderLayout.SOUTH);

        body.add(topRow,       BorderLayout.NORTH);
        body.add(valueLbl,     BorderLayout.CENTER);
        body.add(progressWrap, BorderLayout.SOUTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel subStat(String label, String value) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 1));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(6, 0, 0, 0));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lbl.setForeground(new Color(150, 150, 150));
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 13));
        val.setForeground(new Color(60, 60, 60));
        p.add(lbl);
        p.add(val);
        return p;
    }

    private JPanel subStat(String label, long value) {
        return subStat(label, String.valueOf(value));
    }
}
