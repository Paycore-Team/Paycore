package paycore.paycore.listener;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import paycore.paycore.common.JsonMapper;
import paycore.paycore.domain.OutboxStatus;
import paycore.paycore.dto.PaymentOutboxRecord;
import paycore.paycore.usecase.WorkerUseCase;
import paycore.paycore.usecase.model.WorkerRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class DebeziumMessageListener {
    private final WorkerUseCase workerUseCase;
    private final JsonMapper jsonMapper;

    @ServiceActivator(inputChannel = "debeziumInputChannel")
    public Void handler(Message<?> message) {
        byte[] payload = (byte[]) message.getPayload();

        try {
            JsonNode json = jsonMapper.mapper().readTree(payload);
            PaymentOutboxRecord record = jsonMapper.mapper().treeToValue(json.path("payload").path("after"), PaymentOutboxRecord.class);
            log.info("Received payment event: {}", record);

            if (record.status() == OutboxStatus.SENT) {
                log.error("Record has been sent");

                return null;
            }
            WorkerRequest workerRequest = new WorkerRequest(
                    record.sagaId(),
                    record.paymentId(),
                    record.status(),
                    record.eventType(),
                    record.payload(),
                    record.createdAt()
            );
            workerUseCase.execute(workerRequest);

        } catch (Exception e) {
            log.error("Error while parse debezium message", e);
        }

        return null;
    }
}
