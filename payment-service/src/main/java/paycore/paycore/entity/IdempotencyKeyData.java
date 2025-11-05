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
    /**
     * 주어진 HTTP 상태 코드와 응답 본문으로 IdempotencyKeyData를 생성하며 상태는 HTTP 코드 범위에 따라 결정된다.
     *
     * 생성된 인스턴스의 `status`는 HTTP 상태 코드 범위(200–299: SUCCESS, 400–499: CLIENT_ERROR, 그 외: SERVER_ERROR)에 따라 설정되고,
     * `createdAt`은 시스템 기본 타임존 기준 현재 시각의 epoch 밀리초로 설정된다.
     *
     * @param httpStatus  저장할 HTTP 응답 상태 코드
     * @param responseBody 저장할 응답 본문 문자열
     */
    public IdempotencyKeyData(Integer httpStatus, String responseBody) {
        this(mapStatus(httpStatus), httpStatus, responseBody, LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    /**
     * HTTP 상태 코드를 기반으로 적절한 IdempotencyKeyStatus 값을 결정한다.
     *
     * @param httpStatus HTTP 응답 상태 코드
     * @return 200~299인 경우 `IdempotencyKeyStatus.SUCCESS`, 400~499인 경우 `IdempotencyKeyStatus.CLIENT_ERROR`, 그 외의 경우 `IdempotencyKeyStatus.SERVER_ERROR`
     */
    private static IdempotencyKeyStatus mapStatus(Integer httpStatus) {
        // 요청 성공 시
        if (httpStatus >= 200 && httpStatus < 300) return IdempotencyKeyStatus.SUCCESS;

        // 요청 실패 시 (클라이언트 오류)
        if (httpStatus >= 400 && httpStatus < 500) return IdempotencyKeyStatus.CLIENT_ERROR;

        // 요청 실패 시 (서버 오류)
        return IdempotencyKeyStatus.SERVER_ERROR;
    }
}