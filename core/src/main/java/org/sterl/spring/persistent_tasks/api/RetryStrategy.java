package org.sterl.spring.persistent_tasks.api;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import org.springframework.lang.Nullable;

import lombok.RequiredArgsConstructor;

@FunctionalInterface
public interface RetryStrategy {
    RetryStrategy NO_RETRY = (c, e) -> false;
    /**
     * One initial execution and after that we will try it 3 more times. Overall 4 executions.
     */
    RetryStrategy THREE_RETRIES = new LinearRetryStrategy(4, ChronoUnit.MINUTES, 1);
    /**
     * One initial execution and after that we will try it 3 more times. Overall 4 executions.
     */
    RetryStrategy THREE_RETRIES_IMMEDIATELY = new MultiplicativeRetryStrategy(4, ChronoUnit.MILLIS, 0);


    /**
     * Determines whether a retry should be attempted based on the current
     * execution count and the provided exception.
     *
     * @param executionCount The number of attempts already made.
     * @param error The exception that triggered the retry.
     * @return {@code true} if the current execution count is less than
     *         the maximum execution count; {@code false} otherwise.
     */
    boolean shouldRetry(int executionCount, @Nullable Exception error);

    /**
     * Calculates the time of the next retry attempt based on the current
     * execution count and the provided exception.
     *
     * @param executionCount The number of attempts already made.
     * @param exception The exception that triggered the retry.
     * @return The {@link OffsetDateTime} representing the time of the next retry attempt.
     */
    default OffsetDateTime retryAt(int executionCount, @Nullable Exception exception) {
        return OffsetDateTime.now().plusMinutes(executionCount);
    }


    // Default implementations
    /**
     * A retry strategy that determines the next retry time by adding a fixed
     * offset and the execution count to the current time in the specified temporal unit.
     * 
     * <p>This strategy can be used to create retry intervals that increase linearly
     * with the number of attempts, providing a predictable delay pattern.</p>
     *
     * <p>Example:
     * If {@code offset = 5}, {@code unit = ChronoUnit.SECONDS}, and
     * {@code executionCount = 3}, the next retry will be scheduled after
     * {@code 5 + 3 = 8 seconds} from the current time.</p>
     *
     * <p>Note: The retry attempts will stop once the maximum execution count
     * ({@code maxExecutionCount}) is reached.</p>
     *
     * @author Your Name
     */
    @RequiredArgsConstructor
    class LinearRetryStrategy implements RetryStrategy {
        private final int maxExecutionCount;
        private final TemporalUnit unit;
        private final int offset;

        @Override
        public boolean shouldRetry(int executionCount, Exception error) {
            return maxExecutionCount > executionCount;
        }
        @Override
        public OffsetDateTime retryAt(int executionCount, Exception error) {
            return OffsetDateTime.now().plus(offset + executionCount, unit);
        }
    }

    /**
     * A retry strategy that determines the next retry time by multiplying
     * the execution count by a scaling factor and adding the result to the
     * current time in the specified temporal unit.
     * 
     * <p>This strategy can be used to create retry intervals that increase
     * multiplicatively with the number of attempts, providing a way to progressively
     * delay retries.</p>
     *
     * <p>Example:
     * If {@code scalingFactor = 2}, {@code unit = ChronoUnit.SECONDS}, and
     * {@code executionCount = 3}, the next retry will be scheduled after
     * {@code 2 * 3 = 6 seconds} from the current time.</p>
     *
     * <p>Note: The retry attempts will stop once the maximum execution count
     * ({@code maxExecutionCount}) is reached.</p>
     */
    @RequiredArgsConstructor
    class MultiplicativeRetryStrategy implements RetryStrategy {
        private final int maxExecutionCount;
        private final TemporalUnit unit;
        private final int scalingFactor;

        @Override
        public boolean shouldRetry(int executionCount, Exception error) {
            return maxExecutionCount > executionCount;
        }
        @Override
        public OffsetDateTime retryAt(int executionCount, Exception error) {
            return OffsetDateTime.now().plus(scalingFactor * executionCount, unit);
        }
    }
    
    /**
     * A retry strategy that determines the next retry time by adding a fixed
     * interval to the current time in the specified temporal unit.
     * 
     * <p>This strategy can be used to create retry intervals that remain constant
     * regardless of the number of attempts, providing a uniform delay between retries.</p>
     *
     * <p>Example:
     * If {@code interval = 5}, {@code unit = ChronoUnit.SECONDS}, each retry will
     * be scheduled after 5 seconds from the current time.</p>
     *
     * <p>Note: The retry attempts will stop once the maximum execution count
     * ({@code maxExecutionCount}) is reached.</p>
     */
    @RequiredArgsConstructor
    class FixedIntervalRetryStrategy implements RetryStrategy {
        private final int maxExecutionCount;
        private final TemporalUnit unit;
        private final int interval;

        @Override
        public boolean shouldRetry(int executionCount, Exception error) {
            return maxExecutionCount > executionCount;
        }
        @Override
        public OffsetDateTime retryAt(int executionCount, Exception error) {
            return OffsetDateTime.now().plus(interval, unit);
        }
    }
}
