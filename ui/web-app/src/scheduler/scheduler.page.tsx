import SchedulerStatusView from "@src/scheduler/views/scheduler.view";
import { useEffect } from "react";
import { Card, Col, Row } from "react-bootstrap";
import {
    formatMs,
    HttpErrorView,
    SchedulerEntity,
    TaskStatusHistoryOverview,
    TriggerStatusView,
    useAutoRefresh,
    useServerObject,
} from "spring-persistent-tasks-ui";

const SchedulersPage = () => {
    const schedulers = useServerObject<SchedulerEntity[]>(
        "/spring-tasks-api/schedulers"
    );
    const tasks = useServerObject<string[]>("/spring-tasks-api/tasks");
    const taskHistory = useServerObject<TaskStatusHistoryOverview[]>(
        "/spring-tasks-api/task-status-history"
    );

    useEffect(() => tasks.doGet(), []);
    useAutoRefresh(10000, () => schedulers.doGet(), []);
    useAutoRefresh(10000, () => taskHistory.doGet(), []);

    return (
        <>
            <HttpErrorView error={schedulers.error} />

            {schedulers.data && schedulers.data.length > 0 && (
                <>
                    <div className="mb-3">
                        <h5 className="text-muted fw-semibold">Active Schedulers</h5>
                    </div>
                    <Row className="g-3 mb-4">
                        {schedulers.data.map((s) => (
                            <Col key={s.id} xl={6} lg={12}>
                                <SchedulerStatusView scheduler={s} />
                            </Col>
                        ))}
                    </Row>
                </>
            )}

            {tasks.data && tasks.data.length > 0 && (
                <>
                    <div className="mb-3">
                        <h5 className="text-muted fw-semibold">Task Execution Statistics</h5>
                    </div>
                    <Row className="g-3">
                        {tasks.data.map((i) => (
                            <Col key={i} xl={6} lg={12}>
                                <TaskStatusHistoryOverviewView
                                    name={i}
                                    status={taskHistory.data || []}
                                />
                            </Col>
                        ))}
                    </Row>
                </>
            )}
        </>
    );
};
export default SchedulersPage;

const TaskStatusHistoryOverviewView = ({
    name,
    status,
}: {
    name: string;
    status: TaskStatusHistoryOverview[];
}) => {
    const taskStats = status.filter((s) => s.taskName === name);

    return (
        <Card className="shadow-sm border-0 h-100">
            <Card.Header className="bg-success text-white py-3">
                <h6 className="mb-0 fw-semibold">
                    <i className="fas fa-tasks me-2"></i>
                    {name}
                </h6>
            </Card.Header>
            <Card.Body className="p-3">
                {taskStats.length === 0 ? (
                    <div className="text-muted text-center py-3">
                        <small>No execution history available</small>
                    </div>
                ) : (
                    <div className="d-flex flex-column gap-2">
                        {taskStats.map((s) => (
                            <div
                                key={s.taskName + s.status}
                                className="border rounded p-3 bg-body-secondary"
                            >
                                <Row className="g-3 align-items-center">
                                    <Col xs={12} md={6} lg={3}>
                                        <TriggerStatusView
                                            status={s.status}
                                            suffix=""
                                        />
                                        <div className="mt-1">
                                            <small className="text-muted">Count: </small>
                                            <span className="fw-semibold">{s.executionCount}</span>
                                        </div>
                                    </Col>
                                    <Col xs={6} md={6} lg={3}>
                                        <div>
                                            <small className="text-muted d-block">Avg Duration</small>
                                            <span className="fw-semibold">{formatMs(s.avgDurationMs)}</span>
                                        </div>
                                    </Col>
                                    <Col xs={6} md={6} lg={3}>
                                        <div>
                                            <small className="text-muted d-block">Max Duration</small>
                                            <span className="fw-semibold">{formatMs(s.maxDurationMs)}</span>
                                        </div>
                                    </Col>
                                    <Col xs={12} md={6} lg={3}>
                                        <div>
                                            <small className="text-muted d-block">Avg Retries</small>
                                            <span className="fw-semibold">
                                                {Math.round(Math.max(0, s.avgExecutionCount - 1) * 100) / 100}
                                            </span>
                                        </div>
                                    </Col>
                                </Row>
                            </div>
                        ))}
                    </div>
                )}
            </Card.Body>
        </Card>
    );
};
