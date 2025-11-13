package paycore.paycore.message.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import paycore.paycore.usecase.SettlementMessageHandlerUseCase;
import paycore.paycore.usecase.model.SettlementMessageHandlerServiceRequest;

@Component
@RequiredArgsConstructor
public class SettlementMessageListener {
    public static final String SETTLEMENT_QUEUE = "settlement.queue";

    private final SettlementMessageHandlerUseCase<SettlementMessageHandlerServiceRequest, Void> settlementMessageHandlerUseCase;

    @RabbitListener(queues = SETTLEMENT_QUEUE)
    public void onMessage(SettlementMessageHandlerServiceRequest input) {
        settlementMessageHandlerUseCase.handle(input);
    }
}
