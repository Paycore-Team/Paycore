package paycore.paycore.service.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import paycore.paycore.usecase.model.MockServiceRequest;
import paycore.paycore.usecase.model.MockServiceResponse;
import paycore.paycore.usecase.port.PaymentGatewayPort;

@Service
@RequiredArgsConstructor
public class PGAdapter implements PaymentGatewayPort {
    private final MockService mockService;

    @Override
    public MockServiceResponse pay(MockServiceRequest input) {
        return mockService.execute(input);
    }

    @Override
    public MockServiceResponse refund(MockServiceRequest input) {
        // pay와 refund가 역연산 관계이므로 pay만 우선 구현
        return null;
    }
}
