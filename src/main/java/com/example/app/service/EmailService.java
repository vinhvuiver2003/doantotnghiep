package com.example.app.service;

import java.util.Map;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
    void sendHtmlEmail(String to, String subject, String htmlBody);
    void sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables);
    void sendVerificationEmail(String to, String userName, String verificationLink);
    void sendPasswordResetEmail(String to, String userName, String resetLink);
    void sendOrderConfirmationEmail(String to, Map<String, Object> orderDetails);
    void sendOrderStatusUpdateEmail(String to, Map<String, Object> orderStatusDetails);
}