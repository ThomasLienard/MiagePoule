package com.miage.pouleAPI.services.interfaces;

public interface MaillingService {

    void sendEmail(String to, String subject, String body);
    
}
