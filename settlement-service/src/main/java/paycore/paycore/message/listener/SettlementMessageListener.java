package paycore.paycore.message.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import paycore.paycore.application.usecase.ProcessSettlementUseCase;
import paycore.paycore.config.RabbitMqConfig;
import paycore.paycore.dto.SettlementRequestDto;
import paycore.paycore.entity.SettlementEntity;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementMessageListener {

    private final ProcessSettlementUseCase processSettlementUseCase;

    @Transactional
    @RabbitListener(queues = RabbitMqConfig.PAYMENT_SETTLEMENT_QUEUE)
    public void handleMessage(SettlementRequestDto input) {
        try {
            SettlementEntity settlement = processSettlementUseCase.execute(input);
            log.info("Settlement reservation created. sagaId={}, paymentId={}",
                    settlement.getSagaId(), settlement.getPaymentId());
        } catch (Exception e) {
            log.error("Failed to process settlement message: {}", input, e);
        }
    }
}

