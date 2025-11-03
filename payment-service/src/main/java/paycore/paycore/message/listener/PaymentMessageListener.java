package paycore.paycore.message.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import paycore.paycore.service.PaymentService;

@Component
public class PaymentMessageListener {
    PaymentService paymentService;

    public PaymentMessageListener(
            PaymentService paymentService
    ) {
        this.paymentService = paymentService;
    }

    @RabbitListener(queues = "payment.queue")
    public void onMessage(PaymentService.Input input) {
        paymentService.execute(input);
    }
}
