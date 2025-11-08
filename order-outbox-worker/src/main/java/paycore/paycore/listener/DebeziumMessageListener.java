package paycore.paycore.listener;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import paycore.paycore.config.JsonMapper;
import paycore.paycore.dto.OrderOutboxRecord;
import paycore.paycore.usecase.WorkerUseCase;
import paycore.paycore.usecase.model.WorkerServiceRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class DebeziumMessageListener {
    private final WorkerUseCase workerUseCase;
    private final JsonMapper jsonMapper;

    @ServiceActivator(inputChannel = "debeziumInputChannel")
    public void handler(Message<?> message) {
        byte[] payload = (byte[]) message.getPayload();

        try {
            JsonNode json = jsonMapper.mapper().readTree(payload);
            OrderOutboxRecord record = jsonMapper.mapper().treeToValue(json.path("payload").path("after"), OrderOutboxRecord.class);
            log.info("Received order event: {}", record);

            WorkerServiceRequest workerServiceRequest = new WorkerServiceRequest(
                    record.id(),
                    record.sagaId(),
                    record.eventType(),
                    record.status(),
                    record.apiKey(),
                    record.productDesc(),
                    record.amount(),
                    record.amountTaxFree(),
                    record.createdAt()
            );
            workerUseCase.execute(workerServiceRequest);

        } catch (Exception e) {
            log.error("Error while parse debezium message", e);
        }
    }
}
