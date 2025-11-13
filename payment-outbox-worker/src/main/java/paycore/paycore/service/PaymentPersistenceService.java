package paycore.paycore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import paycore.paycore.common.UseCase;
import paycore.paycore.domain.OutboxStatus;
import paycore.paycore.entity.PaymentOutboxEntity;
import paycore.paycore.repository.PaymentOutboxRepository;
import paycore.paycore.usecase.PaymentPersistenceUseCase;
import paycore.paycore.usecase.model.PaymentPersistenceRequest;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentPersistenceService implements PaymentPersistenceUseCase {
    private final PaymentOutboxRepository paymentOutboxRepository;

    @Override
    public Void updateStatus(PaymentPersistenceRequest input) {
        String stringSagaID = input.id();
        UUID sagaId = UUID.fromString(stringSagaID);
        PaymentOutboxEntity entity = paymentOutboxRepository.findBySagaId(sagaId).orElseThrow(() -> new EntityNotFound(sagaId));
        entity.setStatus(OutboxStatus.SENT);

        paymentOutboxRepository.save(entity);

        return null;
    }

    private class EntityNotFound extends UseCase.Exception {
        public EntityNotFound(UUID id) {
            super("Entity not found. Id : " + id);
        }
    }
}