package paycore.paycore.usecase.model;

import paycore.paycore.domain.OutboxStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentServiceRequest(
        Long orderId,
        UUID sagaId,
        String eventType,
        OutboxStatus status,
        String apiKey,
        String productDesc,
        BigDecimal amount,
        long amountTaxFree
) {
}
