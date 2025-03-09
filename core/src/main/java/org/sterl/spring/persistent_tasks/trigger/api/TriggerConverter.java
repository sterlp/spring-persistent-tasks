package org.sterl.spring.persistent_tasks.trigger.api;

import org.sterl.spring.persistent_tasks.api.Trigger;
import org.sterl.spring.persistent_tasks.shared.ExtendetConvert;
import org.sterl.spring.persistent_tasks.shared.converter.ToTrigger;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

public class TriggerConverter {
    
    public enum FromTriggerEntity implements ExtendetConvert<TriggerEntity, Trigger> {
        INSTANCE;

        @Override
        public Trigger convert(TriggerEntity source) {
            var result = ToTrigger.INSTANCE.convert(source);
            result.setId(source.getId());
            result.setInstanceId(source.getId());
            result.setRunningOn(source.getRunningOn());
            return result;
        }
    }
}
