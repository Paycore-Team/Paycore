package paycore.paycore.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_PAYMENT_QUEUE = "order-payment.queue";
    public static final String ORDER_SUCCESS_ROUTING_KEY = "order.success";
    public static final String ORDER_PAYMENT_DEAD_LETTER_QUEUE = "order-payment.dead-letter.queue";
    public static final String ORDER_PAYMENT_DEAD_LETTER_ROUTING_KEY = "order-payment.dead-letter";

    public static final String SETTLEMENT_EXCHANGE = "settlement.exchange";
    public static final String SETTLEMENT_PAYMENT_QUEUE = "settlement-payment.queue";
    public static final String SETTLEMENT_SUCCESS_ROUTING_KEY = "settlement.success";
    public static final String SETTLEMENT_FAILURE_ROUTING_KEY = "settlement.failure";

    private static final int DEFAULT_PREFETCH_COUNT = 1000;
    private static final int DEFAULT_CONCURRENT_CONSUMERS = 1000;
    private static final int DEFAULT_MAX_CONCURRENT_CONSUMERS = 1000;

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter jackson2JsonMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter);
        factory.setPrefetchCount(DEFAULT_PREFETCH_COUNT);
        factory.setConcurrentConsumers(DEFAULT_CONCURRENT_CONSUMERS);
        factory.setMaxConcurrentConsumers(DEFAULT_MAX_CONCURRENT_CONSUMERS);
        factory.setDefaultRequeueRejected(false); // 예외 발생 시 requeue하지 않고 nack 처리하여 DLQ로 전달되도록 설정

        return factory;
    }

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    // DLQ 정의
    @Bean
    public Queue orderPaymentDeadLetterQueue() {
        return new Queue(ORDER_PAYMENT_DEAD_LETTER_QUEUE);
    }

    @Bean
    public Binding orderPaymentFailureBinding() {
        return BindingBuilder.bind(orderPaymentDeadLetterQueue())
                .to(orderExchange())
                .with(ORDER_PAYMENT_DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public Queue orderPaymentQueue() {
        return QueueBuilder.durable(ORDER_PAYMENT_QUEUE)
                // DLQ 설정
                .withArgument("x-dead-letter-exchange", ORDER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ORDER_PAYMENT_DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding orderPaymentSuccessBinding() {
        return BindingBuilder.bind(orderPaymentQueue())
                .to(orderExchange())
                .with(ORDER_SUCCESS_ROUTING_KEY);
    }

    @Bean
    public DirectExchange settlementExchange() {
        return new DirectExchange(SETTLEMENT_EXCHANGE);
    }

    @Bean
    public Queue settlementPaymentQueue() {
        return new Queue(SETTLEMENT_PAYMENT_QUEUE);
    }

    @Bean
    public Binding settlementPaymentSuccessBinding() {
        return BindingBuilder.bind(settlementPaymentQueue())
                .to(settlementExchange())
                .with(SETTLEMENT_SUCCESS_ROUTING_KEY);
    }

    @Bean
    public Binding settlementPaymentFailureBinding() {
        return BindingBuilder.bind(settlementPaymentQueue())
                .to(settlementExchange())
                .with(SETTLEMENT_FAILURE_ROUTING_KEY);
    }
}