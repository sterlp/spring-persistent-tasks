# Changelog

## v2.1.0 - (2025-07-07)

### New features:

-   Grouped triggers API
-   UI elements are published to npmjs.com
-   Lazy render of pages
-   Link to github
-   moved ui-lib and web-app into own folders
-   using pnpm as build tool
-   exposed TriggerActionsView
-   added trigger group api
-   added release of the ui-lib to npmjs
-   added TriggerGroupListView

## v2.0.0 - (2025-06-16)x

### New features:

-   Better naming for all trigger classes
-   Rename of all tables
-   Rename of all indexes
-   Rename of all pk index
-   NO RENAME OF THE SEQUENCES - not suppoered by all dbs
-   Added suspendable triggers
-   Added resume for triggers
-   Added tag for triggers
-   Added a far better search for triggers
-   Add querydsl

## v1.7.0 - (2025-06-01)

### New features:

-   Added support for a virtual thread factory
-   Added support to configure a custom thread factory

## v1.6.7 - (2025-04-01)

### Bug fixes:

-   abandoned triggers will now fire a failed trigger event
-   trigger history scheduler waits now in case of shut down for any history events to get saved

## v1.6.6 - (2025-03-20)

### New features:

-   first metrics

### Bug fixes:

-   fixed history delete start of history

## v1.6.5 - (2025-03-19)

-   fixed correlation ID size
-   cancel can now always be triggered from the UI
-   showing slow used history

## v1.6.4 - (2025-03-18)

-   in tests the task executor may finish a task before it returns it reference

## v1.6.3 - (2025-03-18)

-   ensure the remove of a running trigger happens after it is added

## v1.6.2 - (2025-03-18)

-   removed synchronized from TaskExecutorComponent

## v1.6.1 - (2025-03-18)

-   simpler RetryStrategy - as function
-   showing last ping
-   showing execution time or still running triggers
-   saver way to keep track of running triggers

## v1.6.0 - (2025-03-11)

-   Running triggers can be canceled now
-   Running triggers can be failed now
-   https://github.com/sterlp/spring-persistent-tasks/wiki/Cancel-a-task-trigger
-   Triggers have now correlationId to collect them
-   Added Re-Queue / Re-Run trigger to history page
-   Correlation Id is shown in the UI
-   ID search includes also Correlation Id
-   Moved helper classes to own test jar

## v1.5.6 - (2025-03-06)

-   Better ID search
-   Added info to the UI how to search

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
