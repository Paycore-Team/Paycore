package paycore.paycore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import paycore.paycore.application.usecase.PlaceOrderUseCase;
import paycore.paycore.common.UseCase;
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

@Service
@RequiredArgsConstructor
public class OrderService implements PlaceOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public OrderEntity execute(OrderRequestDto dto) {
        OrderEntity order = OrderEntity.builder()
                .id(UUID.randomUUID())
                .sagaId(UUID.randomUUID())
                .amount(dto.amount())
                .status(OrderStatus.PENDING)
                .build();

        orderRepository.save(order);

        createOutboxEvent(order, EventType.SUCCESS.name());
        return order;
    }


    private void createOutboxEvent(OrderEntity order, String eventType) {
        try {
            String payload = objectMapper.writeValueAsString(order);

            OrderOutboxEntity event = OrderOutboxEntity.builder()
                    .sagaId(order.getSagaId())
                    .eventType(eventType)
                    .payload(payload)
                    .status(OutboxStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxRepository.save(event);

        } catch (Exception | JsonProcessingException e) {
            throw new UseCase.Exception("Failed to serialize order: " + e.getMessage());
        }
    }
}
