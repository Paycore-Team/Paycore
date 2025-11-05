package paycore.paycore.domain;

import paycore.paycore.entity.IdempotencyKeyData;

public record IdempotencyKeyRecord(
        IdempotencyKeyData result,
        String lease
) {
    /**
     * Idempotency 키의 현재 상태를 판단하여 반환한다.
     *
     * 상태 결정 규칙:
     * <ul>
     *   <li>result가 {@code null}이고 lease도 {@code null}이면 {@code ABSENT}</li>
     *   <li>result가 {@code null}이고 lease가 {@code null}이 아니면 {@code LOCKED}</li>
     *   <li>그 외에는 {@code result.status()}</li>
     * </ul>
     *
     * @return {@code IdempotencyKeyStatus.ABSENT}인 경우는 {@code result}와 {@code lease}가 모두 {@code null}일 때,
     *         {@code IdempotencyKeyStatus.LOCKED}인 경우는 {@code result}가 {@code null}이고 {@code lease}가 존재할 때,
     *         그 외에는 {@code result.status()}를 반환한다.
     */
    public IdempotencyKeyStatus getStatus() {
        if (result == null) {
            if (lease == null) {
                return IdempotencyKeyStatus.ABSENT;
            }

            return IdempotencyKeyStatus.LOCKED;
        }

        return result().status();
    }
}