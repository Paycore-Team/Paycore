package paycore.paycore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.stereotype.Service;
import paycore.paycore.publisher.RabbitMqPublisher;
import paycore.paycore.usecase.WorkerUseCase;
import paycore.paycore.usecase.model.WorkerRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerService implements WorkerUseCase {
    private final RabbitMqPublisher rabbitMqPublisher;

    @Override
    public Void execute(WorkerRequest input) {
        CorrelationData correlationData = new CorrelationData(input.id().toString());
        rabbitMqPublisher.publish(input, correlationData);

        return null;
    }
}