package paycore.paycore.dto;

public record MockServiceRequestDto(
        String apiKey,
        String orderNo,
        String productDesc,
        String retUrl,
        String retCancelUrl,
        long amount,
        long amountTaxFree
) {
}