package com.quanlydatvemaybay.ui.panels;

import com.quanlydatvemaybay.entity.Booking;
import com.quanlydatvemaybay.entity.Flight;
import com.quanlydatvemaybay.entity.User;
import com.quanlydatvemaybay.enums.BookingStatus;
import com.quanlydatvemaybay.enums.FlightStatus;
import com.quanlydatvemaybay.service.AuthService;
import com.quanlydatvemaybay.service.BookingService;
import com.quanlydatvemaybay.service.FlightService;
import com.quanlydatvemaybay.ui.UIConstants;
import com.quanlydatvemaybay.ui.dialogs.BookingDialog;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Trang chủ cho USER:
 *  - 3 thẻ thống kê: vé sắp tới, tổng vé đã đặt, tổng chi tiêu năm nay
 *  - Card nổi bật cho chuyến gần nhất (countdown)
 *  - Section gợi ý chuyến bay phổ biến (3-5 chuyến)
 *  - Nút đặt vé mới
 */
public class UserHomePanel extends JPanel {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final BookingService bookingService = new BookingService();
    private final FlightService flightService = new FlightService();

    private JLabel lblUpcoming, lblTotalBookings, lblTotalSpent;
    private JPanel nextFlightCard;
    private JPanel suggestionsPanel;
    private Consumer<String> navigator;

    public UserHomePanel() {
        setBackground(UIConstants.BG_COLOR);
        setLayout(new BorderLayout());
        initUI();
    }

    /** Cho phép panel mở các trang khác (vd: "search", "bookings") qua MainFrame. */
    public void setNavigator(Consumer<String> navigator) {
        this.navigator = navigator;
    }

    public void refresh() { loadData(); }

    private void initUI() {
        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        topBar.setPreferredSize(new Dimension(0, 60));

        User user = AuthService.getCurrentUser();
        String name = user != null && user.getFullName() != null ? user.getFullName() : "Bạn";
        JLabel title = new JLabel("   Xin chào, " + name + " 👋");
        title.setFont(UIConstants.TITLE_FONT);
        title.setForeground(UIConstants.PRIMARY_DARK);
        topBar.add(title, BorderLayout.WEST);

        JButton btnBook = new JButton("+ Đặt vé mới");
        btnBook.setFont(UIConstants.BUTTON_FONT);
        btnBook.setBackground(UIConstants.SECONDARY_COLOR);
        btnBook.setForeground(Color.WHITE);
        btnBook.setOpaque(true);
        btnBook.setBorderPainted(false);
        btnBook.setFocusPainted(false);
        btnBook.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBook.setPreferredSize(new Dimension(170, UIConstants.BUTTON_HEIGHT));
        btnBook.addActionListener(e -> openBookingDialog());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        right.setBackground(Color.WHITE);
        right.add(btnBook);
        topBar.add(right, BorderLayout.EAST);

        // Body với scroll để màn hình nhỏ vẫn xem được
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(UIConstants.BG_COLOR);
        body.setBorder(new EmptyBorder(20, 24, 20, 24));

        body.add(buildStatsRow());
        body.add(Box.createVerticalStrut(18));
        body.add(buildNextFlightSection());
        body.add(Box.createVerticalStrut(18));
        body.add(buildSuggestionsSection());
        body.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(body,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(UIConstants.BG_COLOR);

        add(topBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        loadData();
    }

    // ── Stats row ───────────────────────────────────────────────────────────
    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setBackground(UIConstants.BG_COLOR);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        lblUpcoming = new JLabel("0");
        lblTotalBookings = new JLabel("0");
        lblTotalSpent = new JLabel("0 đ");

        row.add(statCard("Vé sắp tới", lblUpcoming, new Color(33, 150, 243)));
        row.add(statCard("Tổng vé đã đặt", lblTotalBookings, new Color(76, 175, 80)));
        row.add(statCard("Tổng chi tiêu năm nay", lblTotalSpent, new Color(255, 152, 0)));
        return row;
    }

    private JPanel statCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 4, 0, 0, accent),
                new EmptyBorder(16, 18, 16, 18)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UIConstants.NORMAL_FONT);
        lblTitle.setForeground(new Color(110, 110, 110));

        valueLabel.setFont(new Font(UIConstants.TITLE_FONT.getFontName(), Font.BOLD, 28));
        valueLabel.setForeground(accent);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // ── Next flight card ────────────────────────────────────────────────────
    private JPanel buildNextFlightSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(UIConstants.BG_COLOR);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JLabel header = new JLabel("✈ Chuyến bay sắp tới của bạn");
        header.setFont(UIConstants.HEADER_FONT);
        header.setForeground(UIConstants.PRIMARY_DARK);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));

        nextFlightCard = new JPanel(new BorderLayout());
        nextFlightCard.setBackground(Color.WHITE);
        nextFlightCard.setBorder(new EmptyBorder(20, 24, 20, 24));

        section.add(header, BorderLayout.NORTH);
        section.add(nextFlightCard, BorderLayout.CENTER);
        return section;
    }

    private void fillNextFlightCard(Booking b) {
        nextFlightCard.removeAll();
        if (b == null) {
            JLabel empty = new JLabel("Bạn chưa có chuyến bay nào sắp tới. Hãy đặt vé ngay!", SwingConstants.CENTER);
            empty.setFont(UIConstants.NORMAL_FONT);
            empty.setForeground(Color.GRAY);
            nextFlightCard.add(empty, BorderLayout.CENTER);
        } else {
            JPanel left = new JPanel();
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.setBackground(Color.WHITE);

            JLabel route = new JLabel(b.getFlightCode() + "  •  "
                    + b.getDepartureAirport() + "  →  " + b.getArrivalAirport());
            route.setFont(new Font(UIConstants.HEADER_FONT.getFontName(), Font.BOLD, 18));
            route.setForeground(UIConstants.PRIMARY_DARK);

            String dep = b.getDepartureTime() != null ? b.getDepartureTime().format(DTF) : "";
            JLabel sub = new JLabel("Khởi hành: " + dep + "    •    Ghế: " + b.getSeatNumber()
                    + "    •    Mã: " + b.getBookingCode());
            sub.setFont(UIConstants.NORMAL_FONT);
            sub.setForeground(new Color(80, 80, 80));
            sub.setBorder(new EmptyBorder(6, 0, 0, 0));

            left.add(route);
            left.add(sub);

            // Countdown
            JPanel cd = new JPanel();
            cd.setLayout(new BoxLayout(cd, BoxLayout.Y_AXIS));
            cd.setBackground(Color.WHITE);
            cd.setBorder(new EmptyBorder(0, 24, 0, 0));
            cd.setAlignmentY(Component.CENTER_ALIGNMENT);

            if (b.getDepartureTime() != null) {
                long days = Duration.between(LocalDateTime.now(), b.getDepartureTime()).toDays();
                long hours = Duration.between(LocalDateTime.now(), b.getDepartureTime()).toHours();
                String big, small;
                if (days >= 1) { big = days + " ngày"; small = "còn lại"; }
                else if (hours >= 1) { big = hours + " giờ"; small = "còn lại"; }
                else { big = "Sắp khởi hành"; small = "trong hôm nay"; }

                JLabel lblBig = new JLabel(big);
                lblBig.setFont(new Font(UIConstants.TITLE_FONT.getFontName(), Font.BOLD, 22));
                lblBig.setForeground(new Color(33, 150, 243));
                lblBig.setAlignmentX(Component.RIGHT_ALIGNMENT);
                JLabel lblSmall = new JLabel(small);
                lblSmall.setFont(UIConstants.SMALL_FONT);
                lblSmall.setForeground(Color.GRAY);
                lblSmall.setAlignmentX(Component.RIGHT_ALIGNMENT);
                cd.add(lblBig);
                cd.add(lblSmall);
            }

            nextFlightCard.add(left, BorderLayout.WEST);
            nextFlightCard.add(cd, BorderLayout.EAST);
        }
        nextFlightCard.revalidate();
        nextFlightCard.repaint();
    }

    // ── Suggestions section ─────────────────────────────────────────────────
    private JPanel buildSuggestionsSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(UIConstants.BG_COLOR);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel header = new JLabel("🌟 Gợi ý chuyến bay phổ biến");
        header.setFont(UIConstants.HEADER_FONT);
        header.setForeground(UIConstants.PRIMARY_DARK);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));

        JButton btnAll = new JButton("Xem tất cả →");
        btnAll.setFont(UIConstants.SMALL_FONT);
        btnAll.setBorder(BorderFactory.createEmptyBorder());
        btnAll.setForeground(UIConstants.PRIMARY_COLOR);
        btnAll.setBackground(UIConstants.BG_COLOR);
        btnAll.setFocusPainted(false);
        btnAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAll.addActionListener(e -> { if (navigator != null) navigator.accept("search"); });

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(UIConstants.BG_COLOR);
        headerRow.add(header, BorderLayout.WEST);
        headerRow.add(btnAll, BorderLayout.EAST);

        suggestionsPanel = new JPanel();
        suggestionsPanel.setLayout(new GridLayout(0, 3, 14, 14));
        suggestionsPanel.setBackground(UIConstants.BG_COLOR);

        section.add(headerRow, BorderLayout.NORTH);
        section.add(suggestionsPanel, BorderLayout.CENTER);
        return section;
    }

    private JPanel buildSuggestionCard(Flight f) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel route = new JLabel(f.getDepartureAirport() + "  →  " + f.getArrivalAirport());
        route.setFont(new Font(UIConstants.HEADER_FONT.getFontName(), Font.BOLD, 14));
        route.setForeground(UIConstants.PRIMARY_DARK);

        JLabel airline = new JLabel(f.getAirline() + "  •  " + f.getFlightCode());
        airline.setFont(UIConstants.SMALL_FONT);
        airline.setForeground(new Color(110, 110, 110));

        String dep = f.getDepartureTime() != null ? f.getDepartureTime().format(DTF) : "";
        JLabel time = new JLabel(dep);
        time.setFont(UIConstants.SMALL_FONT);
        time.setForeground(new Color(80, 80, 80));

        JLabel price = new JLabel(f.getPrice() != null ? String.format("%,.0f VNĐ", f.getPrice()) : "");
        price.setFont(new Font(UIConstants.HEADER_FONT.getFontName(), Font.BOLD, 16));
        price.setForeground(UIConstants.SECONDARY_COLOR);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(Color.WHITE);
        info.add(route);
        info.add(Box.createVerticalStrut(4));
        info.add(airline);
        info.add(Box.createVerticalStrut(2));
        info.add(time);
        info.add(Box.createVerticalStrut(8));
        info.add(price);

        JButton btn = new JButton("Đặt ngay");
        btn.setFont(UIConstants.BUTTON_FONT);
        btn.setBackground(UIConstants.PRIMARY_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> openBookingDialog());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        south.setBackground(Color.WHITE);
        south.add(btn);

        card.add(info, BorderLayout.CENTER);
        card.add(south, BorderLayout.SOUTH);
        return card;
    }

    private void openBookingDialog() {
        BookingDialog dlg = new BookingDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) loadData();
    }

    // ── Data load ───────────────────────────────────────────────────────────
    private void loadData() {
        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            protected Object[] doInBackground() throws Exception {
                List<Booking> myBookings = bookingService.getAll(); // đã filter theo user
                List<Flight> allFlights = flightService.getAll();
                return new Object[]{myBookings, allFlights};
            }
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] r = get();
                    List<Booking> myBookings = (List<Booking>) r[0];
                    List<Flight> allFlights = (List<Flight>) r[1];

                    fillStats(myBookings);
                    fillNextFlightCard(findNextFlight(myBookings));
                    fillSuggestions(allFlights);
                } catch (Exception ex) {
                    /* ignore – panel rỗng */
                }
            }
        };
        worker.execute();
    }

    private void fillStats(List<Booking> bookings) {
        LocalDateTime now = LocalDateTime.now();
        int upcoming = 0;
        int total = 0;
        BigDecimal spentThisYear = BigDecimal.ZERO;
        int thisYear = LocalDate.now().getYear();

        for (Booking b : bookings) {
            if (b.getStatus() == BookingStatus.CANCELLED) continue;
            total++;
            if (b.getDepartureTime() != null && b.getDepartureTime().isAfter(now)
                    && (b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.PENDING)) {
                upcoming++;
            }
            if (b.getBookingDate() != null && b.getBookingDate().getYear() == thisYear
                    && b.getTicketPrice() != null) {
                spentThisYear = spentThisYear.add(b.getTicketPrice());
            }
        }
        lblUpcoming.setText(String.valueOf(upcoming));
        lblTotalBookings.setText(String.valueOf(total));
        lblTotalSpent.setText(String.format("%,.0f đ", spentThisYear));
    }

    private Booking findNextFlight(List<Booking> bookings) {
        LocalDateTime now = LocalDateTime.now();
        return bookings.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .filter(b -> b.getDepartureTime() != null && b.getDepartureTime().isAfter(now))
                .min(Comparator.comparing(Booking::getDepartureTime))
                .orElse(null);
    }

    private void fillSuggestions(List<Flight> all) {
        suggestionsPanel.removeAll();
        LocalDateTime now = LocalDateTime.now();
        List<Flight> picks = all.stream()
                .filter(f -> f.getStatus() == FlightStatus.SCHEDULED)
                .filter(f -> f.getDepartureTime() != null && f.getDepartureTime().isAfter(now))
                .filter(f -> f.getAvailableSeats() > 0)
                .sorted(Comparator.comparing(Flight::getDepartureTime))
                .limit(6)
                .collect(java.util.stream.Collectors.toList());

        if (picks.isEmpty()) {
            JLabel empty = new JLabel("Chưa có chuyến bay phù hợp.");
            empty.setFont(UIConstants.NORMAL_FONT);
            empty.setForeground(Color.GRAY);
            suggestionsPanel.add(empty);
        } else {
            for (Flight f : picks) suggestionsPanel.add(buildSuggestionCard(f));
        }
        suggestionsPanel.revalidate();
        suggestionsPanel.repaint();
    }
}
