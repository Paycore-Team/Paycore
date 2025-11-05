package paycore.paycore.domain;

public enum IdempotencyKeyStatus {
    SUCCESS,
    CLIENT_ERROR,
    SERVER_ERROR,
    LOCKED,
    ABSENT
}