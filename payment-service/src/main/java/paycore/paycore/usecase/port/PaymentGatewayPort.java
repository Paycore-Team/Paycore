package paycore.paycore.usecase.port;

import paycore.paycore.usecase.model.MockServiceRequest;
import paycore.paycore.usecase.model.MockServiceResponse;

public interface PaymentGatewayPort {
    MockServiceResponse pay(MockServiceRequest input);

    MockServiceResponse refund(MockServiceRequest input);
}
