package org.sterl.spring.persistent_tasks.trigger.component;

import java.io.Serializable;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.task.StateSerializer;
import org.sterl.spring.persistent_tasks.api.task.StateSerializer.DeSerializationFailedException;
import org.sterl.spring.persistent_tasks.api.task.StateSerializer.SerializationFailedException;
import org.sterl.spring.persistent_tasks.shared.model.TriggerEntity;
import org.sterl.spring.persistent_tasks.task.TaskService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StateSerializationComponent {

    private final StateSerializer<Serializable> defaultStateSerializer;
    private final TaskService taskService;
    
    @Nullable
    public <T extends Serializable> byte[] serialize(@NonNull TaskId<T> id, @Nullable final T obj) {
        if (obj == null) return null;
        StateSerializer<T> d = taskService.getStateSerializer(id, defaultStateSerializer);
        try {
            return d.serialize(id, obj);
        } catch (SerializationFailedException e) {
            throw e;
        } catch (Exception ex) {
            throw new SerializationFailedException(obj, ex);
        }
    }

    @Nullable
    public <T extends Serializable> T deserialize(@NonNull TaskId<T> id, @Nullable byte[] bytes) {
        if (bytes == null) return null;

        StateSerializer<T> d = taskService.getStateSerializer(id, defaultStateSerializer);
        try {
            return d.deserialize(id, bytes);
        } catch (DeSerializationFailedException e) {
            throw e;
        } catch (Exception ex) {
            throw new DeSerializationFailedException(bytes, ex);
        }
    }
    
    @Nullable
    public Serializable deserialize(TriggerEntity data) {
        if (data == null) return null;
        return deserialize(data.getKey().toTaskId(), data.getState());
    }

    @Nullable
    public <T extends Serializable> T deserializeOrNull(TaskId<T> id, byte[] bytes) {
        try {
            return deserialize(id, bytes);
        } catch (DeSerializationFailedException e) {
            log.warn("Failed to deserialize state of {} - returning null.", id, e);
            return null;
        }
    }
    
    @Nullable
    public Serializable deserializeOrNull(TriggerEntity data) {
        if (data == null) return null;
        try {
            return deserialize(data);
        } catch (Exception e) {
            log.warn("Failed to deserialize state of {} - returning null.", data.getKey(), e);
            return null;
        }
    }
}
