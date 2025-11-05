package paycore.paycore.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;
import paycore.paycore.domain.IdempotencyKeyRecord;
import paycore.paycore.domain.IdempotencyResultResponse;
import paycore.paycore.entity.IdempotencyKeyData;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Repository
public class IdempotencyKeyRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, IdempotencyKeyRecord> idempotencyKeyRecordRedisTemplate;
    private final RedisTemplate<String, IdempotencyResultResponse> idempotencyResultResponseRedisTemplate;
    private final DefaultRedisScript<IdempotencyKeyRecord> readLeaseAndResultScript;
    private final DefaultRedisScript<IdempotencyResultResponse> saveResultAndReleaseLockScript;

    public IdempotencyKeyRepository(
            @Qualifier("RedisTemplate")
            RedisTemplate<String, String> redisTemplate,
            @Qualifier("IdempotencyKeyRecordRedisTemplate")
            RedisTemplate<String, IdempotencyKeyRecord> idempotencyKeyRecordRedisTemplate,
            @Qualifier("IdempotencyResultResponseRedisTemplate")
            RedisTemplate<String, IdempotencyResultResponse> idempotencyResultResponseRedisTemplate,

            @Qualifier("ReadLeaseAndResultScript")
            DefaultRedisScript<IdempotencyKeyRecord> readLeaseAndResultScript,
            @Qualifier("SaveResultAndReleaseLockScript")
            DefaultRedisScript<IdempotencyResultResponse> saveResultAndReleaseLockScript
    ) {
        this.redisTemplate = redisTemplate;
        this.idempotencyKeyRecordRedisTemplate = idempotencyKeyRecordRedisTemplate;
        this.idempotencyResultResponseRedisTemplate = idempotencyResultResponseRedisTemplate;

        this.readLeaseAndResultScript = readLeaseAndResultScript;
        this.saveResultAndReleaseLockScript = saveResultAndReleaseLockScript;
    }

    public IdempotencyKeyRecord getStatus(UUID idempotencyKey) {
        List<String> keys = List.of(idempotencyKey + ":lease", idempotencyKey + ":result");

        return idempotencyKeyRecordRedisTemplate.execute(readLeaseAndResultScript, keys);
    }

    public Boolean acquireLockIfAbsent(UUID idempotencyKey) {
        return redisTemplate.opsForValue().setIfAbsent(idempotencyKey + ":lease", "", Duration.ofSeconds(10));
    }

    public IdempotencyResultResponse saveResultAndReleaseLock(UUID idempotencyKey, IdempotencyKeyData idempotencyKeyData, int ttlDays) {
        List<String> keys = List.of(idempotencyKey + ":lease", idempotencyKey + ":result");

        return idempotencyResultResponseRedisTemplate.execute(saveResultAndReleaseLockScript, keys, idempotencyKeyData, ttlDays);
    }
}
