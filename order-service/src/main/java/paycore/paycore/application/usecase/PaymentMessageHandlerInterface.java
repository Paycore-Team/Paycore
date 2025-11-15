package paycore.paycore.application.usecase;

import paycore.paycore.dto.PaymentMessageHandlerRequest;

public interface PaymentMessageHandlerInterface {
    void handle(PaymentMessageHandlerRequest input);
}
