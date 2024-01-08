package gov.hhs.cdc.trustedintermediary.utils

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import spock.lang.Specification

import java.util.concurrent.Callable

class SyncRetryTaskTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(SyncRetryTask, SyncRetryTask.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "scheduleRetry should retry the task until it succeeds"() {
        given:
        def maxRetries = 3
        def waitTime = 10
        Callable<Void> mockTask = Mock(Callable)

        when:
        def result = SyncRetryTask.getInstance().retry(mockTask, maxRetries, waitTime)

        then:
        (1..maxRetries - 1) * mockTask.call() >> { throw new Exception("Fail") }
        1 * mockTask.call() >> null // Succeeds on the last attempt
        result == null
    }

    def "scheduleRetry should give up after max retries and throw RetryFailedException"() {
        given:
        def maxRetries = 3
        def waitTime = 10
        Callable<Void> mockTask = Mock(Callable)

        when:
        SyncRetryTask.getInstance().retry(mockTask, maxRetries, waitTime)

        then:
        maxRetries * mockTask.call() >> { throw new Exception("Fail") }
        def exception = thrown(RetryFailedException)
        exception.getCause().getClass() == Exception
    }

    def "should handle thread interruption"() {
        given:
        def callable = Mock(Callable)
        callable.call() >> { throw new Exception("Fail") }
        Exception thrown
        def syncRetryTask = SyncRetryTask.getInstance()

        when:
        def thread = new Thread({
            try {
                syncRetryTask.retry(callable, 3, 1000)
            } catch (RetryFailedException e) {
                thrown = e
            }
        })
        thread.start()
        Thread.sleep(500)
        thread.interrupt()
        thread.join()

        then:
        thrown.cause instanceof InterruptedException
    }
}
