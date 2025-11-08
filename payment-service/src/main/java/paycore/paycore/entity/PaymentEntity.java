package paycore.paycore.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import paycore.paycore.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentEntity {
    @Id
    private UUID paymentId;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime createdAt;

    public PaymentEntity(UUID paymentId, BigDecimal amount, Integer httpStatus) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.status = mapStatus(httpStatus);
        this.createdAt = LocalDateTime.now();
    }

    private static PaymentStatus mapStatus(Integer httpStatus) {
        // 요청 성공 시
        if (httpStatus >= 200 && httpStatus < 300) return PaymentStatus.SUCCESS;

        // 요청 실패 시 (클라이언트 오류)
        if (httpStatus >= 400 && httpStatus < 500) return PaymentStatus.CLIENT_ERROR;

        // 요청 실패 시 (서버 오류)
        if (httpStatus >= 500) return PaymentStatus.SERVER_ERROR;

        // 그 외
        return PaymentStatus.UNKNOWN_ERROR;
    }
}
