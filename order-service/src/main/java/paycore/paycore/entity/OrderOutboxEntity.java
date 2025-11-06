package paycore.paycore.entity;

import jakarta.persistence.*;
import lombok.*;
import paycore.paycore.domain.OutboxStatus;

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

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    private LocalDateTime createdAt;

    public void markAsSent() {
        this.status = OutboxStatus.SENT;
    }
}
