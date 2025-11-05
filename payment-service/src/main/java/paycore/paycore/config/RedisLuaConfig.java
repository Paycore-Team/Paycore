package paycore.paycore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import paycore.paycore.domain.IdempotencyKeyRecord;
import paycore.paycore.domain.IdempotencyResultResponse;

@Configuration
public class RedisLuaConfig {
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
