import SchedulerStatusView from "@src/scheduler/views/scheduler.view";
import { SchedulerEntity, TaskStatusHistoryOverview } from "@src/server-api";
import { formatMs } from "@src/shared/date.util";
import { useServerObject } from "@src/shared/http-request";
import useAutoRefresh from "@src/shared/use-auto-refresh";
import HttpErrorView from "@src/shared/view/http-error.view";
import StatusView from "@src/task/view/staus.view";
import { useEffect } from "react";
import { Card, Col, ListGroup, Row } from "react-bootstrap";

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
            <Row>
                <Col>
                    <HttpErrorView error={schedulers.error} />
                </Col>
            </Row>
            <Row>
                {schedulers.data?.map((s) => (
                    <Col key={s.id} xl="6" md="12" className="mb-2">
                        <SchedulerStatusView scheduler={s} />
                    </Col>
                ))}

                {tasks.data?.map((i) => (
                    <Col key={i} xl="6" md="12" className="mb-2">
                        <TaskStatusHistoryOverviewView
                            name={i}
                            status={taskHistory.data || []}
                        />
                    </Col>
                ))}
            </Row>
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
}) => (
    <Card>
        <Card.Header
            as="h5"
            className="d-flex justify-content-between align-items-center"
            style={{
                background: "linear-gradient(45deg, #007bff, #00d8ff)",
                color: "white",
            }}
        >
            {name}
        </Card.Header>
        <ListGroup variant="flush">
            {status
                .filter((s) => s.taskName == name)
                .map((s) => (
                    <ListGroup.Item key={s.taskName + s.status}>
                        <Row className="align-items-center">
                            <Col>
                                <StatusView
                                    status={s.status}
                                    suffix={`: ${s.executionCount}`}
                                />
                            </Col>
                            <Col>avg: {formatMs(s.avgDurationMs)}</Col>
                            <Col>max: {formatMs(s.maxDurationMs)}</Col>
                            <Col>
                                avg retry:{" "}
                                {Math.round(
                                    Math.max(0, s.avgExecutionCount - 1) * 100
                                ) / 100}
                            </Col>
                        </Row>
                    </ListGroup.Item>
                ))}
        </ListGroup>
    </Card>
);
