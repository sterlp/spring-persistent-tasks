package org.sterl.spring.task.model;

import java.io.Serializable;

import org.sterl.spring.task.api.TaskId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
public class TriggerId implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id; 
    private String name; 

    public TaskId<Serializable> toTaskId() {
        return new TaskId<Serializable>(name);
    }
}