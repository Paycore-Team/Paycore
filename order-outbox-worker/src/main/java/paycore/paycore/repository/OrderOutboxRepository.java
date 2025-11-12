package paycore.paycore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import paycore.paycore.entity.OrderOutboxEntity;

public interface OrderOutboxRepository extends JpaRepository<OrderOutboxEntity, Long> {
}