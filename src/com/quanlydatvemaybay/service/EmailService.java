package com.quanlydatvemaybay.service;

import com.quanlydatvemaybay.config.EmailConfig;
import com.quanlydatvemaybay.entity.Booking;

import javax.mail.*;
import javax.mail.internet.*;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class EmailService {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void sendBookingConfirmation(Booking booking) {
        if (booking.getPassengerEmail() == null || booking.getPassengerEmail().isBlank()) return;

        new Thread(() -> {
            try {
                Session session = createSession();
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EmailConfig.FROM_EMAIL, "Quản Lý Đặt Vé Máy Bay", "UTF-8"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(booking.getPassengerEmail()));
                message.setSubject("=?UTF-8?B?" + java.util.Base64.getEncoder().encodeToString(
                        ("Xác nhận đặt vé - Mã đặt chỗ: " + booking.getBookingCode()).getBytes("UTF-8")) + "?=");
                message.setContent(buildHtmlBody(booking), "text/html; charset=UTF-8");
                Transport.send(message);
                System.out.println("Email xác nhận đã gửi tới: " + booking.getPassengerEmail());
            } catch (Exception e) {
                System.err.println("Gửi email thất bại: " + e.getMessage());
            }
        }).start();
    }

    private Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", EmailConfig.SMTP_HOST);
        props.put("mail.smtp.port", EmailConfig.SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", EmailConfig.SMTP_HOST);

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EmailConfig.USERNAME, EmailConfig.PASSWORD);
            }
        });
    }

    private String buildHtmlBody(Booking booking) {
        String departureTime = booking.getDepartureTime() != null
                ? booking.getDepartureTime().format(DTF) : "N/A";
        String bookingDate = booking.getBookingDate() != null
                ? booking.getBookingDate().format(DTF) : "N/A";
        String price = booking.getTicketPrice() != null
                ? String.format("%,.0f VNĐ", booking.getTicketPrice()) : "N/A";

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='font-family:Arial,sans-serif;background:#f5f5f5;margin:0;padding:20px'>"
                + "<div style='max-width:600px;margin:0 auto;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1)'>"
                + "<div style='background:linear-gradient(135deg,#8e44ad,#6c3483);padding:30px 40px;color:white'>"
                + "<h1 style='margin:0;font-size:24px'>&#9992; Xác nhận đặt vé máy bay</h1>"
                + "<p style='margin:8px 0 0;opacity:0.9'>Cảm ơn bạn đã đặt vé!</p>"
                + "</div>"
                + "<div style='padding:30px 40px'>"
                + "<div style='background:#f8f4ff;border-left:4px solid #8e44ad;padding:16px 20px;border-radius:4px;margin-bottom:24px'>"
                + "<p style='margin:0;font-size:14px;color:#666'>Mã đặt chỗ</p>"
                + "<p style='margin:4px 0 0;font-size:28px;font-weight:bold;color:#8e44ad;letter-spacing:2px'>" + booking.getBookingCode() + "</p>"
                + "</div>"
                + "<h3 style='color:#333;border-bottom:2px solid #eee;padding-bottom:8px'>Thông tin chuyến bay</h3>"
                + "<table style='width:100%;border-collapse:collapse'>"
                + row("Mã chuyến bay", booking.getFlightCode())
                + row("Hành trình", booking.getDepartureAirport() + " &rarr; " + booking.getArrivalAirport())
                + row("Giờ khởi hành", departureTime)
                + row("Ghế ngồi", booking.getSeatNumber())
                + "</table>"
                + "<h3 style='color:#333;border-bottom:2px solid #eee;padding-bottom:8px;margin-top:24px'>Thông tin hành khách</h3>"
                + "<table style='width:100%;border-collapse:collapse'>"
                + row("Họ tên", booking.getPassengerName())
                + row("Email", booking.getPassengerEmail())
                + row("Số điện thoại", nvl(booking.getPassengerPhone()))
                + row("CCCD / Hộ chiếu", nvl(booking.getPassengerIdCard()))
                + "</table>"
                + "<h3 style='color:#333;border-bottom:2px solid #eee;padding-bottom:8px;margin-top:24px'>Thông tin thanh toán</h3>"
                + "<table style='width:100%;border-collapse:collapse'>"
                + row("Ngày đặt vé", bookingDate)
                + row("Giá vé", price)
                + row("Trạng thái", booking.getStatus() != null ? booking.getStatus().getDisplayName() : "N/A")
                + "</table>"
                + "<div style='margin-top:30px;padding:16px;background:#fff8e1;border-radius:6px;font-size:13px;color:#795548'>"
                + "<strong>Lưu ý:</strong> Vui lòng xuất trình mã đặt chỗ khi làm thủ tục lên máy bay. "
                + "Để hủy vé, vui lòng liên hệ với chúng tôi trước giờ khởi hành."
                + "</div>"
                + "</div>"
                + "<div style='background:#f5f5f5;padding:16px 40px;font-size:12px;color:#999;text-align:center'>"
                + "Email này được gửi tự động. Vui lòng không trả lời email này."
                + "</div></div></body></html>";
    }

    private String row(String label, String value) {
        return "<tr>"
                + "<td style='padding:8px 4px;color:#666;width:160px;font-size:14px'>" + label + "</td>"
                + "<td style='padding:8px 4px;font-weight:600;font-size:14px'>" + (value != null ? value : "-") + "</td>"
                + "</tr>";
    }

    private String nvl(String s) {
        return (s != null && !s.isBlank()) ? s : "-";
    }
}
