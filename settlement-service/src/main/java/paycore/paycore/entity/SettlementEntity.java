package paycore.paycore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import paycore.paycore.domain.SettlementStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "settlements")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID sagaId;

    @Column(nullable = false, unique = true)
    private UUID paymentId;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal fee;

    @Column(nullable = false)
    private String settlementAccount;

    @Column(nullable = false)
    private BigDecimal merchantPayoutAmount;

    @Column(nullable = false)
    private BigDecimal platformFeeAmount;

    @Enumerated(EnumType.STRING)
    private SettlementStatus status;

    private LocalDateTime settledAt;

    public void markCompleted(BigDecimal merchantPayoutAmount, BigDecimal platformFeeAmount) {
        this.status = SettlementStatus.COMPLETED;
        this.merchantPayoutAmount = merchantPayoutAmount;
        this.platformFeeAmount = platformFeeAmount;
        this.settledAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = SettlementStatus.FAILED;
        this.merchantPayoutAmount = BigDecimal.ZERO;
        this.platformFeeAmount = BigDecimal.ZERO;
        this.settledAt = null;
    }
}

