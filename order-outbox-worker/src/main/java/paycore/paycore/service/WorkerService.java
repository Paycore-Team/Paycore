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
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_SUCCESS_ROUTING_KEY = "order.success";

    private final RabbitMqPublisher rabbitMqPublisher;

    @Override
    public Void execute(WorkerRequest input) {
        if (input.eventType() == EventType.FAILURE) {
            log.warn("Saga [{}] - Received FAILURE event during order processing. request={}", input.sagaId(), input);

            return null;
        }

        CorrelationData correlationData = new CorrelationData(input.id().toString());
        rabbitMqPublisher.publish(ORDER_EXCHANGE, ORDER_SUCCESS_ROUTING_KEY, input, correlationData);

        return null;
    }
}