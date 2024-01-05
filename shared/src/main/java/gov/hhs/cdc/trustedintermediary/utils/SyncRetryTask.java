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

    public <T> boolean retry(Callable<T> task, int maxRetries, long waitTime) {
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                task.call();
                return true;
            } catch (Exception e) {
                attempt++;

                logger.logWarning(
                        "Attempt " + attempt + ": Retrying in " + (waitTime * 2) / 1000 + "s");

                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    logger.logError("Thread interrupted during wait before retry", ie);
                    break;
                }

                waitTime *= 2;
            }
        }

        logger.logError("Max retries reached, aborting operation.");
        return false;
    }
}
