package com.quanlydatvemaybay.config;

/**
 * Cấu hình gửi email qua Gmail SMTP.
 *
 * Hướng dẫn:
 *  1. Đặt USERNAME = địa chỉ Gmail của bạn
 *  2. Đặt PASSWORD  = "App Password" (16 ký tự) — KHÔNG dùng mật khẩu Gmail thường
 *     Tạo App Password tại: Google Account → Security → 2-Step Verification → App passwords
 *  3. Đặt FROM_EMAIL = USERNAME (hoặc alias nếu có)
 */
public class EmailConfig {

    public static final String SMTP_HOST = "smtp.gmail.com";
    public static final String SMTP_PORT = "587";

    /** Địa chỉ Gmail dùng để gửi */
    public static final String USERNAME  = "nqhung.dev2511@gmail.com";

    /** App Password 16 ký tự từ Google Account (không phải mật khẩu Gmail) */
    public static final String PASSWORD  = "qakt llof wrqm zewy";

    /** Tên hiển thị của người gửi */
    public static final String FROM_EMAIL = USERNAME;

    private EmailConfig() {}
}
