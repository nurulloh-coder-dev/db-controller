package com.example.events;

import org.springframework.context.ApplicationEvent;

public final class SendEmailToAuthUser extends ApplicationEvent {
    public final String userEmail;
    public final String text;
    public final String subject;

    public SendEmailToAuthUser(Object source, String userEmail, String text, String subject) {
        super(source);
        this.userEmail = userEmail;
        this.text = text;
        this.subject = subject;
    }
}
