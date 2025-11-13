package paycore.paycore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import paycore.paycore.domain.OutboxStatus;
import paycore.paycore.entity.SettlementOutboxEntity;

import java.util.Optional;
import java.util.UUID;

public interface SettlementOutboxRepository extends JpaRepository<SettlementOutboxEntity, UUID> {
    Optional<SettlementOutboxEntity> findFirstByStatusOrderByCreatedAtAsc(OutboxStatus status);
}

