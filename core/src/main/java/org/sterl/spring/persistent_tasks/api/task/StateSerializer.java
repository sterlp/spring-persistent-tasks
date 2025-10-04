package org.sterl.spring.persistent_tasks.api.task;

import java.io.Serializable;

import org.springframework.lang.NonNull;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.exception.SpringPersistentTaskException;

public interface StateSerializer<T extends Serializable> {

    byte[] serialize(@NonNull TaskId<T> id, @NonNull T obj) throws SerializationFailedException;

    T deserialize(@NonNull TaskId<T> id, @NonNull byte[] bytes) throws DeSerializationFailedException;

    public static class DeSerializationFailedException extends SpringPersistentTaskException {
        private static final long serialVersionUID = 1L;

        public DeSerializationFailedException(byte[] bytes, Exception e) {
            super("Failed to deserialize state of length " + bytes.length, bytes, e);
        }
    }
    
    public static class SerializationFailedException extends SpringPersistentTaskException {
        private static final long serialVersionUID = 1L;

        public SerializationFailedException(Serializable obj, Exception e) {
            super("Failed to serialize state " + obj.getClass(), obj, e);
        }
    }
}
