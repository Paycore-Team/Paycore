package paycore.paycore.service;

import org.springframework.stereotype.Service;
import paycore.paycore.common.UseCase;
import paycore.paycore.dto.MockServiceRequestDto;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService implements UseCase<PaymentService.Input, Void> {
    private final MockService mockService;
    private final PaymentPersistenceService paymentPersistenceService;

    public PaymentService(
            MockService mockService,
            PaymentPersistenceService paymentPersistenceService) {
        this.mockService = mockService;
        this.paymentPersistenceService = paymentPersistenceService;
    }

    public record Input(
            UUID sagaId,
            String apiKey,
            String orderNo,
            String productDesc,
            String retUrl,
            String retCancelUrl,
            long amount,
            long amountTaxFree
    ) {
    }

    @Override
    public Void execute(Input input) {
        MockServiceRequestDto newMockServiceRequestDto = new MockServiceRequestDto(
                input.apiKey,
                input.orderNo,
                input.productDesc,
                input.retUrl,
                input.retCancelUrl,
                input.amount,
                input.amountTaxFree
        );
        MockService.Output result = mockService.execute(
                new MockService.Input(newMockServiceRequestDto)
        );

        paymentPersistenceService.execute(
                new PaymentPersistenceService.Input(
                        input.sagaId,
                        result.httpResponse().statusCode(),
                        result.httpResponse().body(),
                        new BigDecimal("10.00"),
                        "test_store"
                )
        );

        return null;
    }
}
