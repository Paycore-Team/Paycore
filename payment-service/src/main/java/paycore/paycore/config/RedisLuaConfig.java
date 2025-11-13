package paycore.paycore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import paycore.paycore.domain.IdempotencyKeyRecord;
import paycore.paycore.domain.IdempotencyLockResponse;
import paycore.paycore.domain.IdempotencyResultResponse;

@Configuration
public class RedisLuaConfig {
    @Bean("ReadLeaseAndResultAndGetLockScript")
    public DefaultRedisScript<IdempotencyKeyRecord> readLeaseAndResultAndGetLockScript() {
        DefaultRedisScript<IdempotencyKeyRecord> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText("""
                -- KEYS[1] = lease_key
                -- KEYS[2] = result_key
                -- ARGV[1] = lease_token
                -- ARGV[2] = ttl_seconds
                
                local ttl_seconds = tonumber(ARGV[2])
                
                local lease  = redis.call('GET', KEYS[1])
                if lease == false then
                    lease = nil
                end
                
                local result = redis.call('GET', KEYS[2])
                if result == false then
                    result = nil
                else
                    result = cjson.decode(result)
                end
                
                if result ~= nil then
                    return cjson.encode({ status = "DONE", result = result })
                end
                
                if lease ~= nil then
                    return cjson.encode({ status = "LOCKED", result = result })
                end
                
                redis.call('SET', KEYS[1], ARGV[1], 'NX', 'EX', ttl_seconds);
                
                return cjson.encode({ status = "ACQUIRED", result = result })
                """
        );
        redisScript.setResultType(IdempotencyKeyRecord.class);

        return redisScript;
    }

    @Bean("SaveResultAndReleaseLockScript")
    public DefaultRedisScript<IdempotencyResultResponse> saveResultAndReleaseLockScript() {
        DefaultRedisScript<IdempotencyResultResponse> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText("""
                -- KEYS[1] = lease_key
                -- KEYS[2] = result_key
                -- ARGV[1] = lease_token
                -- ARGV[2] = result_payload
                -- ARGV[3] = ttl_days
                
                local ttl_days = tonumber(ARGV[3])
                local ttl_seconds = ttl_days * 24 * 3600
                
                local err = nil
                
                if redis.call('EXISTS', KEYS[2]) == 1 then
                    err = 'result_exists'
                else
                    redis.call('SET', KEYS[2], ARGV[2], 'EX', ttl_seconds)
                
                    if redis.call('GET', KEYS[1]) == ARGV[1] then
                        redis.call('DEL', KEYS[1])
                    end
                end
                
                return cjson.encode({ err = err })
                """
        );
        redisScript.setResultType(IdempotencyResultResponse.class);

        return redisScript;
    }

    @Bean("HeartBeatScript")
    public DefaultRedisScript<IdempotencyLockResponse> heartBeatScript() {
        DefaultRedisScript<IdempotencyLockResponse> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText("""
                -- KEYS[1] = lease_key
                -- ARGV[1] = lease_token
                -- ARGV[2] = ttl_seconds
                
                local current = redis.call('GET', KEYS[1])
                local err = nil
                
                if current == ARGV[1] then
                    redis.call('EXPIRE', KEYS[1], tonumber(ARGV[2]))
                else
                    if current == false then
                        err = 'lock_not_found'
                    else
                        err = 'lock_not_owned'
                    end
                end
                
                return cjson.encode({ err = err })
                """
        );
        redisScript.setResultType(IdempotencyLockResponse.class);

        return redisScript;
    }

    @Bean("ReleaseLockScript")
    public DefaultRedisScript<IdempotencyLockResponse> releaseLockScript() {
        DefaultRedisScript<IdempotencyLockResponse> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText("""
                -- KEYS[1] = lease_key
                -- ARGV[1] = lease_token
                
                local currentValue = redis.call("GET", KEYS[1])
                local err = nil
                
                if currentValue == ARGV[1] then
                    redis.call("DEL", KEYS[1])
                else
                    err = 'lock_not_owned'
                end
                
                return cjson.encode({err = err})
                """
        );
        redisScript.setResultType(IdempotencyLockResponse.class);

        return redisScript;
    }
}
