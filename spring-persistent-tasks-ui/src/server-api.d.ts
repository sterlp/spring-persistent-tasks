/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 3.2.1263 on 2024-12-11 21:22:17.

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
    when: Date;
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
    createdTime: Date;
    triggerTime: Date;
    start: Date;
    end: Date;
    executionCount: number;
    priority: number;
    status: TriggerStatus;
    runningDurationInMs: number;
    state: any;
    exceptionName: string;
    lastException: string;
}

export type TriggerStatus = "NEW" | "RUNNING" | "SUCCESS" | "FAILED" | "CANCELED";
