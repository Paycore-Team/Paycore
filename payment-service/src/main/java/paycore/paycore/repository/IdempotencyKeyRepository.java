package paycore.paycore.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;
import paycore.paycore.domain.IdempotencyKeyRecord;
import paycore.paycore.domain.IdempotencyResultResponse;
import paycore.paycore.entity.IdempotencyKeyData;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class IdempotencyKeyRepository {
    private final RedisTemplate<String, IdempotencyKeyRecord> idempotencyKeyRecordRedisTemplate;
    private final RedisTemplate<String, IdempotencyResultResponse> idempotencyResultResponseRedisTemplate;
    private final DefaultRedisScript<IdempotencyKeyRecord> readLeaseAndResultScript;
    private final DefaultRedisScript<IdempotencyResultResponse> saveResultAndReleaseLockScript;

    public IdempotencyKeyRecord getStatusOrLock(UUID idempotencyKey) {
        List<String> keys = List.of("{" + idempotencyKey + "-idempotencyKey}" + ":lease", "{" + idempotencyKey + "-idempotencyKey}" + ":result");

        return idempotencyKeyRecordRedisTemplate.execute(readLeaseAndResultScript, keys);
    }

    public IdempotencyResultResponse saveResultAndReleaseLock(UUID idempotencyKey, IdempotencyKeyData idempotencyKeyData, int ttlDays) {
        List<String> keys = List.of("{" + idempotencyKey + "-idempotencyKey}" + ":lease", "{" + idempotencyKey + "-idempotencyKey}" + ":result");

        return idempotencyResultResponseRedisTemplate.execute(saveResultAndReleaseLockScript, keys, idempotencyKeyData, ttlDays);
    }
}
