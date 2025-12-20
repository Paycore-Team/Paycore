package paycore.paycore.entity;

import jakarta.persistence.*;
import lombok.*;
import paycore.paycore.domain.EventType;
import paycore.paycore.domain.OutboxStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_outbox")
public class OrderOutboxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    private UUID sagaId;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    private String apiKey;

    private String productDesc;

    private BigDecimal amount;

    private Long amountTaxFree;

    private LocalDateTime createdAt;
}