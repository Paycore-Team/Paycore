package paycore.paycore.usecase.model;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentPersistenceServiceRequest(
        UUID sagaId,
        int statusCode,
        String body,
        BigDecimal amount
) {
}
