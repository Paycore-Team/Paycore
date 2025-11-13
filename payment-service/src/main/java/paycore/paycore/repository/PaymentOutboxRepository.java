package paycore.paycore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import paycore.paycore.entity.PaymentOutboxEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentOutboxRepository extends JpaRepository<PaymentOutboxEntity, UUID> {
    Optional<PaymentOutboxEntity> findBySagaId(UUID sagaId);
}