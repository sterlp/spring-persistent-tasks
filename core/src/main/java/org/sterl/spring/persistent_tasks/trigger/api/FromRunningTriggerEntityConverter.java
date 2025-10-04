package org.sterl.spring.persistent_tasks.trigger.api;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.sterl.spring.persistent_tasks.api.Trigger;
import org.sterl.spring.persistent_tasks.shared.ExtendetConvert;
import org.sterl.spring.persistent_tasks.shared.converter.ToTrigger;
import org.sterl.spring.persistent_tasks.trigger.model.RunningTriggerEntity;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FromRunningTriggerEntityConverter implements ExtendetConvert<RunningTriggerEntity, Trigger> {
    
    private final ToTrigger toTrigger;
    
    @Override
    public Trigger convert(@NonNull RunningTriggerEntity source) {
        var result = toTrigger.convert(source);
        result.setId(source.getId());
        result.setInstanceId(source.getId());
        result.setRunningOn(source.getRunningOn());
        result.setLastPing(source.getLastPing());
        return result;
    }
}
