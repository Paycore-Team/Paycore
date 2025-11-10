package paycore.paycore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import paycore.paycore.common.UseCase;
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

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService implements PaymentUseCase {
    private final MockUseCase mockUseCase;
    private final PaymentPersistenceUseCase paymentPersistenceUseCase;
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    private static final String retUrl = "retUrl";
    private static final String retCancelUrl = "retCancelUrl";

    @Override
    public Void execute(PaymentServiceRequest input) {
        IdempotencyKeyRecord record = idempotencyKeyRepository.getStatusOrLock(input.sagaId());
        IdempotencyStatus status = record.status();

        if (status == IdempotencyStatus.LOCKED) {
            log.warn("Idempotency key [{}] already Locked", input.sagaId());

            throw new RecordAlreadyLeased();
        }

        if (status == IdempotencyStatus.DONE) {
            log.info("Idempotency key [{}] already exists", input.sagaId());

            return null;
        }

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
        IdempotencyResultResponse response = idempotencyKeyRepository.saveResultAndReleaseLock(input.sagaId(), newIdempotencyKeyData, 15);
        if (response.err() != null) {
            if (response.err().equals("result_exists")) {
                throw new ResultAlreadyExist();
            }
        }

        log.info("Payment request succeeded.\nSagaId : {}\n, IdempotencyKey : {}\n, orderNo : {}\n", input.sagaId(), input.sagaId(), input.id());

        return null;
    }

    private class RecordAlreadyLeased extends UseCase.Exception {
        public RecordAlreadyLeased() {
            super("Resource is already leased by another process\n");
        }
    }

    private class ResultAlreadyExist extends UseCase.Exception {
        public ResultAlreadyExist() {
            super("Result is already exist\n");
        }
    }
}
