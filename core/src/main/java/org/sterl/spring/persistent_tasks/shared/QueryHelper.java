package org.sterl.spring.persistent_tasks.shared;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;

public class QueryHelper {

    @Nullable
    public static <T> Predicate eq(@NonNull SimpleExpression<T> path, @Nullable T value) {
        if (value == null) return null;
        return path.eq(value);
    }
    @Nullable
    public static Predicate eqOrLike(@NonNull StringPath path, @Nullable String value) {
        if (value == null) return null;
        if (StringHelper.isSqlSearch(value)) {
            return path.like(StringHelper.applySearchWildCard(value));
        } else {
            return path.eq(value);
        }
     }
}
