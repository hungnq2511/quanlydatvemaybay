package com.quanlydatvemaybay.ui.dialogs;

import com.quanlydatvemaybay.entity.Ticket;
import com.quanlydatvemaybay.enums.TicketClass;
import com.quanlydatvemaybay.enums.TicketStatus;
import com.quanlydatvemaybay.ui.UIConstants;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Sơ đồ chọn ghế trực quan.
 *  - Hiển thị các ghế theo lưới (6 ghế / hàng, có lối đi giữa).
 *  - Phân biệt màu: trống / đã đặt / đang chọn.
 *  - Phân nhóm theo hạng vé (Hạng nhất → Thương gia → Phổ thông).
 */
public class SeatMapPanel extends JPanel {

    private static final int SEATS_PER_ROW = 6;
    private static final Color COLOR_AVAILABLE = new Color(76, 175, 80);
    private static final Color COLOR_BOOKED = new Color(189, 189, 189);
    private static final Color COLOR_SELECTED = new Color(33, 150, 243);
    private static final Color COLOR_FIRST = new Color(255, 193, 7);
    private static final Color COLOR_BUSINESS = new Color(156, 39, 176);

    private List<Ticket> tickets = new ArrayList<>();
    private Ticket selectedTicket;
    private final Map<Long, JButton> seatButtons = new LinkedHashMap<>();
    private final JPanel gridPanel;
    private final JLabel lblInfo;
    private Consumer<Ticket> onSelection;

    public SeatMapPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        gridPanel = new JPanel();
        gridPanel.setBackground(Color.WHITE);
        gridPanel.setLayout(new BoxLayout(gridPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(gridPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        lblInfo = new JLabel(" ");
        lblInfo.setFont(UIConstants.SMALL_FONT);
        lblInfo.setForeground(new Color(60, 60, 60));
        lblInfo.setBorder(new EmptyBorder(6, 4, 4, 4));

        add(buildLegend(), BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(lblInfo, BorderLayout.SOUTH);
    }

    public void setOnSelection(Consumer<Ticket> handler) {
        this.onSelection = handler;
    }

    public Ticket getSelectedTicket() { return selectedTicket; }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets != null ? tickets : Collections.emptyList();
        this.selectedTicket = null;
        rebuild();
    }

    private void rebuild() {
        gridPanel.removeAll();
        seatButtons.clear();

        if (tickets.isEmpty()) {
            JLabel empty = new JLabel("Chưa có sơ đồ ghế cho chuyến bay này.", SwingConstants.CENTER);
            empty.setFont(UIConstants.NORMAL_FONT);
            empty.setForeground(Color.GRAY);
            empty.setBorder(new EmptyBorder(20, 0, 20, 0));
            gridPanel.add(empty);
            updateInfo();
            gridPanel.revalidate();
            gridPanel.repaint();
            return;
        }

        // Nhóm theo hạng vé
        Map<TicketClass, List<Ticket>> byClass = new LinkedHashMap<>();
        for (TicketClass c : new TicketClass[]{TicketClass.FIRST_CLASS, TicketClass.BUSINESS, TicketClass.ECONOMY}) {
            byClass.put(c, new ArrayList<>());
        }
        for (Ticket t : tickets) {
            TicketClass c = t.getTicketClass() != null ? t.getTicketClass() : TicketClass.ECONOMY;
            byClass.computeIfAbsent(c, k -> new ArrayList<>()).add(t);
        }
        // Sort mỗi nhóm theo seat number
        for (List<Ticket> list : byClass.values()) {
            list.sort(Comparator.comparing(t -> t.getSeatNumber() == null ? "" : t.getSeatNumber()));
        }

        for (Map.Entry<TicketClass, List<Ticket>> e : byClass.entrySet()) {
            if (e.getValue().isEmpty()) continue;
            gridPanel.add(buildClassSection(e.getKey(), e.getValue()));
            gridPanel.add(Box.createVerticalStrut(10));
        }

        updateInfo();
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel buildClassSection(TicketClass cls, List<Ticket> list) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(Color.WHITE);
        section.setBorder(new EmptyBorder(8, 4, 4, 4));

        Color headerColor;
        if (cls == TicketClass.FIRST_CLASS) headerColor = COLOR_FIRST;
        else if (cls == TicketClass.BUSINESS) headerColor = COLOR_BUSINESS;
        else headerColor = new Color(96, 125, 139);

        JLabel header = new JLabel("  " + cls.getDisplayName() + "  ");
        header.setFont(UIConstants.NORMAL_FONT.deriveFont(Font.BOLD));
        header.setOpaque(true);
        header.setBackground(headerColor);
        header.setForeground(Color.WHITE);
        header.setBorder(new EmptyBorder(4, 8, 4, 8));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(header);
        section.add(Box.createVerticalStrut(6));

        int row = 0;
        for (int i = 0; i < list.size(); i += SEATS_PER_ROW) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
            rowPanel.setBackground(Color.WHITE);
            rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel rowLabel = new JLabel(String.format("%2d ", ++row));
            rowLabel.setFont(UIConstants.SMALL_FONT);
            rowLabel.setForeground(Color.GRAY);
            rowLabel.setPreferredSize(new Dimension(24, 28));
            rowPanel.add(rowLabel);

            for (int j = 0; j < SEATS_PER_ROW; j++) {
                int idx = i + j;
                if (idx >= list.size()) break;
                Ticket t = list.get(idx);
                rowPanel.add(buildSeatButton(t));
                if (j == 2) {
                    // Lối đi giữa hàng (sau ghế thứ 3)
                    rowPanel.add(Box.createHorizontalStrut(20));
                }
            }
            section.add(rowPanel);
        }
        return section;
    }

    private JButton buildSeatButton(Ticket t) {
        JButton btn = new JButton(t.getSeatNumber());
        btn.setFont(UIConstants.SMALL_FONT);
        btn.setPreferredSize(new Dimension(58, 36));
        btn.setMargin(new Insets(2, 2, 2, 2));
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setOpaque(true);
        btn.setForeground(Color.WHITE);

        boolean booked = t.getStatus() != TicketStatus.AVAILABLE;
        if (booked) {
            btn.setBackground(COLOR_BOOKED);
            btn.setEnabled(false);
            btn.setToolTipText("Đã đặt");
        } else {
            btn.setBackground(COLOR_AVAILABLE);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setToolTipText(String.format("<html>Ghế <b>%s</b><br>Hạng: %s<br>Giá: %,.0f VNĐ</html>",
                    t.getSeatNumber(),
                    t.getTicketClass() != null ? t.getTicketClass().getDisplayName() : "",
                    t.getPrice()));
            btn.addActionListener(e -> select(t));
        }
        seatButtons.put(t.getId(), btn);
        return btn;
    }

    private void select(Ticket t) {
        // Reset selection trước đó
        if (selectedTicket != null) {
            JButton prev = seatButtons.get(selectedTicket.getId());
            if (prev != null) prev.setBackground(COLOR_AVAILABLE);
        }
        this.selectedTicket = t;
        JButton b = seatButtons.get(t.getId());
        if (b != null) b.setBackground(COLOR_SELECTED);
        updateInfo();
        if (onSelection != null) onSelection.accept(t);
    }

    private void updateInfo() {
        if (selectedTicket != null) {
            lblInfo.setText(String.format("Đã chọn: Ghế %s  |  %s  |  %,.0f VNĐ",
                    selectedTicket.getSeatNumber(),
                    selectedTicket.getTicketClass() != null
                            ? selectedTicket.getTicketClass().getDisplayName() : "",
                    selectedTicket.getPrice()));
            lblInfo.setForeground(COLOR_SELECTED);
        } else {
            long avail = tickets.stream().filter(t -> t.getStatus() == TicketStatus.AVAILABLE).count();
            lblInfo.setText("Chưa chọn ghế. Còn trống: " + avail + " / " + tickets.size());
            lblInfo.setForeground(new Color(60, 60, 60));
        }
    }

    private JPanel buildLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        legend.setBackground(Color.WHITE);
        legend.setBorder(new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        legend.add(legendItem("Trống", COLOR_AVAILABLE));
        legend.add(legendItem("Đang chọn", COLOR_SELECTED));
        legend.add(legendItem("Đã đặt", COLOR_BOOKED));
        return legend;
    }

    private JPanel legendItem(String text, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setBackground(Color.WHITE);
        JPanel swatch = new JPanel();
        swatch.setBackground(color);
        swatch.setPreferredSize(new Dimension(16, 16));
        swatch.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.SMALL_FONT);
        p.add(swatch);
        p.add(lbl);
        return p;
    }
}
