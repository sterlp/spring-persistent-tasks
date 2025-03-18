import { SchedulerEntity } from "@src/server-api";
import { DateTime } from "luxon";
import { Card, Col, Form, ProgressBar, Row } from "react-bootstrap";

interface Props {
    scheduler: SchedulerEntity;
}
import { Tooltip, OverlayTrigger } from "react-bootstrap";

const SchedulerStatusView = ({ scheduler }: Props) => {
    const renderTooltip = (props: any, label: string) => (
        <Tooltip id="button-tooltip" {...props}>
            {label}
        </Tooltip>
    );

    return (
        <Card className="shadow-sm rounded border-0">
            <Card.Header
                as="h5"
                className="d-flex justify-content-between align-items-center"
                style={{
                    background: "linear-gradient(45deg, #007bff, #00d8ff)",
                    color: "white",
                }}
            >
                <span>
                    <i className="fas fa-server"></i> {scheduler.id}
                </span>
            </Card.Header>
            <Card.Body>
                <Row>
                    <Col>
                        <strong>Last Ping:</strong>{" "}
                        {durationSince(new Date(scheduler.lastPing))}
                    </Col>
                    <Col>
                        <Form.Label htmlFor={"slot-" + scheduler.id}>
                            {"Running " +
                                scheduler.runningTasks +
                                " of " +
                                scheduler.tasksSlotCount}
                        </Form.Label>
                        <OverlayTrigger
                            placement="top"
                            overlay={(props) =>
                                renderTooltip(
                                    props,
                                    `${scheduler.runningTasks} tasks running`
                                )
                            }
                        >
                            <ProgressBar
                                id={"slot-" + scheduler.id}
                                animated={true}
                                striped
                                variant={getVariant(
                                    scheduler.runningTasks,
                                    scheduler.tasksSlotCount
                                )}
                                now={scheduler.runningTasks}
                                max={scheduler.tasksSlotCount}
                            />
                        </OverlayTrigger>
                    </Col>
                </Row>
                <Row>
                    <Col>
                        <Form.Label htmlFor={"cpu-" + scheduler.id}>
                            CPU
                        </Form.Label>
                        <OverlayTrigger
                            placement="top"
                            overlay={(props) =>
                                renderTooltip(
                                    props,
                                    `CPU Load: ${scheduler.systemLoadAverage}%`
                                )
                            }
                        >
                            <ProgressBar
                                id={"cpu-" + scheduler.id}
                                animated={true}
                                striped
                                variant={getVariant(
                                    scheduler.systemLoadAverage,
                                    100
                                )}
                                now={scheduler.systemLoadAverage}
                                max={100}
                                label={`${
                                    Math.round(
                                        scheduler.systemLoadAverage * 10
                                    ) / 10
                                }%`}
                            />
                        </OverlayTrigger>
                    </Col>
                    <Col>
                        <Form.Label htmlFor={"memory-" + scheduler.id}>
                            Memory{" "}
                            {formatMemory(scheduler.usedHeap) +
                                " of " +
                                formatMemory(scheduler.maxHeap)}
                        </Form.Label>
                        <OverlayTrigger
                            placement="top"
                            overlay={(props) =>
                                renderTooltip(
                                    props,
                                    `${formatMemory(scheduler.usedHeap)} used`
                                )
                            }
                        >
                            <ProgressBar
                                id={"memory-" + scheduler.id}
                                animated={true}
                                striped
                                variant={getVariant(
                                    scheduler.usedHeap,
                                    scheduler.maxHeap
                                )}
                                now={scheduler.usedHeap}
                                max={scheduler.maxHeap}
                                label={formatMemory(scheduler.usedHeap)}
                            />
                        </OverlayTrigger>
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

function getVariant(value: number, max: number) {
    const percentage = (value / max) * 100;
    if (percentage < 80) return "success"; // Grün
    if (percentage < 100) return "warning"; // Gelb
    return "danger"; // Rot
}
