package org.sterl.spring.persistent_tasks.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.assertj.core.api.ListAssert;

import lombok.Getter;
import lombok.Setter;

public class AsyncAsserts {

    private final List<String> values = Collections.synchronizedList(new ArrayList<String>());
    private final Map<String, Integer> counts = new ConcurrentHashMap<>();

    @Getter @Setter
    private int maxStepCount = 100;
    
    @Getter @Setter
    private Duration defaultTimeout = Duration.ofSeconds(3);
    
    public synchronized void clear() {
        values.clear();
        counts.clear();
    }
    public synchronized int add(String value) {
        values.add(value);
        final int count = getCount(value) + 1;
        counts.put(value, count);
        if (values.size() > maxStepCount) {
            throw new IllegalStateException("Flow has already more than " + maxStepCount + " steps, assuming error!");
        }
        return count;
    }
    /**
     * @return how often this value has been already added ...
     */
    public int info(String value) {
        if (value == null) {
            value= "[null]";
        }
        int count;
        int size;
        synchronized (values) {
            count = this.add(value);
            size = values.size();
        }
        System.err.println(size + ". " + value);
        return count;
    }
    public int getCount() {
        return counts.size();
    }
    
    public int getCount(String value) {
        return counts.getOrDefault(value, 0);
    }
    public void awaitValue(String value) {
        awaitValue(null, value);
    }
    /**
     * Wait for the given value, if not found call the given method
     * @param fn the optional function to call after each wait
     * @param value the value to wait for
     */
    public void awaitValue(Runnable fn, String value) {
        final var start = System.currentTimeMillis();
        while (!values.contains(value)
                && (System.currentTimeMillis() - start <= defaultTimeout.toMillis())) {
            try {
                Thread.sleep(50);
                if (fn != null) fn.run();
            } catch (InterruptedException e) {
                if (Thread.interrupted()) break;
            }
        }
        assertValue(value);
    }
    /**
     * Wait for the given value, if not found call the given method
     * @param fn the optional function to call after each wait
     * @param value the value to wait for
     */
    public void awaitValue(Runnable fn, String value, String... values) {
        awaitValue(fn, value);
        if (values != null && values.length > 0) {
            for (String v : values) {
                awaitValue(fn, v);
            }
        }
    }
    public void awaitValue(String value, String... values) {
        awaitValue(null, value, values);
    }
    public void awaitOrdered(String value, String... values) {
        awaitOrdered(null, value, values);
    }
    public void awaitOrdered(Runnable fn, String value, String... values) {
        awaitValue(fn, value, values);

        assertThat(this.values.indexOf(value)).isEqualTo(0);
        if (values != null && values.length > 0) {
            for (int i = 0; i < values.length; i++) {
                assertThat(this.values.indexOf(values[i])).isEqualTo(i + 1);
            }
        }
    }
    
    public ListAssert<String> assertValue(String value) {
        return assertThat(new ArrayList<>(values)).contains(value);
    }
    
    public void assertMissing(String value) {
        assertThat(values).doesNotContain(value);
    }
    public void assertMissing(String value, String... inValues) {
        assertThat(values).doesNotContain(value);
        for (String s : inValues) {
            assertThat(values).doesNotContain(s);
        }
    }
    
    public void awaitValueOnce(String value) {
        awaitValue(null, value);
        assertThat(values).contains(value);
        var occurrences = values.stream().filter(e -> value.equals(e)).count();
        if (occurrences > 1) {
            fail("Expected " + value + " to be present once but was present " + occurrences + " times.");
        }
    }
}
