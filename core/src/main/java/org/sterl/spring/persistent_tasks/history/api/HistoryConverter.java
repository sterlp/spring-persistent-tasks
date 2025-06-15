package org.sterl.spring.persistent_tasks.history.api;

import org.springframework.lang.NonNull;
import org.sterl.spring.persistent_tasks.api.Trigger;
import org.sterl.spring.persistent_tasks.history.model.CompletedTriggerEntity;
import org.sterl.spring.persistent_tasks.history.model.HistoryTriggerEntity;
import org.sterl.spring.persistent_tasks.shared.ExtendetConvert;
import org.sterl.spring.persistent_tasks.shared.converter.ToTrigger;

interface HistoryConverter {

    enum FromLastTriggerStateEntity implements ExtendetConvert<CompletedTriggerEntity, Trigger> {
        INSTANCE;

        @NonNull
        @Override
        public Trigger convert(@NonNull CompletedTriggerEntity source) {
            var result = ToTrigger.INSTANCE.convert(source);
            result.setId(source.getId());
            result.setInstanceId(source.getId());
            return result;
        }
    }
    
    enum FromTriggerStateDetailEntity implements ExtendetConvert<HistoryTriggerEntity, Trigger> {
        INSTANCE;

        @NonNull
        @Override
        public Trigger convert(@NonNull HistoryTriggerEntity source) {
            var result = ToTrigger.INSTANCE.convert(source);
            result.setId(source.getId());
            result.setInstanceId(source.getInstanceId());
            return result;
        }
    }
}
