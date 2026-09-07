package com.abcham.message.functions;

import com.abcham.message.dto.NotificationMsgDto;
import com.abcham.message.dto.NotificationType;
import com.abcham.message.service.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MessageFunctions {

    private final EmailSender emailSender;

    @Bean
    public Function<NotificationMsgDto, NotificationMsgDto> email() {
        return msg -> {
            log.info("Sending {} email to {}", msg.type(), msg.email());
            emailSender.send(msg.email(), subjectFor(msg), bodyFor(msg));
            return msg;
        };
    }

    @Bean
    public Function<NotificationMsgDto, Long> sms() {
        return msg -> {
            if (msg.type() != NotificationType.ACCOUNT_OPENED) {
                log.info("Skipping SMS / communication-sent for type {}", msg.type());
                return null;
            }
            log.info("Sending SMS with the details: {}", msg);
            return msg.accountNumber();
        };
    }

    private static String subjectFor(NotificationMsgDto msg) {
        return switch (msg.type()) {
            case ACCOUNT_OPENED -> "Welcome to Securedbank — account opened";
            case TRANSFER_COMPLETED -> "Securedbank transfer completed";
            case LOW_BALANCE -> "Securedbank low balance alert";
        };
    }

    private static String bodyFor(NotificationMsgDto msg) {
        return switch (msg.type()) {
            case ACCOUNT_OPENED -> """
                    <p>Hello %s,</p>
                    <p>Your Securedbank account <strong>%s</strong> is open.</p>
                    """.formatted(msg.name(), msg.accountNumber());
            case TRANSFER_COMPLETED -> """
                    <p>Hello %s,</p>
                    <p>A transfer of <strong>$%s</strong> completed for account <strong>%s</strong>
                    (counterparty <strong>%s</strong>). New balance: <strong>$%s</strong>.</p>
                    """.formatted(
                    msg.name(),
                    msg.amount(),
                    msg.accountNumber(),
                    msg.counterpartyAccount(),
                    msg.balance());
            case LOW_BALANCE -> """
                    <p>Hello %s,</p>
                    <p>Your account <strong>%s</strong> balance is now <strong>$%s</strong>,
                    below the $100 threshold.</p>
                    """.formatted(msg.name(), msg.accountNumber(), msg.balance());
        };
    }
}
