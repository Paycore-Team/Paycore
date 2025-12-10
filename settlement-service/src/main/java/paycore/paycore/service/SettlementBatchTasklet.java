package paycore.paycore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import paycore.paycore.domain.EventType;
import paycore.paycore.domain.OutboxStatus;
import paycore.paycore.domain.SettlementStatus;
import paycore.paycore.entity.SettlementEntity;
import paycore.paycore.entity.SettlementOutboxEntity;
import paycore.paycore.repository.SettlementOutboxRepository;
import paycore.paycore.repository.SettlementRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementBatchTasklet implements Tasklet {

    private final SettlementRepository settlementRepository;
    private final SettlementOutboxRepository settlementOutboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<SettlementEntity> pendings = settlementRepository.findAllByStatus(SettlementStatus.PENDING);
        if (pendings.isEmpty()) {
            log.info("No pending settlements to process");
            return RepeatStatus.FINISHED;
        }

        log.info("Processing {} pending settlement(s)", pendings.size());

        pendings.forEach(settlement -> {
            BigDecimal amount = settlement.getAmount() == null ? BigDecimal.ZERO : settlement.getAmount();
            BigDecimal platformFee = settlement.getFee() == null ? BigDecimal.ZERO : settlement.getFee();

            BigDecimal merchantPayout = amount.subtract(platformFee);
            if (merchantPayout.signum() < 0) {
                merchantPayout = BigDecimal.ZERO;
            }

            settlement.markCompleted(merchantPayout, platformFee);
            settlementRepository.save(settlement);
            createOutboxEvent(settlement, EventType.SUCCESS);
        });

        contribution.incrementWriteCount(pendings.size());
        return RepeatStatus.FINISHED;
    }

    private void createOutboxEvent(SettlementEntity settlement, EventType eventType) {
        try {
            String payload = objectMapper.writeValueAsString(settlement);

            SettlementOutboxEntity event = SettlementOutboxEntity.builder()
                    .settlementId(settlement.getId())
                    .sagaId(settlement.getSagaId())
                    .eventType(eventType)
                    .status(OutboxStatus.PENDING)
                    .payload(payload)
                    .createdAt(LocalDateTime.now())
                    .build();

            settlementOutboxRepository.save(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to create settlement outbox payload: " + e.getMessage(), e);
        }
    }
}

