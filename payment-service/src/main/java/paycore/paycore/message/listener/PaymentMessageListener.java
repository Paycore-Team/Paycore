package paycore.paycore.message.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import paycore.paycore.service.PaymentService;
import paycore.paycore.usecase.model.PaymentServiceRequest;

@Component
public class PaymentMessageListener {
    PaymentService paymentService;

    /**
     * PaymentMessageListener의 인스턴스를 생성하고 메시지 처리에 사용할 PaymentService를 주입합니다.
     *
     * @param paymentService 수신된 결제 메시지를 처리하기 위해 사용되는 서비스 구현체
     */
    public PaymentMessageListener(
            PaymentService paymentService
    ) {
        this.paymentService = paymentService;
    }

    /**
     * RabbitMQ의 "payment.queue"에서 수신한 결제 요청을 처리한다.
     *
     * 수신한 PaymentServiceRequest를 PaymentService에 전달하여 결제 처리를 위임한다.
     *
     * @param input 처리할 결제 요청 데이터
     */
    @RabbitListener(queues = "payment.queue")
    public void onMessage(PaymentServiceRequest input) {
        paymentService.execute(input);
    }
}