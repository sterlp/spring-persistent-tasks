import { SchedulerEntity } from "@src/server-api";
import { DateTime } from "luxon";
import { Card, Col, Form, ProgressBar, Row } from "react-bootstrap";

interface Props {
    scheduler: SchedulerEntity;
}
const SchedulerStatusView = ({ scheduler }: Props) => {
    return (
        <Card>
            <Card.Header
                as="h5"
                className="d-flex justify-content-between align-items-center"
            >
                <span>{scheduler.id}</span>
            </Card.Header>
            <Card.Body>
                <Row>
                    <Col>
                        Last Ping: {durationSince(new Date(scheduler.lastPing))}
                    </Col>
                    <Col>
                        <Form.Label htmlFor={"slot-" + name}>
                            {"Running " +
                                scheduler.runnungTasks +
                                " of " +
                                scheduler.tasksSlotCount}
                        </Form.Label>
                        <ProgressBar
                            id={"slot-" + name}
                            animated={true}
                            min={0}
                            now={scheduler.runnungTasks}
                            max={scheduler.tasksSlotCount}
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
                            now={scheduler.systemLoadAverage}
                            max={100}
                            label={
                                Math.round(scheduler.systemLoadAverage * 10) /
                                    10 +
                                "%"
                            }
                        ></ProgressBar>
                    </Col>
                    <Col>
                        <Form.Label htmlFor={"memory-" + name}>
                            Memory{" "}
                            {formatMemory(scheduler.usedHeap) +
                                " of " +
                                formatMemory(scheduler.maxHeap)}
                        </Form.Label>
                        <ProgressBar
                            id={"memory-" + name}
                            animated={true}
                            min={0}
                            now={scheduler.usedHeap}
                            max={scheduler.maxHeap}
                            label={formatMemory(scheduler.usedHeap)}
                        ></ProgressBar>
                    </Col>
                </Row>
            </Card.Body>
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
