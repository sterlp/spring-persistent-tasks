import { useEffect } from "react";
import { useServerObject } from "../../shared/http-request";
import { SchedulerEntity, TaskSchedulerStatus } from "../../server-api";
import {
    Badge,
    Button,
    Card,
    Col,
    Form,
    ProgressBar,
    Row,
    Spinner,
} from "react-bootstrap";
import { ArrowClockwise } from "react-bootstrap-icons";
import ReloadButton from "../../shared/reload-button";

interface Props {
    name: string;
}
function SchedulerStatusView({ name }: Props) {
    const status = useServerObject<SchedulerEntity>(
        `/spring-tasks-api/schedulers/${name}`
    );

    useEffect(status.doGet, [name]);

    return (
        <Card>
            <Card.Header
                as="h5"
                className="d-flex justify-content-between align-items-center"
            >
                <span>
                    {name}{" "}
                    <TaskSchedulerStatusView status={status.data?.status} />
                </span>
                <ReloadButton
                    isLoading={status.isLoading}
                    onClick={() => status.doGet()}
                />
            </Card.Header>
            {status.data ? (
                <Card.Body>
                    <Row>
                        <Col>
                            Last Ping:{" "}
                            {durationSince(new Date(status.data.lastPing))}
                        </Col>
                        <Col>
                            <Form.Label htmlFor={"slot-" + name}>
                                {"Running " +
                                    status.data.runnungTasks +
                                    " of " +
                                    status.data.tasksSlotCount}
                            </Form.Label>
                            <ProgressBar
                                id={"slot-" + name}
                                animated={true}
                                min={0}
                                now={status.data.runnungTasks}
                                max={status.data.tasksSlotCount}
                            ></ProgressBar>
                        </Col>
                    </Row>
                    <Row>
                        <Col>
                            <Form.Label htmlFor={"cpu-" + name}>CPU</Form.Label>
                            <ProgressBar
                                id={"cpu-" + name}
                                animated={true}
                                min={0}
                                now={status.data.systemLoadAverage}
                                max={100}
                                label={
                                    Math.round(
                                        status.data.systemLoadAverage * 10
                                    ) /
                                        10 +
                                    "%"
                                }
                            ></ProgressBar>
                        </Col>
                        <Col>
                            <Form.Label htmlFor={"memory-" + name}>
                                Memory{" "}
                                {formatMemory(status.data.usedHeap) +
                                    " of " +
                                    formatMemory(status.data.maxHeap)}
                            </Form.Label>
                            <ProgressBar
                                id={"memory-" + name}
                                animated={true}
                                min={0}
                                now={status.data.usedHeap}
                                max={status.data.maxHeap}
                                label={formatMemory(status.data.usedHeap)}
                            ></ProgressBar>
                        </Col>
                    </Row>
                </Card.Body>
            ) : undefined}
        </Card>
    );
}

export default SchedulerStatusView;

function durationSince(from: Date) {
    let diff =
        (new Date().getMilliseconds() - from.getMilliseconds()) / 1000 / 60;

    if (diff < 1) return "just now";
    if (diff < 60) {
        return "seit " + Math.round(diff) + " min";
    } else if (diff < 3600) {
        return "seit " + Math.round(diff / 60) + " hour(s)";
    } else {
        return "seit " + Math.round(diff / 3600) + " day(s)";
    }
}

function formatMemory(value: number) {
    var result = value / 1024 / 1024;

    if (result > 999) {
        return Math.round((result / 1024) * 10) / 10 + "GB";
    }
    return Math.round(result) + "MB";
}

function TaskSchedulerStatusView({ status }: { status?: TaskSchedulerStatus }) {
    if (!status) return undefined;
    if (status === "ONLINE") {
        return (
            <Badge pill bg="success">
                {status}
            </Badge>
        );
    }
    return (
        <Badge pill bg="warning" text="dark">
            {status}
        </Badge>
    );
}
