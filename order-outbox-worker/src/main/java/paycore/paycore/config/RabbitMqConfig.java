package paycore.paycore.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import paycore.paycore.usecase.OutboxPersistenceUseCase;
import paycore.paycore.usecase.model.OutboxPersistenceRequest;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class RabbitMqConfig {
    @Bean
    public RabbitTemplate rabbitTemplate(
            CachingConnectionFactory connectionFactory,
            OutboxPersistenceUseCase outboxPersistenceUseCase
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                OutboxPersistenceRequest input = new OutboxPersistenceRequest(correlationData.getId());
                outboxPersistenceUseCase.updateStatus(input);

                log.info("Message confirmed: {}", correlationData);
            } else
                log.error("Message failed: {}", cause);
        });
        rabbitTemplate.setReturnsCallback(returnCallback -> {
            log.error("Message returned: {}", returnCallback.getMessage());
        });

        return rabbitTemplate;
    }
}