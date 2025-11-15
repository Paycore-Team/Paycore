package paycore.paycore.message.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import paycore.paycore.application.usecase.PaymentMessageHandlerInterface;
import paycore.paycore.config.RabbitMqConfig;
import paycore.paycore.dto.PaymentMessageHandlerRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMessageListener {

    private final PaymentMessageHandlerInterface paymentMessageHandlerInterface;

    /**
     * RabbitMQ 수신 메서드
     * JSON 문자열을 파싱하여 상태 업데이트 수행
     */
    @Transactional
    @RabbitListener(queues = RabbitMqConfig.PAYMENT_ORDER_QUEUE)
    public void handleMessage(PaymentMessageHandlerRequest input) {
        paymentMessageHandlerInterface.handle(input);
    }
}
