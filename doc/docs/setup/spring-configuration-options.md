# Configuration options

| Property                                       | Type                 | Description                                                              | Default Value      |
| ---------------------------------------------- | -------------------- | ------------------------------------------------------------------------ | ------------------ |
| `spring.persistent-tasks.poll-rate`            | `java.lang.Integer`  | The interval at which the scheduler checks for new tasks, in seconds.    | `30`               |
| `spring.persistent-tasks.max-threads`          | `java.lang.Integer`  | The number of threads to use; set to 0 to disable task processing.       | `10`               |
| `spring.persistent-tasks.task-timeout`         | `java.time.Duration` | The maximum time allowed for a task and scheduler to complete a task.    | `PT5M` (5 minutes) |
| `spring.persistent-tasks.poll-task-timeout`    | `java.lang.Integer`  | The interval at which the system checks for abandoned tasks, in seconds. | `300` (5 minutes)  |
| `spring.persistent-tasks.scheduler-enabled`    | `java.lang.Boolean`  | Indicates whether this node should handle triggers.                      | `true`             |
| `spring.persistent-tasks.thread-factory`       | `DEFAULT`/`VIRTUAL`  | Which thread factory should be used DEFAULT or VIRTUAL.                  | `DEFAULT`          |
| `spring.persistent-tasks.history.delete-after` | `java.time.Duration` | The max age for a trigger in the hstory.                                 | `PT72H` (30 days)  |
| `spring.persistent-tasks.history.delete-rate`  | `java.time.Integer`  | The interval at which old triggers are deleted, in hours.                | `24` (24 hours)    |
