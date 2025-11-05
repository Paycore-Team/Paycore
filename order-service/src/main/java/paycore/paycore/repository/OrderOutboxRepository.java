package paycore.paycore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import paycore.paycore.domain.OutboxStatus;
import paycore.paycore.entity.OrderOutboxEntity;

import java.util.List;

public interface OrderOutboxRepository extends JpaRepository<OrderOutboxEntity, Long> {
    List<OrderOutboxEntity> findAllByStatus(OutboxStatus status);
}
