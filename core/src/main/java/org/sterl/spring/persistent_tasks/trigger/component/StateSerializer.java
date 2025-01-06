package org.sterl.spring.persistent_tasks.trigger.component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StateSerializer {

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
            throw new RuntimeException(ex);
        }
    }

    public Serializable deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        var bis = new ByteArrayInputStream(bytes);
        try (ObjectInput in = new ObjectInputStream(bis)) {
            return (Serializable)in.readObject();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to deserialize state of length " + bytes.length, ex);
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
}
