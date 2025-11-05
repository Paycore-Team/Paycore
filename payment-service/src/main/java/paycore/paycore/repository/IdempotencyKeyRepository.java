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

    /**
     * IdempotencyKeyRepository 인스턴스를 생성하고 필요한 Redis 템플릿과 Lua 스크립트를 주입합니다.
     *
     * @param redisTemplate 일반 문자열 키/값 연산에 사용하는 RedisTemplate
     * @param idempotencyKeyRecordRedisTemplate idempotency 키의 lease와 result를 읽을 때 사용하는 RedisTemplate
     * @param idempotencyResultResponseRedisTemplate idempotency 처리 결과를 저장할 때 사용하는 RedisTemplate
     * @param readLeaseAndResultScript lease와 result를 원자적으로 조회하는 Redis Lua 스크립트
     * @param saveResultAndReleaseLockScript 결과를 저장하고 락(lease)을 해제하는 Redis Lua 스크립트
     */
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

    /**
     * 지정된 idempotency 키의 잠금(lease)과 저장된 결과(result)를 함께 조회한다.
     *
     * @param idempotencyKey 조회할 idempotency 키(UUID)
     * @return 해당 키의 잠금 및 결과 상태를 담은 {@code IdempotencyKeyRecord}. 키에 해당하는 레코드가 없으면 {@code null}.
     */
    public IdempotencyKeyRecord getStatus(UUID idempotencyKey) {
        List<String> keys = List.of(idempotencyKey + ":lease", idempotencyKey + ":result");

        return idempotencyKeyRecordRedisTemplate.execute(readLeaseAndResultScript, keys);
    }

    /**
     * 주어진 idempotency 키에 대해 10초 TTL의 임시 잠금(lease)을 존재하지 않을 경우에만 설정합니다.
     *
     * @param idempotencyKey 잠금을 설정할 대상 idempotency 키(UUID)
     * @return `true`이면 잠금이 설정되었음, `false`이면 이미 잠금이 존재함
     */
    public Boolean acquireLockIfAbsent(UUID idempotencyKey) {
        return redisTemplate.opsForValue().setIfAbsent(idempotencyKey + ":lease", "", Duration.ofSeconds(10));
    }

    /**
     * 주어진 idempotency 키에 대한 결과를 저장하고 해당 키의 잠금(lease)을 해제한다.
     *
     * @param idempotencyKey 결과를 저장하고 잠금을 해제할 대상 idempotency 키
     * @param idempotencyKeyData 저장할 idempotency 결과 데이터
     * @param ttlDays 결과 키에 설정할 TTL(일수)
     * @return IdempotencyResultResponse 저장된 결과와 잠금 해제 처리 상태를 포함한 응답 객체
     */
    public IdempotencyResultResponse saveResultAndReleaseLock(UUID idempotencyKey, IdempotencyKeyData idempotencyKeyData, int ttlDays) {
        List<String> keys = List.of(idempotencyKey + ":lease", idempotencyKey + ":result");

        return idempotencyResultResponseRedisTemplate.execute(saveResultAndReleaseLockScript, keys, idempotencyKeyData, ttlDays);
    }
}