package paycore.paycore.usecase.model;

import java.math.BigDecimal;

public record MockServiceRequest(
        String apiKey,
        Long orderNo,
        String productDesc,
        String retUrl,
        String retCancelUrl,
        BigDecimal amount,
        long amountTaxFree
) {
}