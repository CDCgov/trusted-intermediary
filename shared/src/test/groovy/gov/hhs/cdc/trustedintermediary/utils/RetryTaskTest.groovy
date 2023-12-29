package gov.hhs.cdc.trustedintermediary.utils

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import spock.lang.Specification

import java.util.concurrent.Callable
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class RetryTaskTest extends Specification {
    ScheduledExecutorService mockScheduler

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(RetryTask, RetryTask.getInstance())
        mockScheduler = Mock(ScheduledExecutorService)
        mockScheduler.schedule(_, _, _) >> { Runnable task, long delay, TimeUnit unit ->
            task.run()
            return Mock(ScheduledFuture)
        }
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "scheduleRetry should retry the task until it succeeds"() {
        given:
        def maxRetries = 3

        RetryTask retryTask = RetryTask.getInstance()
        retryTask.MAX_RETRIES = maxRetries
        retryTask.scheduler = mockScheduler

        Callable<Void> mockTask = Mock(Callable)

        when:
        retryTask.scheduleRetry(mockTask, 1, 1000)

        then:
        (1..maxRetries - 1) * mockTask.call() >> { throw new Exception("Fail") }
        1 * mockTask.call() >> null // Succeeds on the last attempt
        1 * mockScheduler.shutdown()
    }

    def "scheduleRetry should give up after max retries"() {
        given:
        def maxRetries = 3

        RetryTask retryTask = RetryTask.getInstance()
        retryTask.MAX_RETRIES = maxRetries
        retryTask.scheduler = mockScheduler

        Callable<Void> mockTask = Mock(Callable)

        when:
        retryTask.scheduleRetry(mockTask, 1, 1000)

        then:
        maxRetries * mockTask.call() >> { throw new Exception("Fail") }
        1 * mockScheduler.shutdown()
    }
}
