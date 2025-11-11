package paycore.paycore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import paycore.paycore.common.UseCase;
import paycore.paycore.config.TaskSchedulerManager;
import paycore.paycore.domain.IdempotencyKeyRecord;
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
                    input.id(),
                    input.productDesc(),
                    retUrl,
                    retCancelUrl,
                    input.amount(),
                    input.amountTaxFree()
            );
            MockServiceResponse result = mockUseCase.execute(
                    newMockServiceRequest
            );

            paymentPersistenceUseCase.execute(
                    new PaymentPersistenceServiceRequest(
                            input.sagaId(),
                            result.httpResponse().statusCode(),
                            result.httpResponse().body(),
                            input.amount()
                    )
            );

            IdempotencyKeyData newIdempotencyKeyData = new IdempotencyKeyData(
                    result.httpResponse().statusCode(),
                    result.httpResponse().body()
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

            log.info("Payment request succeeded.\nSagaId : {}\n, orderNo : {}\n",
                    input.sagaId(),
                    input.id()
            );
        } finally {
            taskSchedulerManager.cancel(heartBeat);
        }

        return null;
    }

    private class RecordAlreadyLeased extends UseCase.Exception {
        public RecordAlreadyLeased() {
            super("Resource is already leased by another process\n");
        }
    }
}
