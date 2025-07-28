# Transaction Management

Overall the framework assumes that the queued task is not transactional nor a transaction is needed. To wrap the trigger status updates together with any transactional workload (recommended) where are two possibilites:

1. Use the transactional flag
1. Use the Spring `@Transactional` annotation

## Transactional flag

```java
@Bean
TransactionalTask<String> savePersonInTrx(PersonRepository personRepository) {
    return (state) ->  personRepository.save(new PersonBE(name));
}
```

which is basically the same as

```java
@Bean
PersistentTask<String> savePersonInTrx(PersonRepository personRepository) {
    return new TransactionalTask<String>() {
        @Override
        public void accept(String name) {
            personRepository.save(new PersonBE(name));
        }
        public RetryStrategy retryStrategy() {
            return RetryStrategy.THREE_RETRIES;
        }
        @Override
        public boolean isTransactional() {
            return true;
        }
    };
}
```

!!! note

    A `@Transactional` annotation will overwrite `isTransactional` flag

## Define Timeouts for the default TransactionTemplate

```java
@Bean
TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
    TransactionTemplate template = new TransactionTemplate(transactionManager);
    template.setTimeout(10);
    return template;
}
```

## Use Spring Transactional annotation

### Transactional Bean Class

```java
@Component("transactionalClass")
@Transactional(timeout = 5, propagation = Propagation.MANDATORY)
@RequiredArgsConstructor
public class TransactionalClass implements PersistentTask<String> {
    private final PersonRepository personRepository;
    @Override
    public void accept(String name) {
        personRepository.save(new PersonBE(name));
    }
}
```

### Transactional accept method

In this example the `REPEATABLE_READ` will be applied to the outside transaction

```java
@Component("transactionalMethod")
@RequiredArgsConstructor
static class TransactionalMethod implements PersistentTask<String> {
    private final PersonRepository personRepository;
    @Transactional(timeout = 6, propagation = Propagation.MANDATORY, isolation = Isolation.REPEATABLE_READ)
    @Override
    public void accept(String name) {
        personRepository.save(new PersonBE(name));
    }
}
```

### Own Transaction

No annotation or setting `REQUIRES_NEW` will force the framework to not wrap everything into a transactional template

```java
@Component("transactionalMethod")
@RequiredArgsConstructor
static class TransactionalMethod implements PersistentTask<String> {
    private final PersonRepository personRepository;
    @Transactional(timeout = 6, propagation = Propagation.MANDATORY, isolation = Isolation.REPEATABLE_READ)
    @Override
    public void accept(String name) {
        personRepository.save(new PersonBE(name));
    }
}
```
