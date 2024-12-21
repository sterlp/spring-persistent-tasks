package org.sterl.spring.persistent_tasks.trigger.api;

import org.sterl.spring.persistent_tasks.api.Trigger;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryEntity;
import org.sterl.spring.persistent_tasks.shared.ExtendetConvert;
import org.sterl.spring.persistent_tasks.shared.model.TriggerData;
import org.sterl.spring.persistent_tasks.trigger.component.StateSerializer;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

public class TriggerConverter {
    private final static StateSerializer SERIALIZER = new StateSerializer();
    
    public enum FromTriggerHistoryEntity implements ExtendetConvert<TriggerHistoryEntity, Trigger> {

        INSTANCE;

        @Override
        public Trigger convert(TriggerHistoryEntity source) {
            var result = ToTriggerView.INSTANCE.convert(source.getData());
            result.setId(source.getId());
            result.setInstanceId(source.getInstanceId());
            return result;
        }
    }
    
    enum FromTriggerEntity implements ExtendetConvert<TriggerEntity, Trigger> {

        INSTANCE;

        @Override
        public Trigger convert(TriggerEntity source) {
            var result = ToTriggerView.INSTANCE.convert(source.getData());
            result.setId(source.getId());
            result.setInstanceId(source.getId());
            result.setRunningOn(source.getRunningOn());
            return result;
        }
    }

    private enum ToTriggerView implements ExtendetConvert<TriggerData, Trigger> {

        INSTANCE;

        @Override
        public Trigger convert(TriggerData source) {
            var result = new Trigger();
            
            result.setKey(source.getKey());
            result.setCreatedTime(source.getCreatedTime());
            result.setEnd(source.getEnd());
            result.setExceptionName(source.getExceptionName());
            result.setExecutionCount(source.getExecutionCount());
            result.setLastException(source.getLastException());
            result.setPriority(source.getPriority());
            result.setRunAt(source.getRunAt());
            result.setRunningDurationInMs(source.getRunningDurationInMs());
            result.setStart(source.getStart());
            result.setState(SERIALIZER.deserialize(source.getState()));
            result.setStatus(source.getStatus());


            return result;
        }
    }
}
