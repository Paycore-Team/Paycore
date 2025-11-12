package paycore.paycore.domain;

import paycore.paycore.entity.IdempotencyKeyData;

public record IdempotencyKeyRecord(
        IdempotencyStatus status,
        IdempotencyKeyData result
) {
}
