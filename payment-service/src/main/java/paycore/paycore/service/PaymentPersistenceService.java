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

    /**
     * 결제 정보를 저장하고 해당 결제에 대한 아웃박스 엔티티를 생성하여 영속화한다.
     *
     * <p>메서드는 새 결제 ID를 생성하고 PaymentEntity 및 PaymentOutboxEntity를 구성한 뒤
     * 각각 저장소에 저장한다. 메서드는 트랜잭션 범위에서 실행되므로 런타임 예외 발생 시 전체 작업은 롤백된다.</p>
     *
     * @param input 결제 금액, 상태 코드, 매장명, 사가 ID 및 아웃박스 본문을 제공하는 요청 객체
     * @return 항상 `null`
     */
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