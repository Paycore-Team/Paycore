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

    public static final String PAYMENT_SETTLEMENT_EXCHANGE = "payment-settlement.exchange";
    public static final String PAYMENT_SETTLEMENT_ROUTING_KEY = "payment-settlement.created";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_ROUTING_KEY = "order.create";


    @Override
    public Void execute(WorkerRequest input) {
        CorrelationData correlationData = new CorrelationData(input.sagaId().toString());
        if (input.eventType() == EventType.SUCCESS) {
            rabbitMqPublisher.publish(PAYMENT_SETTLEMENT_EXCHANGE, PAYMENT_SETTLEMENT_ROUTING_KEY, input, correlationData);

            log.info("Saga [{}] has been published successfully to {}", input.sagaId(), PAYMENT_SETTLEMENT_EXCHANGE);
        } else {
            rabbitMqPublisher.publish(ORDER_EXCHANGE, ORDER_ROUTING_KEY, input, correlationData);

            log.info("Saga [{}] has been published successfully to {}", input.sagaId(), ORDER_EXCHANGE);
        }

        return null;
    }
}