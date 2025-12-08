package paycore.paycore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RetryTemplateConfig {
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(500L); // 초기 재시도 간격: 0.5초
        backOffPolicy.setMultiplier(2.0); // 재시도 간격 증가 배율: 2배
        backOffPolicy.setMaxInterval(2000L); // 재시도 간격 최대값: 2초
        retryTemplate.setBackOffPolicy(backOffPolicy);

        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy(3); // 최대 재시도 허용 횟수 : 3회
        retryTemplate.setRetryPolicy(simpleRetryPolicy);

        return retryTemplate;
    }
}
