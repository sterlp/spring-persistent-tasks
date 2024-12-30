package org.sterl.spring.persistent_tasks.history.api;

import org.springframework.lang.NonNull;
import org.sterl.spring.persistent_tasks.api.Trigger;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryDetailEntity;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryLastStateEntity;
import org.sterl.spring.persistent_tasks.shared.ExtendetConvert;
import org.sterl.spring.persistent_tasks.shared.converter.ToTrigger;

interface HistoryConverter {

    enum FromLastTriggerStateEntity implements ExtendetConvert<TriggerHistoryLastStateEntity, Trigger> {
        INSTANCE;

        @NonNull
        @Override
        public Trigger convert(@NonNull TriggerHistoryLastStateEntity source) {
            var result = ToTrigger.INSTANCE.convert(source);
            result.setId(source.getId());
            result.setInstanceId(source.getId());
            return result;
        }
    }
    
    enum FromTriggerStateDetailEntity implements ExtendetConvert<TriggerHistoryDetailEntity, Trigger> {
        INSTANCE;

        @NonNull
        @Override
        public Trigger convert(@NonNull TriggerHistoryDetailEntity source) {
            var result = ToTrigger.INSTANCE.convert(source);
            result.setId(source.getId());
            result.setInstanceId(source.getInstanceId());
            return result;
        }
    }
}
