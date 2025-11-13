package paycore.paycore.usecase.model;

import paycore.paycore.domain.EventType;
import paycore.paycore.domain.OutboxStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record WorkerRequest(
        UUID sagaId,
        UUID paymentId,
        OutboxStatus status,
        EventType eventType,
        String apiKey,
        BigDecimal amount,
        long createdAt
) {
}