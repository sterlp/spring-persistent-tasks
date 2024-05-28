package org.sterl.spring.task.component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class StateSerializer {

    public byte[] serialize(final Serializable obj) {
        if (obj == null) return null;
        
        var bos = new ByteArrayOutputStream(512);

        try (var out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            return bos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Serializable deserialize(byte[] bytes) {
        if (bytes == null) return null;

        var bis = new ByteArrayInputStream(bytes);
        try (ObjectInput in = new ObjectInputStream(bis)) {
            return (Serializable)in.readObject();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
