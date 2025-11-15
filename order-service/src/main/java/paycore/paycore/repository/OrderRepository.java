package paycore.paycore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import paycore.paycore.entity.OrderEntity;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    OrderEntity findByIdempotencyKey(UUID idempotencyKey);
}
