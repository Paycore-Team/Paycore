package paycore.paycore.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_ORDER_QUEUE = "payment-order.queue";
    public static final String PAYMENT_SUCCESS_ROUTING_KEY = "payment.success";
    public static final String PAYMENT_FAILURE_ROUTING_KEY = "payment.failure";

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

        return factory;
    }

    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Queue paymentOrderQueue() {
        return new Queue(PAYMENT_ORDER_QUEUE, true);
    }

    @Bean
    public Binding paymentOrderSuccessBinding() {
        return BindingBuilder.bind(paymentOrderQueue())
                .to(paymentExchange())
                .with(PAYMENT_SUCCESS_ROUTING_KEY);
    }

    @Bean
    public Binding paymentOrderFailureBinding() {
        return BindingBuilder.bind(paymentOrderQueue())
                .to(paymentExchange())
                .with(PAYMENT_FAILURE_ROUTING_KEY);
    }
}
