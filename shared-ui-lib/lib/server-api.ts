/* tslint:disable */
/* eslint-disable */

export interface PagedModel<T> {
    content: T[];
    page: PageMetadata;
}

export interface SchedulerEntity {
    id: string;
    tasksSlotCount: number;
    runningTasks: number;
    systemLoadAverage: number;
    maxHeap: number;
    usedHeap: number;
    lastPing: string;
}

export interface RetryStrategy {}

export interface TaskId {
    name: string;
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

export interface Trigger {
    id: number;
    instanceId: number;
    key: TriggerKey;
    tag: string;
    correlationId: string;
    runningOn: string;
    createdTime: string;
    runAt: string;
    lastPing: string;
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

export interface TriggerKey {
    id: string;
    taskName: string;
}

export interface TriggerRequest<T> {
    key: TriggerKey;
    status: TriggerStatus;
    state: T;
    runtAt: string;
    priority: number;
    correlationId: string;
    tag: string;
}

export interface TriggerSearch {
    search: string;
    keyId: string;
    taskName: string;
    correlationId: string;
    status: TriggerStatus;
    tag: string;
}

export interface PageMetadata {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
}

export type TriggerStatus =
    | "AWAITING_SIGNAL"
    | "WAITING"
    | "RUNNING"
    | "SUCCESS"
    | "FAILED"
    | "CANCELED"
    | "EXPIRED_SIGNAL";
