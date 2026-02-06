package com.example.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
    public void sendEmailAboutUpdate(String userEmail, String text,Boolean sendEmail) {
        if (sendEmail) {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(userEmail);
            mailMessage.setSubject("Update to your profile");
            mailMessage.setText(text);
            mailSender.send(mailMessage);
            log.info("Email is sent successfully to email : {} on update to db user", userEmail);
        }
    }
}
