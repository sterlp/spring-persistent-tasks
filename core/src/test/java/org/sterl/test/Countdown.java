package org.sterl.test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.awaitility.Awaitility;

public class Countdown {

    private final AtomicInteger count = new AtomicInteger(1);
    
    public void await() {
        Awaitility
            .await("Countdown")
            .atMost(Duration.ofSeconds(3))
            .until(() -> count.get() <= 0);
    }
    
    public void countDown() {
        count.decrementAndGet();
    }

    public void reset() {
        count.set(1);
    }
}
