package paycore.paycore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import paycore.paycore.domain.OutboxStatus;
import paycore.paycore.entity.Payment;
import paycore.paycore.entity.PaymentOutbox;
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
        Payment payment = new Payment(
                paymentId,
                input.apiKey(),
                input.amount(),
                input.statusCode()
        );
        PaymentOutbox paymentOutbox = new PaymentOutbox(
                input.sagaId(),
                paymentId,
                OutboxStatus.PENDING,
                input.statusCode(),
                input.apiKey(),
                input.amount()
        );

        paymentRepository.save(payment);
        paymentOutboxRepository.save(paymentOutbox);

        return null;
    }
}