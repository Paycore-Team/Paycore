package paycore.paycore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import paycore.paycore.common.UseCase;
import paycore.paycore.config.TaskSchedulerManager;
import paycore.paycore.domain.IdempotencyKeyRecord;
import paycore.paycore.domain.IdempotencyLockResponse;
import paycore.paycore.domain.IdempotencyResultResponse;
import paycore.paycore.domain.IdempotencyStatus;
import paycore.paycore.entity.IdempotencyKeyData;
import paycore.paycore.repository.IdempotencyKeyRepository;
import paycore.paycore.usecase.MockUseCase;
import paycore.paycore.usecase.PaymentPersistenceUseCase;
import paycore.paycore.usecase.PaymentUseCase;
import paycore.paycore.usecase.model.MockServiceRequest;
import paycore.paycore.usecase.model.MockServiceResponse;
import paycore.paycore.usecase.model.PaymentPersistenceServiceRequest;
import paycore.paycore.usecase.model.PaymentServiceRequest;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService implements PaymentUseCase {
    private final MockUseCase mockUseCase;
    private final PaymentPersistenceUseCase paymentPersistenceUseCase;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final TaskSchedulerManager taskSchedulerManager;
    private final HeartBeatService heartBeatService;

    private static final String retUrl = "retUrl";
    private static final String retCancelUrl = "retCancelUrl";
    private final RetryTemplate retryTemplate;

    @Override
    public Void execute(PaymentServiceRequest input) {
        UUID lockToken = UUID.randomUUID();
        int ttlSeconds = 3;

        IdempotencyKeyRecord record = idempotencyKeyRepository.getStatusOrLock(
                input.sagaId(),
                lockToken,
                ttlSeconds
        );
        IdempotencyStatus status = record.status();

        if (status == IdempotencyStatus.LOCKED) {
            log.warn("Idempotency key [{}] already Locked", input.sagaId());

            throw new RecordAlreadyLeased();
        }

        if (status == IdempotencyStatus.DONE) {
            log.info("Idempotency key [{}] already exists", input.sagaId());

            return null;
        }

        ScheduledFuture<?> heartBeat = taskSchedulerManager.scheduleAtFixedRate(
                () -> heartBeatService.startHeartBeat(input.sagaId(), lockToken, ttlSeconds),
                Duration.ofSeconds(ttlSeconds / 3)
        );

        try {
            MockServiceRequest newMockServiceRequest = new MockServiceRequest(
                    input.apiKey(),
                    input.orderId(),
                    input.productDesc(),
                    retUrl,
                    retCancelUrl,
                    input.amount(),
                    input.amountTaxFree()
            );
            MockServiceResponse result = retryTemplate.execute(
                    context -> mockUseCase.execute(newMockServiceRequest),
                    context -> {
                        // 재시도 후에도 응답 코드가 500 이면 Outbox 에 실패 내역을 저장해 결제 실패 이벤트를 발행한다.
                        // 이후 예외를 발생시켜 RabbitMQ 재처리를 유도한다.
                        log.warn("After {} retries, the external server returned an error: {}", context.getRetryCount(), context.getLastThrowable().getMessage());

                        paymentPersistenceUseCase.execute(
                                new PaymentPersistenceServiceRequest(
                                        input.sagaId(),
                                        500,
                                        input.apiKey(),
                                        input.amount()
                                )
                        );

                        throw (RuntimeException) context.getLastThrowable();
                    }
            );

            paymentPersistenceUseCase.execute(
                    new PaymentPersistenceServiceRequest(
                            input.sagaId(),
                            result.data().statusCode(),
                            input.apiKey(),
                            input.amount()
                    )
            );

            IdempotencyKeyData newIdempotencyKeyData = new IdempotencyKeyData(
                    result.data().statusCode(),
                    result.data().body()
            );
            IdempotencyResultResponse response = idempotencyKeyRepository.saveResultAndReleaseLock(
                    input.sagaId(),
                    lockToken,
                    newIdempotencyKeyData,
                    15
            );
            if (response.err() != null) {
                if (response.err().equals("result_exists")) {
                    log.warn("Result [{}] already exists", input.sagaId());

                    return null;
                }
            }

            log.info("Payment request succeeded.SagaId : {}, orderNo : {}",
                    input.sagaId(),
                    input.orderId()
            );
        } catch (Exception e) {
            log.warn("Error : {}", e.getMessage());

            IdempotencyLockResponse response = idempotencyKeyRepository.releaseLock(input.sagaId(), lockToken);
            if (response.err() != null) {
                log.warn(response.err());
            }

            throw e;
        } finally {
            taskSchedulerManager.cancel(heartBeat);
        }

        return null;
    }

    private class RecordAlreadyLeased extends UseCase.Exception {
        public RecordAlreadyLeased() {
            super("Resource is already leased by another process");
        }
    }
}
