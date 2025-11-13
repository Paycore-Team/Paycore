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
    public static final String PAYMENT_QUEUE = "payment.queue";
    public static final String ROUTING_KEY = "payment.create";

    public static final String SETTLEMENT_EXCHANGE = "settlement.exchange";
    public static final String SETTLEMENT_QUEUE = "settlement.queue";
    public static final String SETTLEMENT_ROUTING_KEY = "settlement.created";

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
        factory.setPrefetchCount(10);
        factory.setMaxConcurrentConsumers(10);

        return factory;
    }

    @Bean
    public DirectExchange paymentDirectExchange() {
        return new DirectExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Queue paymentQueue() {
        return new Queue(PAYMENT_QUEUE);
    }

    @Bean
    public Binding paymentBinding() {
        return BindingBuilder.bind(paymentQueue())
                .to(paymentDirectExchange())
                .with(ROUTING_KEY);
    }

    @Bean
    public DirectExchange settlemnetDirectExchange() {
        return new DirectExchange(SETTLEMENT_EXCHANGE);
    }

    @Bean
    public Queue settlemnetQueue() {
        return new Queue(SETTLEMENT_QUEUE);
    }

    @Bean
    public Binding settlemnetBinding() {
        return BindingBuilder.bind(paymentQueue())
                .to(paymentDirectExchange())
                .with(SETTLEMENT_ROUTING_KEY);
    }
}