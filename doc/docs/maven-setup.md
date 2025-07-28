# Maven setup

-   [Maven Central spring-persistent-tasks-core](https://central.sonatype.com/artifact/org.sterl.spring/spring-persistent-tasks-core/versions)
-   [Maven Central spring-persistent-tasks-ui](https://central.sonatype.com/artifact/org.sterl.spring/spring-persistent-tasks-ui/versions)
-   [Maven Central spring-persistent-tasks-db](https://central.sonatype.com/artifact/org.sterl.spring/spring-persistent-tasks-db/versions)

# Core

-   Allows the build of triggers
-   the execution of tasks
-   Adds also REST APIs to manage triggers and list existing tasks

```xml
<dependency>
    <groupId>org.sterl.spring</groupId>
    <artifactId>spring-persistent-tasks-core</artifactId>
    <version>x.x.x</version>
</dependency>
```

## Setup Spring for Core

```java
@SpringBootApplication
@EnableSpringPersistentTasks
public class ExampleApplication {
```

# DB using liquibase

Dependency needed to setup the DB using liquibase

```xml
<dependency>
    <groupId>org.sterl.spring</groupId>
    <artifactId>spring-persistent-tasks-db</artifactId>
    <version>x.x.x</version>
</dependency>
```

## Option 1: Just include the master file

```xml
<include file="spring-persistent-tasks/db.changelog-master.xml" />
```

## Option 2: import changesets on by one

```xml
<include file="spring-persistent-tasks/db/pt-changelog-v1.xml" />
```

# Enable a SPA management UI

This dependency adds a spring react default UI to /task-ui. Assuming at least an instance of the spring persistent tasks is available on this node.

```xml
<dependency>
    <groupId>org.sterl.spring</groupId>
    <artifactId>spring-persistent-tasks-ui</artifactId>
    <version>x.x.x</version>
</dependency>
```

## Setup Spring Boot for the UI

```java
@SpringBootApplication
@EnableSpringPersistentTasks
@EnableSpringPersistentTasksUI
public class ExampleApplication {
```
