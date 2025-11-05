package paycore.paycore.message.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import paycore.paycore.config.RabbitMqConfig;
import paycore.paycore.domain.EventType;
import paycore.paycore.domain.OrderStatus;
import paycore.paycore.entity.OrderEntity;
import paycore.paycore.repository.OrderRepository;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMessageListener {

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;

    /**
     * RabbitMQ 수신 메서드
     * JSON 문자열을 파싱하여 상태 업데이트 수행
     */
    @Transactional
    @RabbitListener(queues = RabbitMqConfig.ORDER_QUEUE)
    public void handleMessage(String message) {
        try {
            log.info("Received message from MQ: {}", message);
            JsonNode json = objectMapper.readTree(message);

            String eventType = json.get("eventType").asText();
            UUID sagaId = UUID.fromString(json.get("sagaId").asText());
            UUID orderId = UUID.fromString(json.get("orderId").asText());

            log.info("Processing eventType={}, sagaId={}, orderId={}", eventType, sagaId, orderId);

            OrderEntity order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

            switch (EventType.valueOf(eventType)) {
                case SUCCESS -> handleOrderPaid(order);
                case FAILURE -> handlePaymentFailed(order);
                default -> log.warn("Unknown event type received: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Failed to process message: {}", message, e);
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

}
