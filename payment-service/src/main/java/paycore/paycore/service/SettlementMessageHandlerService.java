package paycore.paycore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import paycore.paycore.domain.EventType;
import paycore.paycore.domain.PaymentStatus;
import paycore.paycore.entity.PaymentEntity;
import paycore.paycore.entity.PaymentOutboxEntity;
import paycore.paycore.repository.PaymentOutboxRepository;
import paycore.paycore.repository.PaymentRepository;
import paycore.paycore.usecase.SettlementMessageHandlerUseCase;
import paycore.paycore.usecase.model.SettlementMessageHandlerServiceRequest;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementMessageHandlerService implements SettlementMessageHandlerUseCase<SettlementMessageHandlerServiceRequest, Void> {
    private final PaymentRepository paymentRepository;
    private final PaymentOutboxRepository paymentOutboxRepository;

    @Transactional
    public Void handle(SettlementMessageHandlerServiceRequest input) {
        if (input.eventType() == EventType.SUCCESS) {
            log.info("Settlement Success for sagaId : {}", input.sagaId());

            return null;
        }

        log.warn("Settlement Failure for sagaId : {}", input.sagaId());

        PaymentOutboxEntity paymentOutboxEntity = paymentOutboxRepository.findBySagaId(input.sagaId()).orElseThrow(() -> new EntityNotFound(input.sagaId()));
        PaymentEntity paymentEntity = paymentRepository.findByPaymentId(paymentOutboxEntity.getPaymentId()).orElseThrow(() -> new EntityNotFound(input.sagaId()));
        paymentOutboxEntity.setEventType(EventType.FAILURE);
        paymentEntity.setStatus(PaymentStatus.FAILED);

        paymentOutboxRepository.save(paymentOutboxEntity);
        paymentRepository.save(paymentEntity);

        return null;
    }

    private class EntityNotFound extends RuntimeException {
        public EntityNotFound(UUID entityId) {
            super("Entity with saga id " + entityId + " not found");
        }
    }
}
