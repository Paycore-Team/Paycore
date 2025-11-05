package paycore.paycore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import paycore.paycore.domain.IdempotencyKeyRecord;
import paycore.paycore.domain.IdempotencyResultResponse;

@Configuration
public class RedisLuaConfig {
    /**
     * lease 키와 result 키를 읽어 각각의 값을 포함한 JSON 객체(`{ lease, result }`)을 반환하는 Redis Lua 스크립트를 설정한 DefaultRedisScript 빈을 생성한다.
     *
     * <p>루아 스크립트 동작:
     * - KEYS[1]에서 lease 값을 읽고, 존재하지 않으면 `null`로 처리한다.
     * - KEYS[2]에서 result 값을 읽고, 존재하면 JSON으로 디코드하여 Lua 테이블로 반환값에 포함한다; 존재하지 않으면 `null`로 처리한다.
     *
     * @return DefaultRedisScript 객체 — 실행 결과로 `{"lease": <string|null>, "result": <object|null>}` 형태의 JSON 문자열을 반환하도록 구성된 Redis Lua 스크립트 (결과 타입: IdempotencyKeyRecord)
     */
    @Bean("ReadLeaseAndResultScript")
    public DefaultRedisScript<IdempotencyKeyRecord> readLeaseAndResultScript() {
        DefaultRedisScript<IdempotencyKeyRecord> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText("""
                -- KEYS[1] = lease key
                -- KEYS[2] = result key
                
                 local lease  = redis.call('GET', KEYS[1])
                 if lease == false then lease = nil end
                 local result = redis.call('GET', KEYS[2])
                    if result == false then
                        result = nil
                    else
                        result = cjson.decode(result)
                    end
                 return cjson.encode({ lease = lease, result = result })
                """
        );
        redisScript.setResultType(IdempotencyKeyRecord.class);

        return redisScript;
    }

    /**
     * 결과 페이로드를 지정된 결과 키에 저장하고 해당 리스(lease) 키를 삭제하는 Redis Lua 스크립트를 설정하여 반환한다.
     *
     * 스크립트 동작 요약:
     * - KEYS[1] = lease 키
     * - KEYS[2] = result 키
     * - ARGV[1] = 저장할 결과 페이로드 (문자열)
     * - ARGV[2] = TTL(일 단위)
     * - ARGV[2]를 초 단위 TTL로 변환하여 KEYS[2]에 저장하며, 이미 KEYS[2]가 존재하면 저장을 수행하지 않고 에러를 표시한다.
     * - 성공적으로 저장하면 KEYS[1]을 삭제한다.
     * - 반환값은 JSON으로 인코딩된 객체로, `err` 필드에 에러 코드(예: "result_exists") 또는 `null`이 들어간다.
     *
     * @return DefaultRedisScript&lt;IdempotencyResultResponse&gt; — 위 Lua 스크립트와 결과 타입(IdempotencyResultResponse)을 설정한 Redis 스크립트 객체. 
     */
    @Bean("SaveResultAndReleaseLockScript")
    public DefaultRedisScript<IdempotencyResultResponse> saveResultAndDeleteLeaseScript() {
        DefaultRedisScript<IdempotencyResultResponse> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText("""
                -- KEYS[1] = lease key
                -- KEYS[2] = result key
                -- ARGV[1] = result_payload
                -- ARGV[2] = ttl_days
                
                local ttl_days = tonumber(ARGV[2])
                local ttl_seconds = ttl_days * 24 * 3600
                
                local err = nil
                
                if redis.call('EXISTS', KEYS[2]) == 1 then
                  err = 'result_exists'
                else
                  redis.call('SET', KEYS[2], ARGV[1], 'EX', ttl_seconds)
                  redis.call('DEL', KEYS[1])
                end
                
                return cjson.encode({ err = err })
                """
        );
        redisScript.setResultType(IdempotencyResultResponse.class);

        return redisScript;
    }
}