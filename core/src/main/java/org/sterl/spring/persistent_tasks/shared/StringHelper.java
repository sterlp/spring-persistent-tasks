package org.sterl.spring.persistent_tasks.shared;

public class StringHelper {

    /**
     * Replaces all <code>*</code> with <code>%</code>, and <code>[</code> with <code>_</code> as needed.
     * <p>
     * first call {@link #isSqlSearch(String)}
     * </p>
     */
    public static String applySearchWildCard(String value) {
        if (value == null || value.length() == 0) return null;
        return value.replace('*', '%').replace('[', '_');
    }
    
    /**
     * Checks if we have a wild card search
     * 
     * first call this method, than 
     * @see #applySearchWildCard(String)
     * 
     */
    public static boolean isSqlSearch(String value) {
        if (value == null) return false;
        return value.indexOf('%') > -1 || value.indexOf('*') > -1;
    }

    public static boolean hasValue(String search) {
        return search != null && search.length() > 0;
    }
}
