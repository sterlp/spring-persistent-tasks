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
    lastPing: string;
}

export interface AddTriggerRequest<T> {
    key: TriggerKey;
    state: T;
    runtAt: string;
    priority: number;
}

export interface PersistentTask<T> {
    transactional: boolean;
}

export interface RetryStrategy {
}

export interface FixedIntervalRetryStrategy extends RetryStrategy {
}

export interface LinearRetryStrategy extends RetryStrategy {
}

export interface MultiplicativeRetryStrategy extends RetryStrategy {
}

/**
 * @deprecated
 */
export interface SpringBeanTask<T> extends PersistentTask<T> {
}

export interface TaskId<T> extends Serializable {
    name: string;
}

export interface TaskTriggerBuilder<T> {
}

export interface TaskStatusHistoryOverview {
    taskName: string;
    status: TriggerStatus;
    executionCount: number;
    firstRun: string;
    lastRun: string;
    maxDurationMs: number;
    minDurationMs: number;
    avgDurationMs: number;
    avgExecutionCount: number;
}

export interface TransactionalTask<T> extends PersistentTask<T> {
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

export type TriggerStatus = "WAITING" | "RUNNING" | "SUCCESS" | "FAILED" | "CANCELED";
