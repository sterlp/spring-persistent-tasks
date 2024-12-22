package org.sterl.spring.persistent_tasks.shared.converter;

import org.sterl.spring.persistent_tasks.api.Trigger;
import org.sterl.spring.persistent_tasks.shared.ExtendetConvert;
import org.sterl.spring.persistent_tasks.shared.model.HasTriggerData;
import org.sterl.spring.persistent_tasks.trigger.component.StateSerializer;

public enum ToTrigger implements ExtendetConvert<HasTriggerData, Trigger> {
    INSTANCE;

    private final static StateSerializer SERIALIZER = new StateSerializer();

    @Override
    public Trigger convert(HasTriggerData hasData) {
        final var source = hasData.getData();
        final var result = new Trigger();
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