package org.sterl.spring.persistent_tasks.api;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.lang.Nullable;

@FunctionalInterface
public interface RetryStrategy {
    RetryStrategy NO_RETRY = (c, e) -> false;
    /**
     * One initial execution and after that we will try it 3 more times. Overall 4 executions.
     */
    RetryStrategy THREE_RETRIES = (c, e) -> c < 4;
    /**
     * One initial execution and after that we will try it 3 more times. Overall 4 executions.
     */
    RetryStrategy THREE_RETRIES_IMMEDIATELY = new RetryStrategy() {
        @Override
        public boolean shouldRetry(int executionCount, Exception error) {
            return executionCount < 4;
        }
        @Override
        public OffsetDateTime retryAt(int executionCount, Exception error) {
            return OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        }
    };

    /**
     * Check if a retry should be done.
     *
     * @param executionCount 0 based counter how often the execution was tried
     * @param error the exception, <code>null</code> on a timeout
     */
    boolean shouldRetry(int executionCount, @Nullable Exception error);

    /**
     * By default a linear retry strategy, adding one minute for each failed try.
     *
     * @param executionCount 0 based counter how often the execution was tried
     * @param error the exception, <code>null</code> on a timeout
     */
    default OffsetDateTime retryAt(int executionCount, @Nullable Exception error) {
        return OffsetDateTime.now().plusMinutes(1 + executionCount).truncatedTo(ChronoUnit.SECONDS);
    }
}
