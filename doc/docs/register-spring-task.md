# Register a Tasks

![Spring Persistent Task Interface](/assets/spring-persistent-task-interface.png)

## RunningTriggerContextHolder @since v1.6

Allows the access to the state object and gives more informations about the current executed trigger.

# Simple Closure

TaskId will be the method name.

```java
@Bean
SpringBeanTask<String> task1() {
    return (String state) -> {
        // DO STUFF
    };
}
```

# Define a closure which has a transactional workload

This closure will we wrapped in the default `TransactionalTempalte`

```java
@Bean("transactionalClosure")
TransactionalTask<String> transactionalClosure(PersonRepository personRepository) {
    return name -> {
        personRepository.save(new PersonBE(name + "foo"));
        personRepository.save(new PersonBE(name + "bar"));
    };
}
```

# Override the interface

TaskId will be the method name.

```java
@Bean
SpringBeanTask<String> task2() {
    return new SpringBeanTask<String>() {
        @Override
        public void accept(String state) {
            // DO STUFF
        }
        // gives you access to more customizations
        @Override
        public RetryStrategy retryStrategy() {
            return RetryStrategy.NO_RETRY;
        }
    };
}
```

# Define a Bean class

```java
@Component(BuildVehicleTask.NAME)
@RequiredArgsConstructor
@Slf4j
public class BuildVehicleTask implements PersistentTask<Vehicle> {

    private static final String NAME = "buildVehicleTask";
    public static final TaskId<Vehicle> ID = new TaskId<>(NAME);

    private final VehicleRepository vehicleRepository;

    @Override
    public void accept(Vehicle vehicle) {
        // do stuff
        // save
        vehicleRepository.save(vehicle);
    }
    // OPTIONAL
    @Override
    public RetryStrategy retryStrategy() {
        // run 5 times, multiply the execution count with 4, add the result in HOURS to now.
        return new MultiplicativeRetryStrategy(5, ChronoUnit.HOURS, 4)
    }
    // OPTIONAL
    // if the task in accept requires a DB transaction, join them together with the framework
    // if true the TransactionTemplate is used. Set here any timeouts.
    @Override
    public boolean isTransactional() {
        return true;
    }
}
```
