package paycore.paycore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import paycore.paycore.domain.OutboxStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_outbox")
public class OrderOutboxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID sagaId;

    private String eventType;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    private String apiKey;

    private String productDesc;

    private BigDecimal amount;

    private long amountTaxFree;

    private LocalDateTime createdAt;
}