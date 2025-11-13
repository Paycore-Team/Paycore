package paycore.paycore.usecase.model;

import paycore.paycore.domain.EventType;
import paycore.paycore.domain.OutboxStatus;

import java.util.UUID;

public record WorkerRequest(
        UUID sagaId,
        UUID paymentId,
        OutboxStatus status,
        EventType eventType,
        String payload,
        long createdAt
) {
}