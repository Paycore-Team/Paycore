package paycore.paycore.message.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import paycore.paycore.application.usecase.ProcessSettlementUseCase;
import paycore.paycore.config.RabbitMqConfig;
import paycore.paycore.dto.SettlementRequestDto;
import paycore.paycore.entity.SettlementEntity;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementMessageListener {

    private final ObjectMapper objectMapper;
    private final ProcessSettlementUseCase processSettlementUseCase;

    @Transactional
    @RabbitListener(queues = RabbitMqConfig.SETTLEMENT_QUEUE)
    public void handleMessage(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            BigDecimal amount = node.get("amount").decimalValue();
            BigDecimal fee = node.get("fee").decimalValue();

            SettlementRequestDto request = new SettlementRequestDto(
                    UUID.fromString(node.get("sagaId").asText()),
                    UUID.fromString(node.get("paymentId").asText()),
                    UUID.fromString(node.get("orderId").asText()),
                    amount,
                    fee,
                    node.get("settlementAccount").asText()
            );

            SettlementEntity settlement = processSettlementUseCase.execute(request);
            log.info("Settlement reservation created. sagaId={}, paymentId={}, orderId={}",
                    settlement.getSagaId(), settlement.getPaymentId(), settlement.getOrderId());
        } catch (Exception e) {
            log.error("Failed to process settlement message: {}", message, e);
        }
    }
}

