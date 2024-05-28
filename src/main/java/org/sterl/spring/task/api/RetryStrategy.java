package org.sterl.spring.task.api;

import java.time.OffsetDateTime;

@FunctionalInterface
public interface RetryStrategy {
    RetryStrategy NO_RETRY = (c, e) -> false;
    RetryStrategy TRY_THREE_TIMES = (c, e) -> c < 3;
    RetryStrategy TRY_THREE_TIMES_IMMEDIATELY = new RetryStrategy() {
        @Override
        public boolean shouldRetry(int executionCount, Exception error) {
            return executionCount < 3;
        }
        @Override
        public OffsetDateTime retryAt(int executionCount, Exception error) {
            return OffsetDateTime.now();
        }
    };

    boolean shouldRetry(int executionCount, Exception error);
    
    /**
     * By default a linear retry strategy, adding one minute for each failed try.
     */
    default OffsetDateTime retryAt(int executionCount, Exception error) {
        return OffsetDateTime.now().plusMinutes(1 + executionCount);
    }
}
