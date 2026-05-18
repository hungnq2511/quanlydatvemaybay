package com.quanlydatvemaybay.ui;

import com.quanlydatvemaybay.entity.User;
import com.quanlydatvemaybay.service.AuthService;
import com.quanlydatvemaybay.ui.panels.AirlinePanel;
import com.quanlydatvemaybay.ui.panels.AirportPanel;
import com.quanlydatvemaybay.ui.panels.BookingPanel;
import com.quanlydatvemaybay.ui.panels.DashboardPanel;
import com.quanlydatvemaybay.ui.panels.FlightPanel;
import com.quanlydatvemaybay.ui.panels.MyAccountPanel;
import com.quanlydatvemaybay.ui.panels.TicketPanel;
import com.quanlydatvemaybay.ui.panels.UserHomePanel;
import com.quanlydatvemaybay.ui.panels.UserPanel;
import com.quanlydatvemaybay.ui.panels.UserSearchPanel;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JLabel lblCurrentUser;
    private JButton btnActive;
    private DashboardPanel dashboardPanel;
    private TicketPanel ticketPanel;
    private FlightPanel flightPanel;
    private BookingPanel bookingPanel;
    private UserPanel userPanel;
    private AirlinePanel airlinePanel;
    private AirportPanel airportPanel;
    private UserHomePanel userHomePanel;
    private UserSearchPanel userSearchPanel;
    private MyAccountPanel myAccountPanel;

    public MainFrame() {
        setTitle("Quản Lý Đặt Vé Máy Bay");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 760);
        setMinimumSize(new Dimension(1024, 600));
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BG_COLOR);

        // Sidebar
        JPanel sidebar = createSidebar();

        // Content
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIConstants.BG_COLOR);
        User currentUser = AuthService.getCurrentUser();
        boolean isAdmin = currentUser != null && currentUser.isAdmin();

        if (isAdmin) {
            dashboardPanel = new DashboardPanel();
            flightPanel    = new FlightPanel();
            ticketPanel    = new TicketPanel();
            userPanel      = new UserPanel();
            airlinePanel   = new AirlinePanel();
            airportPanel   = new AirportPanel();
            bookingPanel   = new BookingPanel();

            contentPanel.add(dashboardPanel, "dashboard");
            contentPanel.add(flightPanel,    "flights");
            contentPanel.add(ticketPanel,    "tickets");
            contentPanel.add(userPanel,      "users");
            contentPanel.add(airlinePanel,   "airlines");
            contentPanel.add(airportPanel,   "airports");
            contentPanel.add(bookingPanel,   "bookings");
        } else {
            userHomePanel   = new UserHomePanel();
            userSearchPanel = new UserSearchPanel();
            bookingPanel    = new BookingPanel();
            myAccountPanel  = new MyAccountPanel();

            userHomePanel.setNavigator(this::navigateTo);

            contentPanel.add(userHomePanel,   "home");
            contentPanel.add(userSearchPanel, "search");
            contentPanel.add(bookingPanel,    "bookings");
            contentPanel.add(myAccountPanel,  "account");
        }

        root.add(sidebar, BorderLayout.WEST);
        root.add(contentPanel, BorderLayout.CENTER);
        setContentPane(root);

        showPanel(isAdmin ? "dashboard" : "home", null);
    }

    /** Navigate qua tên panel — không highlight nút (vì gọi từ panel khác). */
    private void navigateTo(String name) {
        // Tìm nút có panelName tương ứng để cũng highlight
        for (Component c : findNavButtons()) {
            if (c instanceof JButton && name.equals(c.getName())) {
                showPanel(name, (JButton) c);
                return;
            }
        }
        showPanel(name, null);
    }

    private java.util.List<Component> findNavButtons() {
        java.util.List<Component> list = new java.util.ArrayList<>();
        collectButtons(getContentPane(), list);
        return list;
    }

    private void collectButtons(Container c, java.util.List<Component> out) {
        for (Component child : c.getComponents()) {
            if (child instanceof JButton && child.getName() != null) out.add(child);
            if (child instanceof Container) collectButtons((Container) child, out);
        }
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(UIConstants.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 0));

        // Logo area
        JPanel logoPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        logoPanel.setBackground(UIConstants.PRIMARY_DARK);
        logoPanel.setBorder(new EmptyBorder(20, 15, 20, 15));
        logoPanel.setPreferredSize(new Dimension(220, 90));

        JLabel logoIcon = new JLabel("✈  QuanLyDatVe", SwingConstants.LEFT);
        logoIcon.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logoIcon.setForeground(Color.WHITE);

        User currentUser = AuthService.getCurrentUser();
        lblCurrentUser = new JLabel(currentUser != null ? currentUser.getFullName() : "Người dùng", SwingConstants.LEFT);
        lblCurrentUser.setFont(UIConstants.SMALL_FONT);
        lblCurrentUser.setForeground(new Color(180, 210, 235));

        logoPanel.add(logoIcon);
        logoPanel.add(lblCurrentUser);

        // Navigation
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(UIConstants.SIDEBAR_BG);
        navPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        User user = AuthService.getCurrentUser();
        boolean isAdmin = user != null && user.isAdmin();

        if (isAdmin) {
            addNavItem(navPanel, "  Tổng quan",   "dashboard");
            addNavItem(navPanel, "  Chuyến bay",  "flights");
            addNavItem(navPanel, "  Vé máy bay",  "tickets");
            addNavItem(navPanel, "  Đặt vé",      "bookings");
            addNavItem(navPanel, "  Hãng bay",    "airlines");
            addNavItem(navPanel, "  Địa điểm",    "airports");
            addNavItem(navPanel, "  Người dùng",  "users");
        } else {
            addNavItem(navPanel, "  🏠  Trang chủ",      "home");
            addNavItem(navPanel, "  🔍  Tìm chuyến bay", "search");
            addNavItem(navPanel, "  🎫  Vé của tôi",     "bookings");
            addNavItem(navPanel, "  👤  Tài khoản",      "account");
        }

        // Bottom - logout
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(UIConstants.SIDEBAR_BG);
        bottomPanel.setBorder(new EmptyBorder(10, 10, 20, 10));

        JButton btnLogout = new JButton("  Đăng xuất");
        btnLogout.setFont(UIConstants.NORMAL_FONT);
        btnLogout.setBackground(new Color(192, 57, 43));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setMaximumSize(new Dimension(200, 40));
        btnLogout.setPreferredSize(new Dimension(200, 40));
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn đăng xuất?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                AuthService.logout();
                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        bottomPanel.add(btnLogout, BorderLayout.CENTER);

        sidebar.add(logoPanel, BorderLayout.NORTH);
        sidebar.add(navPanel, BorderLayout.CENTER);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNavItem(JPanel navPanel, String text, String panelName) {
        JButton btn = new JButton(text);
        btn.setName(panelName);
        btn.setFont(UIConstants.NORMAL_FONT);
        btn.setBackground(UIConstants.SIDEBAR_BG);
        btn.setForeground(UIConstants.SIDEBAR_TEXT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(220, 46));
        btn.setPreferredSize(new Dimension(220, 46));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != btnActive) btn.setBackground(UIConstants.SIDEBAR_HOVER);
            }
            public void mouseExited(MouseEvent e) {
                if (btn != btnActive) btn.setBackground(UIConstants.SIDEBAR_BG);
            }
        });

        btn.addActionListener(e -> showPanel(panelName, btn));
        navPanel.add(btn);
    }

    private void showPanel(String name, JButton btn) {
        if (btnActive != null) {
            btnActive.setBackground(UIConstants.SIDEBAR_BG);
            btnActive.setFont(UIConstants.NORMAL_FONT);
        }
        if (btn != null) {
            btn.setBackground(UIConstants.PRIMARY_COLOR);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnActive = btn;
        }
        cardLayout.show(contentPanel, name);
        switch (name) {
            case "dashboard": if (dashboardPanel != null) dashboardPanel.refresh(); break;
            case "flights":   if (flightPanel    != null) flightPanel.refresh();    break;
            case "tickets":   if (ticketPanel    != null) ticketPanel.refresh();    break;
            case "bookings":  if (bookingPanel   != null) bookingPanel.refresh();   break;
            case "users":     if (userPanel      != null) userPanel.refresh();      break;
            case "airlines":  if (airlinePanel   != null) airlinePanel.refresh();   break;
            case "airports":  if (airportPanel   != null) airportPanel.refresh();   break;
            case "home":      if (userHomePanel  != null) userHomePanel.refresh();  break;
            case "search":    if (userSearchPanel!= null) userSearchPanel.refresh();break;
            case "account":   if (myAccountPanel != null) myAccountPanel.refresh(); break;
        }
    }
}
