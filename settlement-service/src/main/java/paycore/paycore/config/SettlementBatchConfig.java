package paycore.paycore.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class SettlementBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final Tasklet settlementBatchTasklet;

    @Bean
    public Job settlementJob(Step settlementStep) {
        return new JobBuilder("settlementJob", jobRepository)
                .start(settlementStep)
                .build();
    }

    @Bean
    public Step settlementStep() {
        return new StepBuilder("settlementStep", jobRepository)
                .tasklet(settlementBatchTasklet, transactionManager)
                .build();
    }
}

