package gov.hhs.cdc.trustedintermediary.utils;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

public class RetryTask {
    private static final RetryTask INSTANCE = new RetryTask();
    private static final int MAX_RETRIES = 5;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    @Inject Logger logger;

    public static RetryTask getInstance() {
        return INSTANCE;
    }

    private RetryTask() {}

    public <T> void scheduleRetry(Callable<T> task, int attempt, long waitTime) {
        scheduler.schedule(
                () -> {
                    try {
                        task.call();
                        scheduler.shutdown();
                    } catch (Exception e) {
                        if (attempt < MAX_RETRIES) {
                            var nextAttempt = attempt + 1;
                            var nextWaitTime = waitTime * 2;
                            logger.logWarning(
                                    "Attempt "
                                            + nextAttempt
                                            + ": Retrying in "
                                            + nextWaitTime / 1000
                                            + "s");
                            scheduleRetry(task, nextAttempt, nextWaitTime);
                        } else {
                            logger.logError("Max retries reached", e);
                            scheduler.shutdown();
                        }
                    }
                },
                waitTime,
                TimeUnit.MILLISECONDS);
    }
}
