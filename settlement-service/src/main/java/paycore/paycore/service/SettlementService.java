package paycore.paycore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import paycore.paycore.application.usecase.ProcessSettlementUseCase;
import paycore.paycore.common.UseCase;
import paycore.paycore.domain.EventType;
import paycore.paycore.domain.OutboxStatus;
import paycore.paycore.domain.SettlementStatus;
import paycore.paycore.dto.SettlementRequestDto;
import paycore.paycore.entity.SettlementEntity;
import paycore.paycore.entity.SettlementOutboxEntity;
import paycore.paycore.repository.SettlementOutboxRepository;
import paycore.paycore.repository.SettlementRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettlementService implements ProcessSettlementUseCase {

    private final SettlementRepository settlementRepository;
    private final SettlementOutboxRepository settlementOutboxRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public SettlementEntity execute(SettlementRequestDto input) {
        return settlementRepository.findByPaymentId(input.paymentId())
                .orElseGet(() -> createSettlement(input));
    }

    private SettlementEntity createSettlement(SettlementRequestDto input) {
        SettlementEntity settlement = SettlementEntity.builder()
                .sagaId(ofNullable(input.sagaId()))
                .paymentId(input.paymentId())
                .orderId(input.orderId())
                .amount(input.amount())
                .fee(input.fee())
                .settlementAccount(input.settlementAccount())
                .merchantPayoutAmount(BigDecimal.ZERO)
                .platformFeeAmount(BigDecimal.ZERO)
                .status(SettlementStatus.PENDING)
                .build();

        SettlementEntity persisted = settlementRepository.save(settlement);
        createOutboxEvent(persisted, EventType.SUCCESS);

        return persisted;
    }

    private void createOutboxEvent(SettlementEntity settlement, EventType eventType) {
        try {
            String payload = objectMapper.writeValueAsString(settlement);

            SettlementOutboxEntity event = SettlementOutboxEntity.builder()
                    .sagaId(settlement.getSagaId())
                    .eventType(eventType)
                    .status(OutboxStatus.PENDING)
                    .payload(payload)
                    .createdAt(LocalDateTime.now())
                    .build();

            settlementOutboxRepository.save(event);
        } catch (JsonProcessingException e) {
            throw new UseCase.Exception("Failed to create settlement outbox payload: " + e.getMessage());
        }
    }

    private UUID ofNullable(UUID sagaId) {
        return sagaId != null ? sagaId : UUID.randomUUID();
    }
}

