package org.sterl.spring.persistent_tasks.trigger.component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import org.springframework.core.serializer.DefaultDeserializer;
import org.springframework.core.serializer.DefaultSerializer;
import org.springframework.core.serializer.Deserializer;
import org.springframework.core.serializer.Serializer;
import org.springframework.lang.NonNull;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.task.StateSerializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DefaultStateSerializer implements StateSerializer<Serializable> {
    
    private final Serializer<Object> serializer;
    private final Deserializer<Object> deserializer;
    
    public DefaultStateSerializer() {
        // needed for spring boot developer tools
        // https://github.com/sterlp/spring-persistent-tasks/issues/19
        this(new DefaultSerializer(), 
                new DefaultDeserializer(Thread.currentThread().getContextClassLoader()));
    }

    @SuppressWarnings("PMD.UnusedLocalVariable")
    @Override
    public byte[] serialize(@NonNull TaskId<Serializable> id, @NonNull Serializable obj) {
        if (obj instanceof byte[] b) return b;

        try {
            var bos = new ByteArrayOutputStream(512);
            serializer.serialize(obj, bos);
            return bos.toByteArray();
        } catch (IOException ex) {
            throw new SerializationFailedException(obj, ex);
        }
    }

    @SuppressWarnings("PMD.UnusedLocalVariable")
    @Override
    public Serializable deserialize(@NonNull TaskId<Serializable> id, @NonNull byte[] bytes) {
        try {
            var bis = new ByteArrayInputStream(bytes);
            return (Serializable)deserializer.deserialize(bis);
        } catch (IOException ex) {
            throw new DeSerializationFailedException(bytes, ex);
        }
    }
}
