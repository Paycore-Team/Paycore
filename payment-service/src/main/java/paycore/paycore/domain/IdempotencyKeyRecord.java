package paycore.paycore.domain;

import paycore.paycore.entity.IdempotencyKeyData;

public record IdempotencyKeyRecord(
        IdempotencyKeyData result,
        String lease
) {
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
