package paycore.paycore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import paycore.paycore.common.UseCase;
import paycore.paycore.domain.OutboxStatus;
import paycore.paycore.entity.OrderOutboxEntity;
import paycore.paycore.repository.OrderOutboxRepository;
import paycore.paycore.usecase.OutboxPersistenceUseCase;
import paycore.paycore.usecase.model.OutboxPersistenceRequest;

@Service
@RequiredArgsConstructor
public class OutboxPersistenceService implements OutboxPersistenceUseCase {
    private final OrderOutboxRepository orderOutboxRepository;

    @Override
    public Void updateStatus(OutboxPersistenceRequest input) {
        Long entityId = Long.valueOf(input.id());
        OrderOutboxEntity entity = orderOutboxRepository.findById(entityId).orElseThrow(() -> new EntityNotFound(entityId));
        entity.setStatus(OutboxStatus.SENT);
        orderOutboxRepository.save(entity);

        return null;
    }

    private class EntityNotFound extends UseCase.Exception {
        public EntityNotFound(Long id) {
            super("Entity not found. Id : " + id);
        }
    }
}