package paycore.paycore.dto;

import paycore.paycore.domain.EventType;
import paycore.paycore.domain.OutboxStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentMessageHandlerRequest(
        UUID sagaId,
        UUID paymentId,
        OutboxStatus status,
        EventType eventType,
        String apiKey,
        BigDecimal amount,
        long createdAt
) {
}