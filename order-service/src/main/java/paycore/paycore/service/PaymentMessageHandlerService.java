package paycore.paycore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import paycore.paycore.application.usecase.PaymentMessageHandlerInterface;
import paycore.paycore.domain.EventType;
import paycore.paycore.domain.OrderStatus;
import paycore.paycore.dto.PaymentMessageHandlerRequest;
import paycore.paycore.entity.OrderEntity;
import paycore.paycore.entity.OrderOutboxEntity;
import paycore.paycore.repository.OrderOutboxRepository;
import paycore.paycore.repository.OrderRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentMessageHandlerService implements PaymentMessageHandlerInterface {
    private final OrderRepository orderRepository;
    private final OrderOutboxRepository orderOutboxRepository;

    public void handle(PaymentMessageHandlerRequest input) {
        try {
            log.info("Received message from MQ: {}", input);

            String eventType = input.eventType().toString();
            UUID sagaId = input.sagaId();
            OrderOutboxEntity orderEntity = orderOutboxRepository.findBySagaId(sagaId).orElseThrow(() -> new EntityNotFound(sagaId));

            log.info("Processing eventType={}, sagaId={}, orderId={}", eventType, sagaId, orderEntity.getOrderId());

            OrderEntity order = orderRepository.findById(orderEntity.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderEntity.getOrderId()));

            switch (EventType.valueOf(eventType)) {
                case SUCCESS -> handleOrderPaid(order);
                case FAILURE -> handlePaymentFailed(order);
                default -> log.warn("Unknown event type received: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Failed to process message: {}", input, e);
        }
    }

    /**
     * 결제 완료 이벤트 처리
     */
    private void handleOrderPaid(OrderEntity order) {
        order.updateStatus(OrderStatus.PAID);
        orderRepository.save(order);
        log.info("Order {} marked as PAID", order.getId());
    }

    /**
     * 결제 실패 이벤트 처리 (보상 트랜잭션)
     */
    private void handlePaymentFailed(OrderEntity order) {
        order.updateStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.warn("Payment failed — Order {} rolled back to CANCELLED", order.getId());
    }

    private class EntityNotFound extends RuntimeException {
        public EntityNotFound(UUID entityId) {
            super("Entity with saga orderId " + entityId + " not found");
        }
    }
}
