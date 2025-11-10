package paycore.paycore.domain;

public enum IdempotencyStatus {
    DONE,
    LOCKED,
    ACQUIRED
}