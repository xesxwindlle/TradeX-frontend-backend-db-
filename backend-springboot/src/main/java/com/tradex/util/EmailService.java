package com.tradex.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendSignupVerificationEmail(String toEmail, String code) {
        send(toEmail,
            "TradeX - Email Verification",
            "Welcome to TradeX!\n\nYour verification code is: " + code +
            "\n\nThis code will expire once used.\n\n— The TradeX Team");
    }

    public void sendPasswordResetEmail(String toEmail, String code) {
        send(toEmail,
            "TradeX - Password Reset",
            "You requested a password reset.\n\nYour verification code is: " + code +
            "\n\nThis code expires in 30 minutes. If you did not request this, ignore this email.\n\n— The TradeX Team");
    }

    public void sendMobileVerificationEmail(String toEmail, String code, String newMobile) {
        send(toEmail,
            "TradeX - Mobile Number Update",
            "You requested to update your mobile number to: " + newMobile +
            "\n\nYour verification code is: " + code +
            "\n\nThis code expires in 30 minutes.\n\n— The TradeX Team");
    }

    private void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
