package paycore.paycore.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderRequestDto(
        Long userId,
        UUID idempotencyKey,
        String apiKey,
        String productDesc,
        BigDecimal amount,
        long amountTaxFree
) {
}
