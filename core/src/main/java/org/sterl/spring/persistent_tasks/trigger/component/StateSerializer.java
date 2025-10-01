package org.sterl.spring.persistent_tasks.trigger.component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;

import org.sterl.spring.persistent_tasks.exception.SpringPersistentTaskException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StateSerializer {
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

    public byte[] serialize(final Serializable obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof byte[] b) return b;

        var bos = new ByteArrayOutputStream(512);
        try (var out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            return bos.toByteArray();
        } catch (Exception ex) {
            throw new SerializationFailedException(obj, ex);
        }
    }

    public Serializable deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        var bis = new ByteArrayInputStream(bytes);
        try (var in = new ContextClassLoaderObjectInputStream(bis)) {
            return (Serializable)in.readObject();
        } catch (Exception ex) {
            throw new DeSerializationFailedException(bytes, ex);
        }
    }
    
    public Serializable deserializeOrNull(byte[] bytes) {
        try {
            return deserialize(bytes);
        } catch (Exception e) {
            log.error("Failed to deserialize bytes", e);
            return null;
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
