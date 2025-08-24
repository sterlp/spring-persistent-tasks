package org.sterl.spring.persistent_tasks.shared;

import java.time.OffsetDateTime;

public class DateUtil {

    public static long secondsBeetween(OffsetDateTime start, long secondsEnd) {
        if (start == null) return 0;
        return secondsEnd - start.toEpochSecond();
    }
}
