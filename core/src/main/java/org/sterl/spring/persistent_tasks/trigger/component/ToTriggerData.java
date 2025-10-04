package org.sterl.spring.persistent_tasks.trigger.component;

import java.io.Serializable;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TriggerRequest;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.api.task.RunningTriggerContextHolder;
import org.sterl.spring.persistent_tasks.shared.model.TriggerEntity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ToTriggerData implements Converter<TriggerRequest<? extends Serializable>, TriggerEntity> {

    @Getter
    private final StateSerializationComponent stateSerialization;

    @Override
    @Nullable
    public TriggerEntity convert(@NonNull TriggerRequest<? extends Serializable> trigger) {
        var correlationId = RunningTriggerContextHolder.buildOrGetCorrelationId(trigger.correlationId());
        TaskId taskId = trigger.taskId();
        Serializable state = trigger.state();
        byte[] stateBytes = stateSerialization.serialize(taskId, state);
        final var data = TriggerEntity.builder()
                .key(trigger.key())
                .runAt(trigger.runtAt())
                .priority(trigger.priority())
                .state(stateBytes)
                .status(trigger.status() == null ? TriggerStatus.WAITING : trigger.status())
                .correlationId(correlationId)
                .tag(trigger.tag());
        
        return data.build();
    }
}
