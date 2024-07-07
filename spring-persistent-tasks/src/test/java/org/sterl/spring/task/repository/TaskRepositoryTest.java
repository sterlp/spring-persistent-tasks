package org.sterl.spring.task.repository;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.sterl.spring.task.api.Task;
import org.sterl.spring.task.api.TaskId;
import org.sterl.spring.task.model.RegisteredTask;

class TaskRepositoryTest {

    private final TaskRepository subject = new TaskRepository(new ArrayList<>());

    @Test
    void testAssertIsKnown() {
        // GIVEN
        Task<String> t = new RegisteredTask<>("foo", (s) -> {});

        // WHEN
        subject.addTask(t);
        
        // THEN
        subject.assertIsKnown(t.getId());
        subject.assertIsKnown(new TaskId<String>("foo"));
        assertThrows(IllegalStateException.class, () -> subject.assertIsKnown(new TaskId<String>("1")));
    }

}
