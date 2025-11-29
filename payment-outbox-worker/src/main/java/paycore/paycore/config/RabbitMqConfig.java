package paycore.paycore.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import paycore.paycore.usecase.PaymentPersistenceUseCase;
import paycore.paycore.usecase.model.PaymentPersistenceRequest;

import java.util.concurrent.Executor;

@Configuration
@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(RabbitProperties.class)
public class RabbitMqConfig {
    private final RabbitProperties rabbitProperties;

    @Bean
    public Executor rabbitExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50); // 기본 스레드 수
        executor.setMaxPoolSize(100); // 최대 스레드 수
        executor.setQueueCapacity(10000); // 대기 작업을 10000 개까지 큐에 적재. 큐가 다 차면 스레드를 maxPoolSize 까지 증가시킴
        executor.setThreadNamePrefix("rabbit-callback-");
        executor.initialize();

        return executor;
    }

    @Bean
    public CachingConnectionFactory connectionFactory(Executor rabbitExecutor) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(rabbitProperties.getHost());
        connectionFactory.setUsername(rabbitProperties.getUsername());
        connectionFactory.setPassword(rabbitProperties.getPassword());
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED); // publisher confirm 활성화
        connectionFactory.setPublisherReturns(true); // 메시지 라우팅 실패 시 브로커가 메시지를 반환하도록 설정
        connectionFactory.setExecutor(rabbitExecutor); // 콜백 수행할 스레드 풀 지정
        connectionFactory.setChannelCacheSize(100);   // 캐싱할 채널 수

        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            CachingConnectionFactory connectionFactory,
            PaymentPersistenceUseCase paymentPersistenceUseCase
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                PaymentPersistenceRequest input = new PaymentPersistenceRequest(correlationData.getId());
                paymentPersistenceUseCase.updateStatus(input);

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