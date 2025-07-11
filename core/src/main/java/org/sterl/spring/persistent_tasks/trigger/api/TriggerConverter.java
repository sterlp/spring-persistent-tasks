package org.sterl.spring.persistent_tasks.trigger.api;

import org.springframework.lang.NonNull;
import org.sterl.spring.persistent_tasks.api.Trigger;
import org.sterl.spring.persistent_tasks.shared.ExtendetConvert;
import org.sterl.spring.persistent_tasks.shared.converter.ToTrigger;
import org.sterl.spring.persistent_tasks.trigger.model.RunningTriggerEntity;

public class TriggerConverter {
    
    public enum FromTriggerEntity implements ExtendetConvert<RunningTriggerEntity, Trigger> {
        INSTANCE;

        @Override
        public Trigger convert(@NonNull RunningTriggerEntity source) {
            var result = ToTrigger.INSTANCE.convert(source);
            result.setId(source.getId());
            result.setInstanceId(source.getId());
            result.setRunningOn(source.getRunningOn());
            result.setLastPing(source.getLastPing());
            return result;
        }
    }
}
