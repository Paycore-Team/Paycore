package paycore.paycore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import paycore.paycore.domain.OutboxStatus;
import paycore.paycore.entity.PaymentEntity;
import paycore.paycore.entity.PaymentOutboxEntity;
import paycore.paycore.repository.PaymentOutboxRepository;
import paycore.paycore.repository.PaymentRepository;
import paycore.paycore.usecase.PaymentPersistenceUseCase;
import paycore.paycore.usecase.model.PaymentPersistenceServiceRequest;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentPersistenceService implements PaymentPersistenceUseCase {
    private final PaymentRepository paymentRepository;
    private final PaymentOutboxRepository paymentOutboxRepository;

    @Override
    @Transactional
    public Void execute(PaymentPersistenceServiceRequest input) {
        UUID paymentId = UUID.randomUUID();
        PaymentEntity paymentEntity = new PaymentEntity(
                paymentId,
                input.amount(),
                input.statusCode(),
                input.storeName()
        );
        PaymentOutboxEntity paymentOutboxEntity = new PaymentOutboxEntity(
                input.sagaId(),
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