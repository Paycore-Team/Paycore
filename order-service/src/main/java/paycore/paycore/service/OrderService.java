package paycore.paycore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import paycore.paycore.application.usecase.PlaceOrderUseCase;
import paycore.paycore.domain.EventType;
import paycore.paycore.domain.OrderStatus;
import paycore.paycore.domain.OutboxStatus;
import paycore.paycore.dto.OrderRequestDto;
import paycore.paycore.entity.OrderEntity;
import paycore.paycore.entity.OrderOutboxEntity;
import paycore.paycore.repository.OrderOutboxRepository;
import paycore.paycore.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService implements PlaceOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderOutboxRepository outboxRepository;

    @Override
    @Transactional
    public OrderEntity execute(OrderRequestDto dto) {
        OrderEntity existOrder = orderRepository.findByIdempotencyKey(dto.idempotencyKey());
        if (existOrder != null) {
            log.info("Order already exists {}", existOrder);

            return existOrder;
        }

        OrderEntity order = OrderEntity.builder()
                .sagaId(UUID.randomUUID())
                .idempotencyKey(dto.idempotencyKey())
                .apiKey(dto.apiKey())
                .productDesc(dto.productDesc())
                .amount(dto.amount())
                .amountTaxFree(dto.amountTaxFree())
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);

        createOutboxEvent(order, EventType.SUCCESS.name());
        return order;
    }

    private void createOutboxEvent(OrderEntity order, String eventType) {
        OrderOutboxEntity event = OrderOutboxEntity.builder()
                .orderId(order.getId())
                .sagaId(order.getSagaId())
                .eventType(eventType)
                .status(OutboxStatus.PENDING)
                .apiKey(order.getApiKey())
                .productDesc(order.getProductDesc())
                .amount(order.getAmount())
                .amountTaxFree(order.getAmountTaxFree())
                .createdAt(LocalDateTime.now())
                .build();

        outboxRepository.save(event);
    }
}
