package paycore.paycore.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import paycore.paycore.usecase.model.WorkerRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publish(String exchange, String routingKey, WorkerRequest input, CorrelationData correlationData) {
        rabbitTemplate.convertAndSend(exchange, routingKey, input, correlationData);
    }
}