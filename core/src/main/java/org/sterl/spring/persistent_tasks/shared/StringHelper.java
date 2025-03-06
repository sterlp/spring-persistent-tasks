package org.sterl.spring.persistent_tasks.shared;

import org.sterl.spring.persistent_tasks.api.TriggerKey;

public class StringHelper {

    /**
     * Replaces all <code>*</code> with <code>%</code> as needed
     */
    public static String applySearchWildCard(String value) {
        if (value == null || value.length() == 0) return null;
        return value.replace('*', '%');
    }

    /**
     * Replaces all <code>*</code> with <code>%</code> as needed for the id.
     */
    public static String applySearchWildCard(TriggerKey key) {
        if (key == null) return null;
        return applySearchWildCard(key.getId());
    }
}
