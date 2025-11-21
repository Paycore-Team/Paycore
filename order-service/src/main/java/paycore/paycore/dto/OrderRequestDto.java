package paycore.paycore.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderRequestDto(
        UUID idempotencyKey,
        String apiKey,
        String productDesc,
        BigDecimal amount,
        long amountTaxFree
) {
}
