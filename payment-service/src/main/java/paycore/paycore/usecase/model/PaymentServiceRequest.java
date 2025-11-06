package paycore.paycore.usecase.model;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentServiceRequest(
        UUID sagaId,
        UUID idempotencyKey,
        String apiKey,
        String orderNo,
        String productDesc,
        String retUrl,
        String retCancelUrl,
        BigDecimal amount,
        long amountTaxFree,
        String storeName
) {
}
