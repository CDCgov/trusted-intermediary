package gov.hhs.cdc.trustedintermediary.utils;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.concurrent.Callable;
import javax.inject.Inject;

/**
 * Provides a reusable utility for retrying a task with a specified number of retries and wait time
 * between retries.
 */
public class SyncRetryTask {
    private static final SyncRetryTask INSTANCE = new SyncRetryTask();
    @Inject Logger logger;

    public static SyncRetryTask getInstance() {
        return INSTANCE;
    }

    private SyncRetryTask() {}

    public <T> T retry(Callable<T> task, int maxRetries, long waitTime)
            throws RetryFailedException {
        Exception lastException = null;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                return task.call();
            } catch (Exception e) {
                lastException = e;
                attempt++;

                logger.logWarning("Attempt {}: Retrying in {}s", attempt, waitTime * 2 / 1000);

                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    throw new RetryFailedException("Thread interrupted during retries", ie);
                }

                waitTime *= 2;
            }
        }

        throw new RetryFailedException("Failed after " + maxRetries + " retries", lastException);
    }
}
