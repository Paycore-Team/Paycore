package paycore.paycore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import paycore.paycore.domain.EventType;
import paycore.paycore.domain.OutboxStatus;
import paycore.paycore.domain.PaymentStatus;
import paycore.paycore.entity.Payment;
import paycore.paycore.entity.PaymentOutbox;
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

        PaymentOutbox paymentOutbox = paymentOutboxRepository.findBySagaId(input.sagaId()).orElseThrow(() -> new EntityNotFound(input.sagaId()));
        Payment payment = paymentRepository.findByPaymentId(paymentOutbox.getPaymentId()).orElseThrow(() -> new EntityNotFound(input.sagaId()));
        paymentOutbox.setEventType(EventType.FAILURE);
        paymentOutbox.setStatus(OutboxStatus.PENDING);
        payment.setStatus(PaymentStatus.FAILED);

        paymentOutboxRepository.save(paymentOutbox);
        paymentRepository.save(payment);

        return null;
    }

    private class EntityNotFound extends RuntimeException {
        public EntityNotFound(UUID entityId) {
            super("Entity with saga orderId " + entityId + " not found");
        }
    }
}
