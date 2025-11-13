package paycore.paycore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaymentOutboxWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentOutboxWorkerApplication.class, args);
    }
}