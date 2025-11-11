package paycore.paycore.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskSchedulerManager {
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration ttlMillis) {
        log.info("Task scheduler started for task: {}", task.getClass().getSimpleName());

        return threadPoolTaskScheduler.scheduleAtFixedRate(task, ttlMillis);
    }

    public void cancel(ScheduledFuture<?> future) {
        if (future != null) {
            future.cancel(true);

            log.info("Task scheduler cancelled for task: {}", future.getClass().getSimpleName());
        }
    }
}
