package org.sterl.spring.persistent_tasks.shared;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@FunctionalInterface
public interface ExtendetConvert<S, T> 
    extends Converter<S, T>, Function<S, T> {
    
    @Nullable
    default T apply(@Nullable S s) {
        if (s == null) return null;
        return convert(s);
    }
    
    @NonNull
    default Optional<T> convert(Optional<S> s) {
        if (s.isEmpty()) return Optional.empty();
        return Optional.of(convert(s.get()));
    }

    @NonNull
    default List<T> convert(Collection<S> source) {
        return source.stream().map(this::convert).toList();
    }

    @NonNull
    default PagedModel<T> toPage(@NonNull Page<S> page) {
        return new PagedModel<>(page.map(this::apply));
    }
}
