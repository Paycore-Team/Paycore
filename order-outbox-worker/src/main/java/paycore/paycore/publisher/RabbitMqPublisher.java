package paycore.paycore.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import paycore.paycore.usecase.model.WorkerRequest;

@Component
@RequiredArgsConstructor
public class RabbitMqPublisher {
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_SUCCESS_ROUTING_KEY = "order.success";

    private final RabbitTemplate rabbitTemplate;

    public void publish(WorkerRequest input, CorrelationData correlationData) {
        rabbitTemplate.convertAndSend(ORDER_EXCHANGE, ORDER_SUCCESS_ROUTING_KEY, input, correlationData);
    }
}