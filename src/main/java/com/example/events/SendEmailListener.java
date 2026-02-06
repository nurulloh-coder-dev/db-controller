package com.example.events;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import com.example.service.EmailService;

@Component
@AllArgsConstructor
@Slf4j
public class SendEmailListener {
    private final EmailService emailService;

    @TransactionalEventListener()
    public void sendEmail(SendEmailToAuthUser event) {
        emailService.sendEmailAboutCreationAcc(event.userEmail, event.text,event.subject);
        log.info("Email sent to {}", event.userEmail);
    }

    @TransactionalEventListener()
    public void sendEmailOnChangeDB(SendEmailUpdateDBEvent event) {
        emailService.sendEmailAboutUpdate(event.userEmail, event.text,event.sendEmail);
    }
}
