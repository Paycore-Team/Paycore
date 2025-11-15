package paycore.paycore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import paycore.paycore.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID sagaId;

    private UUID idempotencyKey; // 일단 Entity 에 저장

    private String apiKey;

    private String productDesc;

    private BigDecimal amount;

    private long amountTaxFree;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime createdAt;

    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }
}
