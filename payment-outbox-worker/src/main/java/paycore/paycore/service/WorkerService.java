package paycore.paycore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.stereotype.Service;
import paycore.paycore.domain.EventType;
import paycore.paycore.publisher.RabbitMqPublisher;
import paycore.paycore.usecase.WorkerUseCase;
import paycore.paycore.usecase.model.WorkerRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerService implements WorkerUseCase {
    private final RabbitMqPublisher rabbitMqPublisher;

    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_SUCCESS_ROUTING_KEY = "payment.success";
    public static final String PAYMENT_FAILURE_ROUTING_KEY = "payment.failure";


    @Override
    public Void execute(WorkerRequest input) {
        String routingKey = input.eventType() == EventType.SUCCESS ? PAYMENT_SUCCESS_ROUTING_KEY : PAYMENT_FAILURE_ROUTING_KEY;
        CorrelationData correlationData = new CorrelationData(input.sagaId().toString());
        rabbitMqPublisher.publish(PAYMENT_EXCHANGE, routingKey, input, correlationData);

        log.info("Saga [{}] has been published successfully with routing key {}", input.sagaId(), routingKey);

        return null;
    }
}