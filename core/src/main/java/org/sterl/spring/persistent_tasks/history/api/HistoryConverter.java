package org.sterl.spring.persistent_tasks.history.api;

import org.sterl.spring.persistent_tasks.api.Trigger;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryLastStateEntity;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryDetailEntity;
import org.sterl.spring.persistent_tasks.shared.ExtendetConvert;
import org.sterl.spring.persistent_tasks.shared.converter.ToTrigger;

interface HistoryConverter {

    enum FromLastTriggerStateEntity implements ExtendetConvert<TriggerHistoryLastStateEntity, Trigger> {
        INSTANCE;

        @Override
        public Trigger convert(TriggerHistoryLastStateEntity source) {
            var result = ToTrigger.INSTANCE.convert(source);
            result.setId(source.getId());
            return result;
        }
    }
    
    enum FromTriggerStateDetailEntity implements ExtendetConvert<TriggerHistoryDetailEntity, Trigger> {
        INSTANCE;

        @Override
        public Trigger convert(TriggerHistoryDetailEntity source) {
            var result = ToTrigger.INSTANCE.convert(source);
            result.setId(source.getId());
            result.setInstanceId(source.getInstanceId());
            return result;
        }
    }
}
