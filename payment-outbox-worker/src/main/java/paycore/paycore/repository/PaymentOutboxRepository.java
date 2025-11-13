package paycore.paycore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import paycore.paycore.entity.PaymentOutboxEntity;

import java.util.Optional;
import java.util.UUID;

public interface PaymentOutboxRepository extends JpaRepository<PaymentOutboxEntity, Long> {
    Optional<PaymentOutboxEntity> findBySagaId(UUID sagaId);
}