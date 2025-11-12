package paycore.paycore.usecase;

import paycore.paycore.usecase.model.OutboxPersistenceRequest;

public interface OutboxPersistenceUseCase {
    Void updateStatus(OutboxPersistenceRequest input);
}