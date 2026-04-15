package com.quanlydatvemaybay.ui.panels;

import com.quanlydatvemaybay.dao.BookingDAO;
import com.quanlydatvemaybay.dao.FlightDAO;
import com.quanlydatvemaybay.dao.TicketDAO;
import com.quanlydatvemaybay.enums.BookingStatus;
import com.quanlydatvemaybay.enums.FlightStatus;
import com.quanlydatvemaybay.enums.TicketStatus;
import com.quanlydatvemaybay.ui.UIConstants;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class DashboardPanel extends JPanel {

    private JPanel cardsPanel;

    public DashboardPanel() {
        setBackground(UIConstants.BG_COLOR);
        setLayout(new BorderLayout());
        initUI();
        loadCards();
    }

    public void refresh() {
        loadCards();
    }

    private void initUI() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        topBar.setPreferredSize(new Dimension(0, 60));

        JLabel titleLabel = new JLabel("   Tổng Quan Hệ Thống");
        titleLabel.setFont(UIConstants.TITLE_FONT);
        titleLabel.setForeground(UIConstants.PRIMARY_DARK);
        topBar.add(titleLabel, BorderLayout.WEST);

        cardsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        cardsPanel.setBackground(UIConstants.BG_COLOR);
        cardsPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        add(topBar, BorderLayout.NORTH);
        add(cardsPanel, BorderLayout.CENTER);
    }

    private void loadCards() {
        cardsPanel.removeAll();
        try {
            FlightDAO flightDAO = new FlightDAO();
            TicketDAO ticketDAO = new TicketDAO();
            BookingDAO bookingDAO = new BookingDAO();

            long totalFlights = flightDAO.findAll().size();
            long scheduledFlights = flightDAO.search(null, null, null).stream()
                    .filter(f -> f.getStatus() == FlightStatus.SCHEDULED).count();
            long totalTickets = ticketDAO.findAll().size();
            long availableTickets = ticketDAO.findByStatus(TicketStatus.AVAILABLE).size();
            long totalBookings = bookingDAO.findAll().size();
            long pendingBookings = bookingDAO.search(null, BookingStatus.PENDING, null).size();
            long confirmedBookings = bookingDAO.search(null, BookingStatus.CONFIRMED, null).size();

            cardsPanel.add(createCard("Chuyến Bay", String.valueOf(totalFlights),
                    "Đã lên lịch: " + scheduledFlights, UIConstants.PRIMARY_COLOR, "✈"));
            cardsPanel.add(createCard("Vé Máy Bay", String.valueOf(totalTickets),
                    "Còn trống: " + availableTickets, UIConstants.SECONDARY_COLOR, "🎫"));
            cardsPanel.add(createCard("Đặt Vé", String.valueOf(totalBookings),
                    "Chờ xác nhận: " + pendingBookings, UIConstants.WARNING_COLOR, "📋"));
            cardsPanel.add(createCard("Đã Xác Nhận", String.valueOf(confirmedBookings),
                    "Tổng đặt vé: " + totalBookings, new Color(142, 68, 173), "✅"));

        } catch (Exception e) {
            cardsPanel.add(createCard("Lỗi kết nối", "N/A", e.getMessage(), UIConstants.DANGER_COLOR, "⚠"));
            cardsPanel.add(createCard("Database", "N/A", "Kiểm tra kết nối Oracle", UIConstants.DANGER_COLOR, "⚠"));
            cardsPanel.add(createCard("Dữ liệu", "N/A", "Không thể tải", UIConstants.DANGER_COLOR, "⚠"));
            cardsPanel.add(createCard("Thống kê", "N/A", "Không thể tải", UIConstants.DANGER_COLOR, "⚠"));
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private JPanel createCard(String title, String value, String subtitle, Color color, String icon) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(20, 25, 20, 25)));

        JPanel leftPanel = new JPanel(new GridLayout(3, 1, 0, 4));
        leftPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIConstants.NORMAL_FONT);
        titleLabel.setForeground(new Color(100, 100, 100));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(color);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(UIConstants.SMALL_FONT);
        subtitleLabel.setForeground(new Color(130, 130, 130));

        leftPanel.add(titleLabel);
        leftPanel.add(valueLabel);
        leftPanel.add(subtitleLabel);

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconLabel.setPreferredSize(new Dimension(80, 80));

        card.add(leftPanel, BorderLayout.CENTER);
        card.add(iconLabel, BorderLayout.EAST);

        return card;
    }
}
