package paycore.paycore.entity;

import jakarta.persistence.*;
import lombok.*;
import paycore.paycore.domain.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID sagaId;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }
}
