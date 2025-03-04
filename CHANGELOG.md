# Changelog

## v1.5.5 - (2025-03-04)

-   MdcTriggerInterceptor adds now start date and scheduler name

## v1.5.4 - (2025-01-19)

-   Added MdcTriggerInterceptor

## v1.5.3 - (2025-01-14)

-   adjusted trigger cols that the UI does not break
-   showing always all existing schedulers

## v1.5.2 - (2025-01-13)

-   FixedIntervalRetryStrategy
-   Added SchedulerCustomizer

## v1.5.1 - (2025-01-12)

-   filter trigger by status
-   filter history by status

## v1.5.0 - (2025-01-11)

-   Adjusted transaction handling for trigger life cycle events
-   Base event entry is only written for done/finished trigger
-   Base statistics added for a task

## v1.4.6 - (2025-01-08)

-   Trigger history with more details - not waiting for the transaction

## v1.4.5 - (2025-01-08)

-   Adjusted path matching to support sub routes for an SPA web app

## v1.4.4 - (2025-01-08)

-   Fixed UI routing
-   added support for thymeleaf - adding index.html to template folder

## v1.4.3 - (2025-01-08)

-   Scheduler service leaves current transaction before executing task

## v1.4.2 - (2025-01-06)

-   Fixed count by TaskId
-   added search by ID to the UI
-   added search by task to history

## v1.4.1 - (2025-01-06)

-   Added state to the TriggerLifeCycleEvent
-   @Transactional annotation is taken from the method first

## v1.4.0 - (2025-01-05)

-   @Transactional Annotation support
-   PersistentTask instead of Task or SpringBeanTask

## v1.3.1 - (2025-01-02)

-   Bugfixes
-   Sprign Transaction Template support

## v1.3.0 - (2025-01-01)

-   MariaDB support
-   PostgreSQL support

## v1.2.0 - (2024-12-31)

-   Run now button in the UI
-   Offline Schedulers are deleted from the registry
-   testing mssql server automatically
-   DB change to support MS for durations

## v1.1.0 - (2024-12-30)

-   Showing trigger history entries
-   Added `PersistentTaskService` as a new abstraction
-   Added cancel trigger button to the UI
-   Retry is now 3 times as in the strategy name
