package paycore.paycore.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SettlementRequestDto(
        UUID sagaId,
        UUID paymentId,
        UUID orderId,
        BigDecimal amount,
        BigDecimal fee,
        String settlementAccount
) {
}

