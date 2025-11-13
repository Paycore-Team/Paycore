package paycore.paycore.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import paycore.paycore.domain.EventType;
import paycore.paycore.domain.OutboxStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment_outbox")
public class PaymentOutboxEntity {
    @Id
    private UUID sagaId;

    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private String payload;

    private LocalDateTime createdAt;

    public PaymentOutboxEntity(UUID sagaId, UUID paymentId, OutboxStatus status, Integer httpStatus, String payload) {
        this.sagaId = sagaId;
        this.paymentId = paymentId;
        this.status = status;
        this.eventType = mapStatus(httpStatus);
        this.payload = payload;
        this.createdAt = LocalDateTime.now();
    }

    private static EventType mapStatus(Integer httpStatus) {
        // 요청 성공 시
        if (httpStatus >= 200 && httpStatus < 300) return EventType.SUCCESS;

        // 요청 실패 시 (클라이언트 오류)
        if (httpStatus >= 400 && httpStatus < 500) return EventType.FAILURE;

        // 요청 실패 시 (서버 오류)
        if (httpStatus >= 500) return EventType.FAILURE;

        // 그 외
        return EventType.FAILURE;
    }
}
