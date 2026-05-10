package com.example.service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final Resend resend;

    @Value("${resend.from-email}")
    private String fromEmail;

    public EmailService(@Value("${resend.api-key}") String apiKey) {
        this.resend = new Resend(apiKey);
    }

    @Async
    @SneakyThrows
    public void sendEmailAboutCreationAcc(String userEmail, String text, String subject) {
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(userEmail)
                .subject(subject)
                .text(text)
                .build();
        resend.emails().send(params);
    }

    @Async
    @SneakyThrows
    public void sendEmailAboutUpdate(String userEmail, String text, Boolean sendEmail) {
        if (sendEmail) {
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(userEmail)
                    .subject("Update to your profile")
                    .text(text)
                    .build();
            resend.emails().send(params);
            log.info("Email sent to: {}", userEmail);
        }
    }

    @Async
    @SneakyThrows
    public void sendResetLink(String email, String rawToken) {
        String htmlBody = """
                <p>Click the link below to reset your password:</p>
                <a href="https://coder12345-web.github.io/db-controller-front/login/reset-password.html?token=%s">Reset Password</a>
                <p>This link expires in 15 minutes.</p>
                <p>If you did not request this, ignore this email.</p>
                """.formatted(rawToken);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(email)
                .subject("Password Reset Request")
                .html(htmlBody)
                .build();
        resend.emails().send(params);
    }
}