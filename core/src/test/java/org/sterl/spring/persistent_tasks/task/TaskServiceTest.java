package org.sterl.spring.persistent_tasks.task;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.task.component.TaskTransactionComponent;
import org.sterl.spring.persistent_tasks.task.repository.TaskRepository;

class TaskServiceTest {

    private final TaskService subject = new TaskService(
            new TaskTransactionComponent(null, null),
            new TaskRepository());

    @Test
    void testAssertIsKnown() {
        // GIVEN
        // WHEN
        var id = subject.replace("foo", (s) -> {});

        // THEN
        subject.assertIsKnown(id);
        subject.assertIsKnown(new TaskId<String>("foo"));
        assertThrows(IllegalStateException.class, () -> subject.assertIsKnown(new TaskId<String>("1")));
    }
}
