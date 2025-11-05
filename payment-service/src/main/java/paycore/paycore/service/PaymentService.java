package paycore.paycore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import paycore.paycore.common.UseCase;
import paycore.paycore.domain.IdempotencyKeyRecord;
import paycore.paycore.domain.IdempotencyKeyStatus;
import paycore.paycore.domain.IdempotencyResultResponse;
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

    /**
     * 결제 요청을 idempotency 키 기반으로 처리하고 결과를 영속화한 뒤 idempotency 잠금을 해제한다.
     *
     * <p>요청의 idempotency 키 상태를 확인하여 이미 처리된 결과가 있으면 조기 종료하고, 없으면 잠금을 획득하여
     * Mock 결제 호출을 수행한 뒤 결과를 저장하고 idempotency 결과를 저장하며 잠금을 해제한다.</p>
     *
     * @param input 처리할 결제 요청 정보(예: sagaId, idempotencyKey, 결제 파라미터들)
     * @return 항상 `null`을 반환한다.
     * @throws RecordAlreadyLeased 다른 프로세스가 동일한 idempotency 키에 대해 이미 잠금(lease)을 보유하고 있을 때 발생한다.
     * @throws ResultAlreadyExist 저장 시점에 동일한 결과가 이미 존재함이 검출되면 발생한다.
     */
    @Override
    public Void execute(PaymentServiceRequest input) {
        IdempotencyKeyRecord record = idempotencyKeyRepository.getStatus(input.idempotencyKey());
        IdempotencyKeyStatus status = record.getStatus();

        if (status == IdempotencyKeyStatus.LOCKED) {
            throw new RecordAlreadyLeased();
        }

        if (status == IdempotencyKeyStatus.SUCCESS || status == IdempotencyKeyStatus.SERVER_ERROR || status == IdempotencyKeyStatus.CLIENT_ERROR) {
            log.info("Idempotency key already exists\n {}", record.result());
            return null;
        }

        Boolean locked = idempotencyKeyRepository.acquireLockIfAbsent(input.idempotencyKey());
        if (!locked) {
            throw new RecordAlreadyLeased();
        }

        MockServiceRequest newMockServiceRequest = new MockServiceRequest(
                input.apiKey(),
                input.orderNo(),
                input.productDesc(),
                input.retUrl(),
                input.retCancelUrl(),
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
                        input.amount(),
                        input.storeName()
                )
        );

        IdempotencyKeyData newIdempotencyKeyData = new IdempotencyKeyData(
                result.httpResponse().statusCode(),
                result.httpResponse().body()
        );
        IdempotencyResultResponse response = idempotencyKeyRepository.saveResultAndReleaseLock(input.idempotencyKey(), newIdempotencyKeyData, 15);
        if (response.err() != null) {
            if (response.err().equals("result_exists")) {
                throw new ResultAlreadyExist();
            }
        }

        log.info("Payment request succeeded.\nSagaId : {}\n, IdempotencyKey : {}\n, orderNo : {}\n", input.sagaId(), input.idempotencyKey(), input.orderNo());

        return null;
    }

    private class RecordAlreadyLeased extends UseCase.Exception {
        /**
         * 다른 프로세스가 자원을 대여(잠금)한 경우를 나타내는 예외 객체를 생성한다.
         *
         * 생성된 예외의 상세 메시지는 "Resource is already leased by another process"이다.
         */
        public RecordAlreadyLeased() {
            super("Resource is already leased by another process\n");
        }
    }

    private class ResultAlreadyExist extends UseCase.Exception {
        /**
         * 중복된 결과가 이미 존재할 때 던져지는 예외 인스턴스를 생성한다.
         *
         * <p>예외 메시지는 "Result is already exist"이다.</p>
         */
        public ResultAlreadyExist() {
            super("Result is already exist\n");
        }
    }
}