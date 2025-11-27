package paycore.paycore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import paycore.paycore.domain.OutboxStatus;
import paycore.paycore.domain.SettlementStatus;
import paycore.paycore.dto.SettlementRequestDto;
import paycore.paycore.entity.SettlementEntity;
import paycore.paycore.entity.SettlementOutboxEntity;
import paycore.paycore.repository.SettlementOutboxRepository;
import paycore.paycore.repository.SettlementRepository;
import paycore.paycore.service.SettlementService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringBatchTest
@AutoConfigureTestDatabase
@TestPropertySource(properties = {
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.rabbitmq.listener.direct.auto-startup=false"
})
class SettlementIntegrationTest {

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private SettlementOutboxRepository settlementOutboxRepository;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job settlementJob;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(settlementJob);
        settlementOutboxRepository.deleteAll();
        settlementRepository.deleteAll();
    }

    @Test
    void createSettlement_persistsPendingWithZeroAmounts() {
        SettlementRequestDto request = new SettlementRequestDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "apikey",
                new BigDecimal("5.00"),
                new BigDecimal("5.00")
        );

        SettlementEntity settlement = settlementService.execute(request);

        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.PENDING);
        assertThat(settlement.getMerchantPayoutAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(settlement.getPlatformFeeAmount()).isEqualByComparingTo(BigDecimal.ZERO);

        SettlementEntity persisted = settlementRepository.findById(settlement.getId()).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(SettlementStatus.PENDING);
        assertThat(persisted.getMerchantPayoutAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(persisted.getPlatformFeeAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void batchJobCompletesPendingSettlement() throws Exception {
        SettlementEntity pending = settlementRepository.save(
                SettlementEntity.builder()
                        .sagaId(UUID.randomUUID())
                        .paymentId(UUID.randomUUID())
                        .amount(new BigDecimal("150.00"))
                        .fee(new BigDecimal("12.50"))
                        .apiKey("apikey")
                        .merchantPayoutAmount(BigDecimal.ZERO)
                        .platformFeeAmount(BigDecimal.ZERO)
                        .status(SettlementStatus.PENDING)
                        .build());

        jobLauncherTestUtils.launchJob();

        SettlementEntity updated = settlementRepository.findById(pending.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(SettlementStatus.COMPLETED);
        assertThat(updated.getMerchantPayoutAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(updated.getPlatformFeeAmount()).isEqualByComparingTo(new BigDecimal("12.50"));

        List<SettlementOutboxEntity> outboxEntries = settlementOutboxRepository.findAll();
        assertThat(outboxEntries).hasSize(1);
        assertThat(outboxEntries.get(0).getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(outboxEntries.get(0).getSagaId()).isEqualTo(updated.getSagaId());
    }
}

