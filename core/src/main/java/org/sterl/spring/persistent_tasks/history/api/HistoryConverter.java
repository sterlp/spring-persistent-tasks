package org.sterl.spring.persistent_tasks.history.api;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.sterl.spring.persistent_tasks.api.Trigger;
import org.sterl.spring.persistent_tasks.api.HistoryTrigger;
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
    
    enum ToHistoryTrigger implements ExtendetConvert<HistoryTriggerEntity, HistoryTrigger> {
        INSTANCE;

        @Override
        @Nullable
        public HistoryTrigger convert(@NonNull HistoryTriggerEntity source) {
            var result = new HistoryTrigger();
            result.setCreatedTime(source.getCreatedTime());
            result.setExecutionCount(source.getExecutionCount());
            result.setId(source.getId());
            result.setInstanceId(source.getInstanceId());
            result.setKey(source.getKey());
            result.setMessage(source.getMessage());
            result.setStart(source.getStart());
            result.setStatus(source.getStatus());
            return result;
        }
        
    }
}
