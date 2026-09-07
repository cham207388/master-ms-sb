package com.abcham.message.service;

public interface EmailSender {

    void send(String to, String subject, String htmlBody);
}
