package paycore.paycore.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import paycore.paycore.usecase.model.WorkerRequest;

@Component
@RequiredArgsConstructor
public class RabbitMqPublisher {
    public static final String PAYMENT_EXCHANGE = "order-payment.exchange";
    public static final String ROUTING_KEY = "order-payment.created";
    private final RabbitTemplate rabbitTemplate;

    public void publish(WorkerRequest input, CorrelationData correlationData) {
        rabbitTemplate.convertAndSend(PAYMENT_EXCHANGE, ROUTING_KEY, input, correlationData);
    }
}