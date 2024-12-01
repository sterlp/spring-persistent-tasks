package org.sterl.spring.persistent_tasks.task;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.task.model.RegisteredTask;
import org.sterl.spring.persistent_tasks.task.repository.TaskRepository;

class TaskServiceTest {

    private final TaskService subject = new TaskService(new TaskRepository(new ArrayList<>()));

    @Test
    void testAssertIsKnown() {
        // GIVEN
        RegisteredTask<String> t = new RegisteredTask<>("foo", (s) -> {});

        // WHEN
        subject.repalce(t);
        
        // THEN
        subject.assertIsKnown(t.getId());
        subject.assertIsKnown(new TaskId<String>("foo"));
        assertThrows(IllegalStateException.class, () -> subject.assertIsKnown(new TaskId<String>("1")));
    }
}
