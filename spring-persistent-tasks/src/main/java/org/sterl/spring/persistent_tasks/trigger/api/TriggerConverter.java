package org.sterl.spring.persistent_tasks.trigger.api;

import org.sterl.spring.persistent_tasks.api.TriggerView;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryEntity;
import org.sterl.spring.persistent_tasks.shared.ExtendetConvert;
import org.sterl.spring.persistent_tasks.shared.model.TriggerData;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

class TriggerConverter {
    
    enum FromTriggerHistoryEntity implements ExtendetConvert<TriggerHistoryEntity, TriggerView> {

        INSTANCE;

        @Override
        public TriggerView convert(TriggerHistoryEntity source) {
            var result = ToTriggerView.INSTANCE.convert(source.getData());
            
            result.setId(source.getTriggerId());
            result.setKey(source.getId() + "");

            return result;
        }
    }
    
    enum FromTriggerEntity implements ExtendetConvert<TriggerEntity, TriggerView> {

        INSTANCE;

        @Override
        public TriggerView convert(TriggerEntity source) {
            var result = ToTriggerView.INSTANCE.convert(source.getData());
            
            result.setId(source.getId());
            result.setKey(source.getId().toString());
            result.setRunningOn(source.getRunningOn());

            return result;
        }
    }

    private enum ToTriggerView implements ExtendetConvert<TriggerData, TriggerView> {

        INSTANCE;

        @Override
        public TriggerView convert(TriggerData source) {
            var result = new TriggerView();
            
            result.setCreatedTime(source.getCreatedTime());
            result.setEnd(source.getEnd());
            result.setExceptionName(source.getExceptionName());
            result.setExecutionCount(source.getExecutionCount());
            result.setLastException(source.getLastException());
            result.setPriority(source.getPriority());
            result.setRunAt(source.getRunAt());
            result.setRunningDurationInMs(source.getRunningDurationInMs());
            result.setStart(source.getStart());
            result.setState(source.getState());
            result.setStatus(source.getStatus());

            return result;
        }
    }
}
