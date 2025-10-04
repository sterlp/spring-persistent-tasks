package org.sterl.spring.persistent_tasks.history.api;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.sterl.spring.persistent_tasks.api.Trigger;
import org.sterl.spring.persistent_tasks.history.model.CompletedTriggerEntity;
import org.sterl.spring.persistent_tasks.shared.ExtendetConvert;
import org.sterl.spring.persistent_tasks.shared.converter.ToTrigger;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FromCompletedTriggerEntityConverter implements ExtendetConvert<CompletedTriggerEntity, Trigger> {

    private final ToTrigger toTrigger;

    @NonNull
    @Override
    public Trigger convert(@NonNull CompletedTriggerEntity source) {
        var result = toTrigger.convert(source);
        result.setId(source.getId());
        result.setInstanceId(source.getId());
        return result;
    }
}
