package com.example.events;

import org.springframework.context.ApplicationEvent;

public final class SendEmailUpdateDBEvent extends ApplicationEvent {
    public final String userEmail;
    public final String text;
    public final Boolean sendEmail;

    public SendEmailUpdateDBEvent(Object source, String userEmail, String text, Boolean sendEmail) {
        super(source);
        this.userEmail = userEmail;
        this.text = text;
        this.sendEmail = sendEmail;
    }
}
