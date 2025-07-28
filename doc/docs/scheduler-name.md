# Custom Scheduler Name

To customize the way how the default `SchedulerService` is build the `SchedulerCustomizer` interface can be used.

# Use random string

One possible way could be just to use a random string for each instance, which will change on each restart.

```java
@Bean
SchedulerCustomizer schedulerCustomizer(Environment env) {
  return new SchedulerCustomizer() {
    public String name() {
      var hostname = UUID.randomUUID().toString().substring(0, 8);
      hostname = String.join("-", env.getActiveProfiles()) + "-" + hostname;
      return hostname;
    }
  };
}
```

# Azure example

In this example we use the azure web-application environment variables to set the scheduler name:

```java
@Bean
SchedulerCustomizer schedulerCustomizer(Environment env) {
  return new SchedulerCustomizer() {
    public String name() {
      var hostname = env.getProperty("WEBSITE_INSTANCE_ID");
      var role = env.getProperty("APPSETTING_APPLICATIONINSIGHTS_ROLE_NAME", "").toLowerCase();
      if (role != "") {
        hostname = hostname + "-" + role;
      }
      return hostname;
    }
  };
}
```
