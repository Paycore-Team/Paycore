package paycore.paycore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import paycore.paycore.entity.PaymentOutbox;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentOutboxRepository extends JpaRepository<PaymentOutbox, UUID> {
    Optional<PaymentOutbox> findBySagaId(UUID sagaId);
}