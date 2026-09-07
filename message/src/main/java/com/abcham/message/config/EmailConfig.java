package com.abcham.message.config;

import com.abcham.message.service.EmailSender;
import com.abcham.message.service.ResendEmailSender;
import com.resend.Resend;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {

    @Bean
    public EmailSender emailSender(
            @Value("${resend.api-key:}") String apiKey,
            @Value("${resend.from}") String from) {
        if (apiKey == null || apiKey.isBlank()) {
            return (to, subject, htmlBody) -> {
                throw new IllegalStateException(
                        "RESEND_API_KEY is not configured; cannot send email to " + to);
            };
        }
        return new ResendEmailSender(new Resend(apiKey), from);
    }
}
