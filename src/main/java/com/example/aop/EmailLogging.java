package com.example.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import com.example.service.FileService;

/**
 * This class is designed to aid admin to control the login section of the project
 * variable text is the text that will be sent to user
 * it is then cut down to show the necessary part to admin
 */

@Aspect
@Component
@Slf4j
public class EmailLogging {

    private final FileService fileService;

    public EmailLogging(FileService fileService) {
        this.fileService = fileService;
    }

    @After(
            value = "execution(* com.example.service.EmailService.sendEmailAboutCreationAcc(..)) && args(userEmail, text,subject)",
            argNames = "userEmail,text,subject"
    )
    public void logOnUserCreationSuccess(String userEmail, String text, String subject) {
        String adminData = text.substring(text.indexOf(": ") + 2, text.indexOf(" for login."));
        fileService.writeUserRegister(adminData);
        log.info("email is sent to user {}: {}", userEmail, text);
    }


    /**
     * these emails are sent when a user's database activity happens (role change, e.g.)
     */


    @AfterReturning(
            value = "execution(* com.example.service.EmailService.*(..)) && args(userEmail,text,sendEmail))",
            argNames = "userEmail,text,sendEmail"
    )
    public void logOnDatabaseUpdateSuccess(String userEmail, String text, Boolean sendEmail) {
        log.info("Email is sent to user {} about database action : {}", userEmail, text);
    }
}
