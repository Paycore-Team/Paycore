package paycore.paycore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import paycore.paycore.domain.IdempotencyLockResponse;
import paycore.paycore.repository.IdempotencyKeyRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HeartBeatService {
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    public void startHeartBeat(UUID key, UUID lockToken, int ttlSeconds) {
        IdempotencyLockResponse result = idempotencyKeyRepository.heartBeat(key, lockToken, ttlSeconds);
        if (result.err() != null) {
            log.warn(result.err());

            return;
        }

        log.info("Redis lock heartbeat success (key={}, owner={}, ttl={}s)", key, lockToken, ttlSeconds);
    }
}
