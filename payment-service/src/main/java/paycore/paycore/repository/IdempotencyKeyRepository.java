package paycore.paycore.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;
import paycore.paycore.domain.IdempotencyKeyRecord;
import paycore.paycore.domain.IdempotencyLockResponse;
import paycore.paycore.domain.IdempotencyResultResponse;
import paycore.paycore.entity.IdempotencyKeyData;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class IdempotencyKeyRepository {
    private final RedisTemplate<String, IdempotencyLockResponse> idempotencyLockResponseRedisTemplate;
    private final RedisTemplate<String, IdempotencyKeyRecord> idempotencyKeyRecordRedisTemplate;
    private final RedisTemplate<String, IdempotencyResultResponse> idempotencyResultResponseRedisTemplate;

    private final DefaultRedisScript<IdempotencyKeyRecord> readLeaseAndResultScript;
    private final DefaultRedisScript<IdempotencyResultResponse> saveResultAndReleaseLockScript;
    private final DefaultRedisScript<IdempotencyLockResponse> heartBeatScript;

    public IdempotencyKeyRecord getStatusOrLock(UUID idempotencyKey, UUID lockToken, int ttlSeconds) {
        List<String> keys = List.of("{" + idempotencyKey + "-idempotencyKey}" + ":lease", "{" + idempotencyKey + "-idempotencyKey}" + ":result");

        return idempotencyKeyRecordRedisTemplate.execute(readLeaseAndResultScript, keys, lockToken, ttlSeconds);
    }

    public IdempotencyResultResponse saveResultAndReleaseLock(UUID idempotencyKey, UUID lockToken, IdempotencyKeyData idempotencyKeyData, int ttlDays) {
        List<String> keys = List.of("{" + idempotencyKey + "-idempotencyKey}" + ":lease", "{" + idempotencyKey + "-idempotencyKey}" + ":result");

        return idempotencyResultResponseRedisTemplate.execute(saveResultAndReleaseLockScript, keys, lockToken, idempotencyKeyData, ttlDays);
    }

    public IdempotencyLockResponse heartBeat(UUID idempotencyKey, UUID lockToken, int ttlSeconds) {
        List<String> keys = List.of("{" + idempotencyKey + "-idempotencyKey}" + ":lease");

        return idempotencyLockResponseRedisTemplate.execute(heartBeatScript, keys, lockToken, ttlSeconds);
    }
}
