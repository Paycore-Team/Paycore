package paycore.paycore.usecase;

import paycore.paycore.usecase.model.PaymentPersistenceRequest;

public interface PaymentPersistenceUseCase {
    Void updateStatus(PaymentPersistenceRequest input);
}