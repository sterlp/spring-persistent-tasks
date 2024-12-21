package org.sterl.spring.persistent_tasks.history.api;

import org.sterl.spring.persistent_tasks.api.Trigger;
import org.sterl.spring.persistent_tasks.history.model.LastTriggerStateEntity;
import org.sterl.spring.persistent_tasks.history.model.TriggerStateDetailEntity;
import org.sterl.spring.persistent_tasks.shared.ExtendetConvert;
import org.sterl.spring.persistent_tasks.trigger.api.TriggerConverter.ToTrigger;
import org.sterl.spring.persistent_tasks.trigger.component.StateSerializer;

public interface HistoryConverter {

    StateSerializer SERIALIZER = new StateSerializer();
    
    enum FromLastTriggerStateEntity implements ExtendetConvert<LastTriggerStateEntity, Trigger> {

        INSTANCE;

        @Override
        public Trigger convert(LastTriggerStateEntity source) {
            var result = ToTrigger.INSTANCE.convert(source.getData());
            result.setId(source.getId());
            return result;
        }
    }
    
    enum FromTriggerStateDetailEntity implements ExtendetConvert<TriggerStateDetailEntity, Trigger> {

        INSTANCE;

        @Override
        public Trigger convert(TriggerStateDetailEntity source) {
            var result = ToTrigger.INSTANCE.convert(source.getData());
            result.setId(source.getId());
            result.setInstanceId(source.getInstanceId());
            return result;
        }
    }
}
