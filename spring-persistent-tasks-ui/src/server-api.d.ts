/* tslint:disable */
/* eslint-disable */

export interface PagedModel<T> {
    content: T[];
    page: PageMetadata;
}

export interface SchedulerEntity {
    id: string;
    tasksSlotCount: number;
    runnungTasks: number;
    systemLoadAverage: number;
    maxHeap: number;
    usedHeap: number;
    status: TaskSchedulerStatus;
    lastPing: string;
}

export interface AddTriggerRequest<T> {
    id: string;
    taskId: TaskId<T>;
    state: T;
    runtAt: string;
    priority: number;
}

export interface HistoryOverview {
    instanceId: number;
    taskName: string;
    entryCount: number;
    start: string;
    end: string;
    createdTime: string;
    executionCount: number;
    runningDurationInMs: number;
}

export interface RetryStrategy {
}

export interface SpringBeanTask<T> extends Consumer<T> {
}

export interface Task<T> extends SpringBeanTask<T> {
    id: TaskId<T>;
}

export interface TaskId<T> extends Serializable {
    name: string;
}

export interface TaskTriggerBuilder<T> {
}

export interface Trigger {
    id: number;
    instanceId: number;
    key: TriggerKey;
    runningOn: string;
    createdTime: string;
    runAt: string;
    start: string;
    end: string;
    executionCount: number;
    priority: number;
    status: TriggerStatus;
    runningDurationInMs: number;
    state: any;
    exceptionName: string;
    lastException: string;
}

export interface TriggerKey extends Serializable {
    id: string;
    taskName: string;
}

export interface TriggerKeyBuilder {
}

export interface TriggerTaskCommand<T> {
    triggers: AddTriggerRequest<T>[];
}

export interface PageMetadata {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
}

export interface Serializable {
}

export interface Consumer<T> {
}

export type TaskSchedulerStatus = "ONLINE" | "OFFLINE";

export type TriggerStatus = "NEW" | "RUNNING" | "SUCCESS" | "FAILED" | "CANCELED";
