package paycore.paycore.entity;

import paycore.paycore.domain.IdempotencyKeyStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record IdempotencyKeyData(
        IdempotencyKeyStatus status,
        Integer httpStatus,
        String responseBody,
        Long createdAt
) {
    public IdempotencyKeyData(Integer httpStatus, String responseBody) {
        this(mapStatus(httpStatus), httpStatus, responseBody, LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    private static IdempotencyKeyStatus mapStatus(Integer httpStatus) {
        // 요청 성공 시
        if (httpStatus >= 200 && httpStatus < 300) return IdempotencyKeyStatus.SUCCESS;

        // 요청 실패 시 (클라이언트 오류)
        if (httpStatus >= 400 && httpStatus < 500) return IdempotencyKeyStatus.CLIENT_ERROR;

        // 요청 실패 시 (서버 오류)
        return IdempotencyKeyStatus.SERVER_ERROR;
    }
}
