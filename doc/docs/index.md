---
title: Spring Persistent Tasks - Async Task Management Framework
description: A lightweight task management framework for Spring Boot with JPA persistence, clustering support, retry mechanisms, and transactional integration for asynchronous task execution.
keywords: spring boot, task scheduler, async tasks, JPA persistence, distributed tasks, task queue, retry mechanism, spring framework, task management, background jobs
tags:
  - Spring Boot
  - Task Scheduler
  - Async Processing
  - JPA
  - Clustering
  - Retry Logic
---

# Spring Persistent Tasks

[![Java CI with Maven](https://github.com/sterlp/spring-persistent-tasks/actions/workflows/build.yml/badge.svg)](https://github.com/sterlp/spring-persistent-tasks/actions/workflows/build.yml)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

A lightweight and simple task management framework for **Spring Boot** designed to queue and execute **asynchronous tasks** with support for **database persistence** and a user-friendly interface. Perfect for implementing scheduling patterns, outbound patterns, and background job processing in distributed systems.

**Built for Spring Boot 3.x with JPA** - seamlessly integrates with your existing Spring application.

![Dashboard](/assets/dashboard.png)

## Key Features ✨

1. **JPA-Powered Persistence** - Automatically persists tasks to your database
1. **Spring Boot Auto-Configuration** - Simple setup with @EnableSpringPersistentTasks
1. **Clustering Support** - Built-in lock management for distributed environments
1. **Type-Safe Tasks** - Generic task definitions with state objects
1. **Retry Mechanisms** - Automatic retry policies for failed executions
1. **Transactional Integration** - Works with Spring's transaction management
1. **Queue Management** - Intelligent task queuing and prioritization
1. **Different DB Supports** - MySQL, azure-sql-edge, PostgreSQL, and H2

## Setup with Maven

-   [Maven Central spring-persistent-tasks-core](https://central.sonatype.com/artifact/org.sterl.spring/spring-persistent-tasks-core/versions)

```xml
<dependency>
    <groupId>org.sterl.spring</groupId>
    <artifactId>spring-persistent-tasks-core</artifactId>
    <version>2.x.x</version>
</dependency>
```

# Setup Spring

```java
@SpringBootApplication
@EnableSpringPersistentTasks
public class ExampleApplication {
```

# Create a Task

```java
@Bean
PersistentTask<Vehicle> task1(VehicleHttpConnector vehicleHttpConnector) {
    return v -> vehicleHttpConnector.send(v);
}
```

# Trigger a task

```java
@Autowired
PersistentTaskService persistentTaskService;

public void triggerTask1(Vehicle vehicle) {
    persistentTaskService.runOrQueue(
        TriggerBuilder.newTrigger("task1").state(vehicle).build());
}
```
