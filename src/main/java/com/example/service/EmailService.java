package com.example.service;

import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Async
    public void sendEmailAboutCreationAcc(String userEmail, String text, String subject) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(userEmail);
        mailMessage.setSubject(subject);
        mailMessage.setText(text);
        mailSender.send(mailMessage);
    }

    @Async
    public void sendEmailAboutUpdate(String userEmail, String text, Boolean sendEmail) {
        if (sendEmail) {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(userEmail);
            mailMessage.setSubject("Update to your profile");
            mailMessage.setText(text);
            mailSender.send(mailMessage);
            log.info("Email is sent successfully to email : {} on update to db user", userEmail);
        }
    }

    @SneakyThrows
    @Async
    public void sendResetLink(String email, String rawToken) {
        String htmlBody = """
                    <p>Click the link below to reset your password:</p>
                    <a href="https://yourapp.com/reset-password?token=%s">Reset Password</a>
                    <p>This link expires in 15 minutes.</p>
                    <p>If you did not request this, ignore this email.</p>
                """.formatted(rawToken);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("Password Reset Request");
        helper.setText(htmlBody, true);
    }
}
