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

    /**
     * Redis 연결 설정 값을 주입받아 구성 객체의 필드로 초기화한다.
     *
     * <p>생성자 매개변수는 Spring 프로퍼티에서 주입되며, 주입된 호스트, 포트, 비밀번호 값을 내부 필드에 저장한다.</p>
     *
     * @param redisHost Redis 서버 호스트명 (예: "localhost")
     * @param redisPort Redis 서버 포트 (예: 6379)
     * @param redisPassword Redis 서버 인증 비밀번호 (없으면 빈 문자열 또는 null일 수 있음)
     */
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

    /**
     * Lettuce 클라이언트를 사용하여 호스트, 포트, 비밀번호로 구성된 RedisConnectionFactory 빈을 생성한다.
     *
     * Redis 설정(spring.data.redis.host, spring.data.redis.port, spring.data.redis.password)에 따라
     * RedisStandaloneConfiguration을 구성하고 해당 설정으로 초기화된 LettuceConnectionFactory를 반환한다.
     *
     * @return 구성된 LettuceConnectionFactory 인스턴스 (Redis 연결 설정을 포함)
     */
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

    /**
     * String 키와 JSON 직렬화된 String 값을 사용하는 RedisTemplate 빈을 생성한다.
     *
     * 이 빈은 문자열 키를 위해 StringRedisSerializer를 사용하고,
     * 값은 Jackson2JsonRedisSerializer<String>로 직렬화되도록 구성된다.
     *
     * @return RedisTemplate<String, String> 인스턴스 — 키는 String으로 직렬화되고 값은 JSON 직렬화된 String으로 저장된다.
     */
    @Bean(name = "RedisTemplate")
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));

        return redisTemplate;
    }

    /**
     * IdempotencyKeyRecord 객체를 저장하고 조회하기 위한 RedisTemplate 빈을 생성하여 등록합니다.
     *
     * 이 템플릿은 키를 문자열로 직렬화하고 값은 Jackson JSON 직렬화기(IdempotencyKeyRecord 타입)로 직렬화하도록 구성됩니다.
     *
     * @return 키는 `String`, 값은 `IdempotencyKeyRecord` 타입인 구성된 `RedisTemplate<String, IdempotencyKeyRecord>` 인스턴스
     */
    @Bean(name = "IdempotencyKeyRecordRedisTemplate")
    public RedisTemplate<String, IdempotencyKeyRecord> idempotencyKeyRecordRedisTemplate() {
        RedisTemplate<String, IdempotencyKeyRecord> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(IdempotencyKeyRecord.class));

        return redisTemplate;
    }

    /**
     * IdempotencyResultResponse 객체를 키-값으로 사용하여 Redis와 상호작용하는 RedisTemplate 빈을 생성한다.
     *
     * 생성된 빈은 문자열 키를 사용하고 값은 `IdempotencyResultResponse` 타입을 JSON으로 직렬화/역직렬화하도록 구성되어 있으며,
     * Spring 컨텍스트에 "IdempotencyResultResponseRedisTemplate" 이름으로 등록된다.
     *
     * @return `IdempotencyResultResponse` 값을 JSON으로 직렬화/역직렬화하는 RedisTemplate<String, IdempotencyResultResponse> 인스턴스
     */
    @Bean(name = "IdempotencyResultResponseRedisTemplate")
    public RedisTemplate<String, IdempotencyResultResponse> idempotencyResultResponseRedisTemplate() {
        RedisTemplate<String, IdempotencyResultResponse> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(IdempotencyResultResponse.class));

        return redisTemplate;
    }
}