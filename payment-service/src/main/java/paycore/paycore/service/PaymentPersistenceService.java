package paycore.paycore.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import paycore.paycore.common.UseCase;
import paycore.paycore.domain.OutboxStatus;
import paycore.paycore.entity.PaymentEntity;
import paycore.paycore.entity.PaymentOutboxEntity;
import paycore.paycore.repository.PaymentOutboxRepository;
import paycore.paycore.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentPersistenceService implements UseCase<PaymentPersistenceService.Input, Void> {
    private final PaymentRepository paymentRepository;
    private final PaymentOutboxRepository paymentOutboxRepository;

    public PaymentPersistenceService(
            PaymentRepository paymentRepository,
            PaymentOutboxRepository paymentOutboxRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentOutboxRepository = paymentOutboxRepository;
    }

    public record Input(
            UUID sagaId,
            int statusCode,
            String body,
            BigDecimal amount,
            String storeName
    ) {
    }

    @Override
    @Transactional
    public Void execute(Input input) {
        UUID paymentId = UUID.randomUUID();
        PaymentEntity paymentEntity = new PaymentEntity(
                paymentId,
                input.amount(),
                input.statusCode(),
                input.storeName()
        );
        PaymentOutboxEntity paymentOutboxEntity = new PaymentOutboxEntity(
                input.sagaId,
                paymentId,
                OutboxStatus.NEW,
                input.statusCode(),
                input.body()
        );

        paymentRepository.save(paymentEntity);
        paymentOutboxRepository.save(paymentOutboxEntity);

        return null;
    }
}