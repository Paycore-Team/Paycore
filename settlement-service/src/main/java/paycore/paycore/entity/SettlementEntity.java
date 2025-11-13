package paycore.paycore.entity;

import jakarta.persistence.*;
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

    private BigDecimal amount;

    private BigDecimal fee;

    private String apiKey;

    private BigDecimal merchantPayoutAmount;

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

