package org.sterl.spring.persistent_tasks.trigger.component;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.sterl.spring.persistent_tasks.api.TriggerRequest;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.api.task.RunningTriggerContextHolder;
import org.sterl.spring.persistent_tasks.shared.model.TriggerData;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ToTriggerData implements Converter<TriggerRequest<?>, TriggerData> {

    private final StateSerializer stateSerializer;

    @Override
    @Nullable
    public TriggerData convert(@NonNull TriggerRequest<?> trigger) {
        var correlationId = RunningTriggerContextHolder.buildOrGetCorrelationId(trigger.correlationId());
        byte[] state = stateSerializer.serialize(trigger.state());
        final var data = TriggerData.builder()
                .key(trigger.key())
                .runAt(trigger.runtAt())
                .priority(trigger.priority())
                .state(state)
                .status(trigger.status() == null ? TriggerStatus.WAITING : trigger.status())
                .correlationId(correlationId)
                .tag(trigger.tag());
        
        return data.build();
    }
}
