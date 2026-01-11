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

export interface ConTriggerBuilder<T> {
}

export interface CronSchedule extends Schedule {
    expression: string;
}

export interface CronTriggerInfo {
    id: string;
    taskName: string;
    schedule: string;
    tag?: string;
    priority: number;
    suspended: boolean;
    hasStateProvider: boolean;
}

export interface HistoryTrigger {
    id: number;
    instanceId: number;
    key: TriggerKey;
    createdTime: string;
    start?: string;
    executionCount: number;
    status: TriggerStatus;
    message?: string;
}

export interface IntervalSchedule extends Schedule {
    interval: Duration;
}

export interface Schedule {
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
    tag?: string;
    correlationId?: string;
    runningOn?: string;
    createdTime: string;
    runAt: string;
    lastPing?: string;
    start?: string;
    end?: string;
    executionCount: number;
    priority: number;
    status: TriggerStatus;
    runningDurationInMs?: number;
    state?: any;
    exceptionName?: string;
    lastException?: string;
}

export interface TriggerBuilder<T> {
}

export interface TriggerGroup {
    count: number;
    groupByValue: string;
    sumDurationMs: number;
    sumRunCount: number;
    minRunAt: string;
    minCreatedTime: string;
    minStart: string;
    maxEnd: string;
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

export interface Duration extends TemporalAmount, Comparable<Duration> {
}

export interface TemporalAmount {
    units: TemporalUnit[];
}

export interface TemporalUnit {
    durationEstimated: boolean;
    duration: Duration;
    timeBased: boolean;
    dateBased: boolean;
}

export interface Comparable<T> {
}

export type TriggerStatus = "AWAITING_SIGNAL" | "WAITING" | "RUNNING" | "SUCCESS" | "FAILED" | "CANCELED" | "EXPIRED_SIGNAL";
