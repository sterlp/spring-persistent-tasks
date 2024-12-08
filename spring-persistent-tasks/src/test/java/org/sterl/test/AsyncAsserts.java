package org.sterl.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.assertj.core.api.ListAssert;

import lombok.Setter;

public class AsyncAsserts {

    private final List<String> values = Collections.synchronizedList(new ArrayList<String>());
    private final Map<String, Integer> counts = new ConcurrentHashMap<>();
    @Setter
    private Duration defaultTimeout = Duration.ofSeconds(5);

    public synchronized void clear() {
        values.clear();
        counts.clear();
    }

    public int add(String value) {
        values.add(value);
        final int count = getCount(value) + 1;
        counts.put(value, count);
        if (values.size() > 100) {
            throw new IllegalStateException("Workflow has already more than 100 steps, assuming error!");
        }
        return count;
    }

    /**
     * @return how often this value has been already added ...
     */
    public int info(String value) {
        if (value == null) value= "[null]";
        int count;
        int size;
        synchronized (values) {
            count = this.add(value);
            size = values.size();
        }
        System.err.println(size + ". " + value);
        return count;
    }

    public int getCount(String value) {
        return counts.getOrDefault(value, 0);
    }

    public void awaitValue(String value) {
        final var start = Instant.now();
        while (!values.contains(value)
                && (System.currentTimeMillis() - start.toEpochMilli() <= defaultTimeout.toMillis())) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                if (Thread.interrupted()) break;
            }
        }
        assertValue(value);
    }

    public ListAssert<String> assertValue(String value) {
        return assertThat(new ArrayList<>(values)).contains(value);
    }

    public void awaitValue(String value, String... values) {
        awaitValue(value);
        if (values != null && values.length > 0) {
            for (String v : values) {
                awaitValue(v);
            }
        }
    }
    
    public void awaitValueOnce(String value) {
        awaitValue(value);
        assertThat(values).contains(value);
        var occurrences = values.stream().filter(e -> value.equals(e)).count();
        if (occurrences > 1) {
            fail("Expected " + value + " to be present once but was present " + occurrences + " times.");
        }
    }

    public void awaitOrdered(String value, String... values) {
        awaitValue(value, values);

        assertThat(this.values.indexOf(value)).isEqualTo(0);
        if (values != null && values.length > 0) {
            for (int i = 0; i < values.length; i++) {
                assertThat(this.values.indexOf(values[i])).isEqualTo(i + 1);
            }
        }
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
}
