package org.sterl.spring.persistent_tasks.api.task;

import java.io.Serializable;

import org.springframework.lang.NonNull;
import org.sterl.spring.persistent_tasks.api.TaskId;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * Default implementation to use jackson instead java serialization for a task.
 * 
 * @param <T> the type of the state for the deserialization
 */
@RequiredArgsConstructor
public class JacksonStateSerializer<T extends Serializable> implements StateSerializer<T> {
    
    private final ObjectMapper mapper;
    private final Class<T> clazz;

    @Override
    public byte[] serialize(@NonNull TaskId<T> id, @NonNull T obj) 
        throws SerializationFailedException{

        try {
            return mapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new SerializationFailedException(obj, e);
        }
    }

    @Override
    public T deserialize(@NonNull TaskId<T> id, @NonNull byte[] bytes)
            throws DeSerializationFailedException {
        try {
            return mapper.readValue(bytes, clazz);
        } catch (Exception e) {
            throw new DeSerializationFailedException(bytes, e);
        }
    }
}
