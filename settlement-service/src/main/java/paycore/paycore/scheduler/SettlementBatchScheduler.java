package paycore.paycore.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job settlementJob;

    @Scheduled(cron = "${settlement.batch.cron:0 13 21 * * *}",
            zone = "Asia/Seoul")
    public void runSettlementJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(settlementJob, jobParameters);
            log.info("Settlement batch job triggered successfully");
        } catch (Exception e) {
            log.error("Failed to run settlement batch job", e);
        }
    }
}

