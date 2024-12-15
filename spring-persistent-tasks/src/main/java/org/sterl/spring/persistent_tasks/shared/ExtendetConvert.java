package org.sterl.spring.persistent_tasks.shared;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;

@FunctionalInterface
public interface ExtendetConvert<S, T> 
    extends Converter<S, T>, Function<S, T> {
    
    default T apply(S s) {
        return convert(s);
    }

    default List<T> convert(Collection<S> source) {
        return source.stream().map(this::convert).toList();
    }

    default PagedModel<T> toPage(Page<S> page) {
        return new PagedModel<>(page.map(this::apply));
    }
}
