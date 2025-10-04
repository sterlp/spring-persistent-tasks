package org.sterl.spring.persistent_tasks.trigger.component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
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
        this.serializer = new DefaultSerializer();
        this.deserializer = new DefaultDeserializer(Thread.currentThread().getContextClassLoader());
    }

    @Override
    public byte[] serialize(@NonNull TaskId<Serializable> id, @NonNull Serializable obj) {
        if (obj instanceof byte[] b) return b;

        var bos = new ByteArrayOutputStream(512);
        try (var out = new ObjectOutputStream(bos)) {
            serializer.serialize(obj, bos);
            return bos.toByteArray();
        } catch (IOException ex) {
            throw new SerializationFailedException(obj, ex);
        }
    }

    @Override
    public Serializable deserialize(@NonNull TaskId<Serializable> id, @NonNull byte[] bytes) {
        var bis = new ByteArrayInputStream(bytes);
        try (var in = new ContextClassLoaderObjectInputStream(bis)) {
            return (Serializable)deserializer.deserialize(bis);
        } catch (IOException ex) {
            throw new DeSerializationFailedException(bytes, ex);
        }
    }
    
    // needed for spring boot developer tools
    // https://github.com/sterlp/spring-persistent-tasks/issues/19
    static class ContextClassLoaderObjectInputStream extends ObjectInputStream {
        ContextClassLoaderObjectInputStream(InputStream in) throws IOException {
            super(in);
        }
        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) 
                throws IOException, ClassNotFoundException {
            return Class.forName(desc.getName(), false, 
                Thread.currentThread().getContextClassLoader());
        }
    }
}
