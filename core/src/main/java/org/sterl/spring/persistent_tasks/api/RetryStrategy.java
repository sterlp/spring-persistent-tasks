package org.sterl.spring.persistent_tasks.api;

import java.time.OffsetDateTime;

import org.springframework.lang.Nullable;

@FunctionalInterface
public interface RetryStrategy {
    RetryStrategy NO_RETRY = (c, e) -> false;
    RetryStrategy THREE_RETRIES = (c, e) -> c < 3;
    RetryStrategy THREE_RETRIES_IMMEDIATELY = new RetryStrategy() {
        @Override
        public boolean shouldRetry(int executionCount, Exception error) {
            return executionCount < 3;
        }
        @Override
        public OffsetDateTime retryAt(int executionCount, Exception error) {
            return OffsetDateTime.now();
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
        return OffsetDateTime.now().plusMinutes(1 + executionCount);
    }
}
