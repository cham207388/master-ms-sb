package com.abcham.message.dto;

import java.math.BigDecimal;

public record NotificationMsgDto(
        NotificationType type,
        Long accountNumber,
        String name,
        String email,
        String mobileNumber,
        BigDecimal amount,
        BigDecimal balance,
        Long counterpartyAccount
) {
}
