package org.sterl.spring.task.api;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor 
@AllArgsConstructor
public class TriggerId implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id; 
    private String name; 

    public TaskId<Serializable> toTaskId() {
        return new TaskId<Serializable>(name);
    }
}