package paycore.paycore.usecase.model;

import paycore.paycore.domain.EventType;
import paycore.paycore.domain.OutboxStatus;

import java.util.UUID;

public record SettlementMessageHandlerServiceRequest(
        UUID settlementId,
        UUID sagaId,
        EventType eventType,
        OutboxStatus status,
        String payload
) {
}
