package org.sterl.spring.persistent_tasks.history.api;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.sterl.spring.persistent_tasks.api.HistoryTrigger;
import org.sterl.spring.persistent_tasks.history.model.HistoryTriggerEntity;
import org.sterl.spring.persistent_tasks.shared.ExtendetConvert;

interface HistoryConverter {

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
