package org.sterl.spring.persistent_tasks.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.task.JacksonStateSerializer;
import org.sterl.spring.persistent_tasks.api.task.PersistentTask;
import org.sterl.spring.persistent_tasks.api.task.SerializationProvider;
import org.sterl.spring.persistent_tasks.api.task.StateSerializer;
import org.sterl.spring.persistent_tasks.test.AsyncAsserts;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

class TaskServiceTest extends AbstractSpringTest {
    
    public record TaskState(int id, String message) implements Serializable {}
    
    public static class LoggingJacksonStateSerializer<T extends Serializable> 
        extends JacksonStateSerializer<T> {
        
        private final AtomicInteger deserializeCount = new AtomicInteger(0);
        private final AtomicInteger serializeCount = new AtomicInteger(0);
        private final AsyncAsserts asserts;
        
        public LoggingJacksonStateSerializer(AsyncAsserts asserts, ObjectMapper mapper, Class<T> clazz) {
            super(mapper, clazz);
            this.asserts = asserts;
        }
        
        @Override
        public T deserialize(@NonNull TaskId<T> id, @NonNull byte[] bytes) throws DeSerializationFailedException {
            Objects.requireNonNull(bytes, "bytes are null!");
            Objects.requireNonNull(id, "TaskId is null");
            deserializeCount.incrementAndGet();
            asserts.add("deserialize " + id.name());
            return super.deserialize(id, bytes);
        }
        
        @Override
        public byte[] serialize(@NonNull TaskId<T> id, @NonNull T obj) throws SerializationFailedException {
            Objects.requireNonNull(obj, "T is null!");
            Objects.requireNonNull(id, "TaskId is null");
            serializeCount.incrementAndGet();
            asserts.add("serialize " + id.name());
            return super.serialize(id, obj);
        }
        
        public int getDeserializeCount() {
            return deserializeCount.get();
        }
        public int getSerializeCount() {
            return serializeCount.get();
        }
    }

    @RequiredArgsConstructor
    public static class TaskWithOwnSerialization 
        implements PersistentTask<TaskState>, SerializationProvider<TaskState> {
        
        private final StateSerializer<TaskState> stateSerializer;
        private final AsyncAsserts asserts;

        @Override
        public StateSerializer<TaskState> getSerializer() {
            return stateSerializer;
        }

        @Override
        public void accept(@Nullable TaskState state) {
            asserts.info("state: " + state);
        }
    }

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private TaskService subject;

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
    
    @Test
    void testStateSerializerisRegistered() throws Exception {
        // GIVEN
        TaskId<TaskState> taskId = TaskId.of("testStateSerializerisRegistered");
        // AND
        var stateSerializer = subject.getStateSerializer(taskId, null);
        assertThat(stateSerializer).isNull();
        // AND
        stateSerializer = subject.getStateSerializer(taskId, null);
        taskService.replace(taskId,
                new TaskWithOwnSerialization(new JacksonStateSerializer<>(mapper, TaskState.class), asserts));
        
        // WHEN
        stateSerializer = subject.getStateSerializer(taskId, null);
        
        // THEN
        assertThat(stateSerializer).isNotNull();
    }
    
    @Test
    void testUsesTaskSerialization() throws Exception {
        // GIVEN
        var customSerialization = new LoggingJacksonStateSerializer<>(asserts, mapper, TaskState.class);
        TaskId<TaskState> taskId1 = TaskId.of("testUsesTaskSerialization1");
        taskService.replace(taskId1, new TaskWithOwnSerialization(customSerialization, asserts));
        TaskId<String> task2 = taskService.replace("testUsesTaskSerialization2", s -> {
            asserts.info("task2::" + s);
        });
        
        // WHEN
        triggerService.queue(taskId1.newTrigger().build());
        triggerService.queue(task2.newTrigger().state("Hallo task2").build());

        // THEN
        assertThat(customSerialization.getDeserializeCount()).isZero();
        assertThat(customSerialization.getSerializeCount()).isZero();
        
        // WHEN
        triggerService.queue(taskId1.newTrigger().state(new TaskState(1, "hallo task1")) .build());
        // THEN
        assertThat(customSerialization.getDeserializeCount()).isZero();
        assertThat(customSerialization.getSerializeCount()).isOne();
        
        // WHEN
        persistentTaskTestService.runAllDueTrigger(OffsetDateTime.now());
        // THEN
        assertThat(customSerialization.getDeserializeCount()).isOne();
        assertThat(customSerialization.getSerializeCount()).isOne();
    }
}
