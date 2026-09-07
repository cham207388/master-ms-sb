package com.abcham.message.functions;

import com.abcham.message.dto.NotificationMsgDto;
import com.abcham.message.dto.NotificationType;
import com.abcham.message.service.EmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageFunctionsTest {

    @Mock
    private EmailSender emailSender;

    private Function<NotificationMsgDto, NotificationMsgDto> email;
    private Function<NotificationMsgDto, Long> sms;

    @BeforeEach
    void setUp() {
        MessageFunctions functions = new MessageFunctions(emailSender);
        email = functions.email();
        sms = functions.sms();
    }

    @Test
    void email_SendsViaResend() {
        NotificationMsgDto msg = opened();
        assertEquals(msg, email.apply(msg));
        verify(emailSender).send(eq("john@example.com"), anyString(), anyString());
    }

    @Test
    void sms_EmitsAccountNumberOnlyForAccountOpened() {
        assertEquals(1234567890L, sms.apply(opened()));
        assertNull(sms.apply(transfer()));
        assertNull(sms.apply(lowBalance()));
    }

    private static NotificationMsgDto opened() {
        return new NotificationMsgDto(
                NotificationType.ACCOUNT_OPENED,
                1234567890L,
                "John",
                "john@example.com",
                "1234567890",
                null,
                null,
                null);
    }

    private static NotificationMsgDto transfer() {
        return new NotificationMsgDto(
                NotificationType.TRANSFER_COMPLETED,
                1234567890L,
                "John",
                "john@example.com",
                "1234567890",
                new BigDecimal("40.00"),
                new BigDecimal("60.00"),
                9876543210L);
    }

    private static NotificationMsgDto lowBalance() {
        return new NotificationMsgDto(
                NotificationType.LOW_BALANCE,
                1234567890L,
                "John",
                "john@example.com",
                "1234567890",
                null,
                new BigDecimal("90.00"),
                null);
    }
}
