package com.iridian.movie.social.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class EmailTestService {

    private final JavaMailSender mailSender;

    public EmailTestService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTestEmail(String to) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setFrom(System.getenv("SPRING_MAIL_USERNAME"));
        msg.setSubject("Test Email");
        msg.setText("This is a test email from your app.");
        mailSender.send(msg);
        System.out.println("✅ Test email sent to " + to);
    }

 
}
