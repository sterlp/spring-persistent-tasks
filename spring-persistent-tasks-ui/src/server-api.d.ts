/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 3.2.1263 on 2024-12-14 15:33:48.

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

export interface Trigger<T> {
    id: string;
    taskId: TaskId<T>;
    state: T;
    when: string;
    priority: number;
}

export interface TriggerId extends Serializable {
    id: string;
    name: string;
}

export interface TriggerIdBuilder {
}

export interface TriggerCanceledEvent extends TriggerLifeCycleEvent {
    trigger: TriggerEntity;
}

export interface TriggerCompleteEvent extends TriggerLifeCycleEvent {
    trigger: TriggerEntity;
}

export interface TriggerFailedEvent extends TriggerLifeCycleEvent {
    trigger: TriggerEntity;
}

export interface TriggerLifeCycleEvent {
}

export interface TriggerTaskCommand<T> {
    triggers: Trigger<T>[];
}

export interface OnlineSchedulersEntity {
    online: string[];
    countOffline: number;
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

export interface Serializable {
}

export interface TriggerEntity {
    id: TriggerId;
    data: TriggerData;
    runningOn: string;
}

export interface Consumer<T> {
}

export interface TriggerData {
    createdTime: string;
    triggerTime: string;
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

export type TaskSchedulerStatus = "ONLINE" | "OFFLINE";

export type TriggerStatus = "NEW" | "RUNNING" | "SUCCESS" | "FAILED" | "CANCELED";
