package org.sterl.spring.persistent_tasks.shared;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;

public class QueryHelper {
    
    public static Pageable applySortIfEmpty(Pageable page, Sort sort) {
        Pageable result = page;
        if (page.getSort() == null || page.getSort() == Sort.unsorted()) {
            result = PageRequest.of(page.getPageNumber(), page.getPageSize(), sort);
        }
        return result;
    }

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
