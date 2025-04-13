package com.example.app.service.impl;

import com.example.app.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final FreeMarkerConfigurer freemarkerConfigurer;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender, FreeMarkerConfigurer freemarkerConfigurer) {
        this.mailSender = mailSender;
        this.freemarkerConfigurer = freemarkerConfigurer;
    }

    @Async
    @Override
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    @Async
    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi emails HTML", e);
        }
    }

    @Async
    @Override
    public void sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            String templateContent = FreeMarkerTemplateUtils.processTemplateIntoString(
                    freemarkerConfigurer.getConfiguration().getTemplate("emails/" + templateName + ".html"),
                    variables
            );

            sendHtmlEmail(to, subject, templateContent);
        } catch (Exception e) {
            throw new RuntimeException("Không thể gửi emails từ template: " + templateName, e);
        }
    }

    @Async
    @Override
    public void sendVerificationEmail(String to, String userName, String verificationLink) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", userName);
        variables.put("verificationLink", verificationLink);

        sendTemplateEmail(to, "Xác Thực Tài Khoản", "verification", variables);
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String to, String userName, String resetLink) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", userName);
        variables.put("resetLink", resetLink);

        sendTemplateEmail(to, "Đặt Lại Mật Khẩu", "reset-password", variables);
    }

    @Async
    @Override
    public void sendOrderConfirmationEmail(String to, Map<String, Object> orderDetails) {
        sendTemplateEmail(to, "Xác Nhận Đơn Hàng #" + orderDetails.get("orderId"), "order-confirmation", orderDetails);
    }

    @Async
    @Override
    public void sendOrderStatusUpdateEmail(String to, Map<String, Object> orderStatusDetails) {
        sendTemplateEmail(to, "Cập Nhật Trạng Thái Đơn Hàng #" + orderStatusDetails.get("orderId"), "order-status-update", orderStatusDetails);
    }
}