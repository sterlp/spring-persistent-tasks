import { SchedulerEntity } from "@src/server-api";
import { useServerObject } from "@src/shared/http-request";
import ReloadButton from "@src/shared/view/reload-button.view";
import useAutoRefresh from "@src/shared/use-auto-refresh";
import { DateTime } from "luxon";
import { Card, Col, Form, ProgressBar, Row } from "react-bootstrap";

interface Props {
    name: string;
}
const SchedulerStatusView = ({ name }: Props) => {
    const status = useServerObject<SchedulerEntity>(
        `/spring-tasks-api/schedulers/${name}`
    );

    useAutoRefresh(10000, status.doGet, [name]);

    return (
        <Card>
            <Card.Header
                as="h5"
                className="d-flex justify-content-between align-items-center"
            >
                <span>{name}</span>
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
};

export default SchedulerStatusView;

function durationSince(from: Date) {
    const now = DateTime.now();
    const diff = now.diff(DateTime.fromJSDate(from), [
        "days",
        "hours",
        "minutes",
        "seconds",
    ]);
    const days = diff.days;
    const hours = diff.hours;
    const minutes = diff.minutes;
    const seconds = diff.seconds;
    const result = [];
    if (days > 0) result.push(`${Math.floor(days)}d`);
    if (hours > 0) result.push(`${Math.floor(hours)}h`);
    if (minutes > 0) result.push(`${Math.floor(minutes)}min`);
    if (result.length === 0 || (days === 0 && seconds > 0))
        result.push(`${Math.floor(seconds)}s`);
    return result.join(" ") + " ago";
}

function formatMemory(value: number) {
    const result = value / 1024 / 1024;

    if (result > 999) {
        return Math.round((result / 1024) * 10) / 10 + "GB";
    }
    return Math.round(result) + "MB";
}
