package paycore.paycore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import paycore.paycore.domain.IdempotencyKeyRecord;
import paycore.paycore.domain.IdempotencyResultResponse;

@Configuration
public class RedisConfig {
    private final String redisHost;
    private final int redisPort;
    private final String redisPassword;

    public RedisConfig(
            @Value("${spring.data.redis.host}")
            String redisHost,
            @Value("${spring.data.redis.port}")
            int redisPort,
            @Value("${spring.data.redis.password}")
            String redisPassword
    ) {
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.redisPassword = redisPassword;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(
                redisHost, redisPort
        );
        redisStandaloneConfiguration.setPassword(redisPassword);

        return new LettuceConnectionFactory(
                redisStandaloneConfiguration,
                LettuceClientConfiguration.defaultConfiguration()
        );
    }

    @Bean(name = "RedisTemplate")
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));

        return redisTemplate;
    }

    @Bean(name = "IdempotencyKeyRecordRedisTemplate")
    public RedisTemplate<String, IdempotencyKeyRecord> idempotencyKeyRecordRedisTemplate() {
        RedisTemplate<String, IdempotencyKeyRecord> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(IdempotencyKeyRecord.class));

        return redisTemplate;
    }

    @Bean(name = "IdempotencyResultResponseRedisTemplate")
    public RedisTemplate<String, IdempotencyResultResponse> idempotencyResultResponseRedisTemplate() {
        RedisTemplate<String, IdempotencyResultResponse> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(IdempotencyResultResponse.class));

        return redisTemplate;
    }
}
