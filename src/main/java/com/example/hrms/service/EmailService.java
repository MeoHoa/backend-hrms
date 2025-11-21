package com.example.hrms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.frontend-url}")
    private String frontendUrl;

    public void sendAccountCreationEmail(String toEmail, String fullName, String username, String password) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Tài khoản HRMS của bạn đã được tạo");
            
            String body = String.format(
                "Xin chào %s,\n\n" +
                "Tài khoản HRMS của bạn đã được tạo thành công.\n\n" +
                "Thông tin đăng nhập:\n" +
                "- Username: %s\n" +
                "- Email: %s\n" +
                "- Mật khẩu tạm thời: %s\n\n" +
                "Lưu ý: Vui lòng đăng nhập và đổi mật khẩu ngay lần đầu tiên để bảo mật tài khoản.\n\n" +
                "Trân trọng,\n" +
                "HRMS Team",
                fullName, username, toEmail, password
            );
            
            message.setText(body);
            mailSender.send(message);
            log.info("Account creation email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Error sending account creation email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send account creation email: " + e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String toEmail, String fullName, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Đặt lại mật khẩu HRMS");
            
            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
            
            String body = String.format(
                "Xin chào %s,\n\n" +
                "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản HRMS của bạn.\n\n" +
                "Vui lòng click vào link sau để đặt lại mật khẩu:\n" +
                "%s\n\n" +
                "Link này sẽ hết hạn sau 1 giờ.\n\n" +
                "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n" +
                "Trân trọng,\n" +
                "HRMS Team",
                fullName, resetLink
            );
            
            message.setText(body);
            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Error sending password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email: " + e.getMessage());
        }
    }
}

