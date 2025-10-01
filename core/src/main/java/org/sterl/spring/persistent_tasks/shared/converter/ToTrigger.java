package org.sterl.spring.persistent_tasks.shared.converter;

import org.springframework.lang.NonNull;
import org.sterl.spring.persistent_tasks.api.Trigger;
import org.sterl.spring.persistent_tasks.shared.ExtendetConvert;
import org.sterl.spring.persistent_tasks.shared.model.HasTrigger;
import org.sterl.spring.persistent_tasks.shared.model.TriggerEntity;
import org.sterl.spring.persistent_tasks.trigger.component.StateSerializer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum ToTrigger implements ExtendetConvert<HasTrigger, Trigger> {
    INSTANCE;

    private final static StateSerializer SERIALIZER = new StateSerializer();

    @NonNull
    @Override
    public Trigger convert(@NonNull HasTrigger hasData) {
        final TriggerEntity source = hasData.getData();
        final var result = new Trigger();
        result.setKey(source.getKey());
        result.setCorrelationId(source.getCorrelationId());
        result.setTag(source.getTag());
        result.setCreatedTime(source.getCreatedTime());
        result.setEnd(source.getEnd());
        result.setExceptionName(source.getExceptionName());
        result.setExecutionCount(source.getExecutionCount());
        result.setLastException(source.getLastException());
        result.setPriority(source.getPriority());
        result.setRunAt(source.getRunAt());
        result.setRunningDurationInMs(source.getRunningDurationInMs());
        result.setStart(source.getStart());
        try {
            result.setState(SERIALIZER.deserialize(source.getState()));
        } catch (Exception e) {
            var info = """
                    Failed to deserialize state
                    This is most likely due to an incompatible code change.
                    Old states in the DB cannot be read anymore/deserialized and cast to the given class. 
                    """;
            result.setState(new FailedToReadStateInfo(e.getMessage(), info));
            log.warn("""
                    Failed to deserialize state of {}. 
                    This is most likely due to an incompatible code change.
                    Old states in the DB cannot be read anymore/deserialized and cast to the given class. 
                    {}""", 
                    source.getKey(), e.getMessage());
        }
        result.setStatus(source.getStatus());
        return result;
    }
    
    record FailedToReadStateInfo(String message, String info) {}
}