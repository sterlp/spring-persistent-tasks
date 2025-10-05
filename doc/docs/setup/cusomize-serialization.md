By default java serialization is used to serializer the state. It can by customized in a task or for all tasks.

## Use Jackson serialization for a task @since v2.3

```java
public record TaskState(int id, String message) implements Serializable {}

@RequiredArgsConstructor
public class TaskWithOwnSerialization
    implements PersistentTask<TaskState>, SerializationProvider<TaskState> {

    private final ObjectMapper mapper;
    private final AsyncAsserts asserts;

    @Override
    public StateSerializer<TaskState> getSerializer() {
        return new JacksonStateSerializer<>(mapper, TaskState.class);
    }

    @Override
    public void accept(@Nullable TaskState state) {
        asserts.info("state: " + state);
    }
}
```

## Replace the default serialization @since v2.3

By default the `DefaultStateSerializer` uses Java Serialization and can be replaced if required:

```java
@Bean
@ConditionalOnMissingBean(DefaultStateSerializer.class)
StateSerializer<Serializable> defaultStateSerializer() {
    log.info("Using java serialization for task states.",
            DefaultStateSerializer.class.getSimpleName());
    return new DefaultStateSerializer();
}
```

For compatibility with Spring DEV tools the current thread is registered during startup as class loader context:

```java
import org.springframework.core.serializer.DefaultDeserializer;
import org.springframework.core.serializer.DefaultSerializer;
import org.springframework.core.serializer.Deserializer;
import org.springframework.core.serializer.Serializer;

public DefaultStateSerializer() {
    this.serializer = new DefaultSerializer();
    this.deserializer = new DefaultDeserializer(Thread.currentThread().getContextClassLoader());
}
```
