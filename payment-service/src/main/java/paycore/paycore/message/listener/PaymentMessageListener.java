package paycore.paycore.message.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import paycore.paycore.usecase.PaymentUseCase;
import paycore.paycore.usecase.model.PaymentServiceRequest;

@Component
@RequiredArgsConstructor
public class PaymentMessageListener {
    private final PaymentUseCase paymentUseCase;

    @RabbitListener(queues = "payment.queue")
    public void onMessage(PaymentServiceRequest input) {
        paymentUseCase.execute(input);
    }
}
