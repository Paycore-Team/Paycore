package paycore.paycore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import paycore.paycore.entity.OrderOutboxEntity;

import java.util.Optional;
import java.util.UUID;

public interface OrderOutboxRepository extends JpaRepository<OrderOutboxEntity, Long> {
    Optional<OrderOutboxEntity> findBySagaId(UUID sagaId);
}
