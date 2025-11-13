package paycore.paycore.dto;

import paycore.paycore.domain.EventType;
import paycore.paycore.domain.OutboxStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentOutboxRecord(
        UUID sagaId,
        UUID paymentId,
        OutboxStatus status,
        EventType eventType,
        String apiKey,
        BigDecimal amount,
        long createdAt // LocalDateTime 이 Long(타임스탬프) 형태로 전달되기 때문에 변환하여 처리
) {
}
