package paycore.paycore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import paycore.paycore.domain.SettlementStatus;
import paycore.paycore.entity.SettlementEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SettlementRepository extends JpaRepository<SettlementEntity, UUID> {
    Optional<SettlementEntity> findByPaymentId(UUID paymentId);
    Optional<SettlementEntity> findBySagaId(UUID sagaId);
    List<SettlementEntity> findAllByStatus(SettlementStatus status);
}

