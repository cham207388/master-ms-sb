package com.abcham.message.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ResendEmailSender implements EmailSender {

    private final Resend resend;
    private final String from;

    @Override
    public void send(String to, String subject, String htmlBody) {
        try {
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(from)
                    .to(to)
                    .subject(subject)
                    .html(htmlBody)
                    .build();
            var response = resend.emails().send(params);
            log.info("Resend accepted email to {} with id {}", to, response.getId());
        } catch (ResendException e) {
            throw new IllegalStateException("Failed to send email via Resend: " + e.getMessage(), e);
        }
    }
}
