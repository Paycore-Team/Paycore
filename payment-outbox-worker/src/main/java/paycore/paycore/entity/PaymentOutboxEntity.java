package paycore.paycore.entity;

import jakarta.persistence.*;
import lombok.Setter;
import paycore.paycore.domain.EventType;
import paycore.paycore.domain.OutboxStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Setter
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
}