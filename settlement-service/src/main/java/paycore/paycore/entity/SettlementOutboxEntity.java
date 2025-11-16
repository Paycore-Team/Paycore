package paycore.paycore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import paycore.paycore.domain.EventType;
import paycore.paycore.domain.OutboxStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "settlement_outbox")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementOutboxEntity {

    @Id
    private UUID settlementId;

    @Column(nullable = false)
    private UUID sagaId;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    @Lob
    private String payload;

    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    public void markSent() {
        this.status = OutboxStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = OutboxStatus.FAILED;
    }
}

