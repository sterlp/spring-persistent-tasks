import { DateTime } from "luxon";
import { useEffect, useState } from "react";
import {
    Card,
    Col,
    ProgressBar,
    Row,
    Tooltip,
    OverlayTrigger,
} from "react-bootstrap";
import {
    Bar,
    BarChart,
    CartesianGrid,
    ResponsiveContainer,
    XAxis,
    YAxis,
} from "recharts";
import { SchedulerEntity } from "spring-persistent-tasks-ui";

interface Props {
    scheduler: SchedulerEntity;
}

const SchedulerStatusView = ({ scheduler }: Props) => {
    const [historyData, setHistoryData] = useState<
        { time: string; tasks: number; ping: string }[]
    >([]);

    useEffect(() => {
        setHistoryData((prev) => {
            // Prevent duplicate entries if lastPing hasn't changed
            const lastEntry = prev[prev.length - 1];
            const lastPingChanged =
                !lastEntry || scheduler.lastPing !== lastEntry.ping;

            if (lastPingChanged) {
                return [
                    ...prev.slice(-10), // Keep only the last 14 entries
                    {
                        time: new Date(scheduler.lastPing).toLocaleTimeString(), // Format timestamp
                        tasks: scheduler.runningTasks ?? 0, // Running tasks
                        ping: scheduler.lastPing,
                    },
                ];
            }
            return prev; // No update if lastPing hasn't changed
        });
    }, [scheduler.lastPing]); // Depends only on lastPing

    return (
        <Card className="shadow-sm border-0 h-100">
            <Card.Header className="bg-primary text-white py-3">
                <h6 className="mb-0 fw-semibold">
                    <i className="fas fa-server me-2"></i>
                    {scheduler.id}
                </h6>
            </Card.Header>
            <Card.Body className="p-3">
                <div className="mb-3 pb-2 border-bottom">
                    <small className="text-muted">Last Ping</small>
                    <div className="fw-semibold">
                        {durationSince(new Date(scheduler.lastPing))}
                    </div>
                </div>

                <Row className="g-3">
                    <Col lg={6}>
                        <div className="mb-3">
                            {renderLoadStatus(
                                "Threads",
                                scheduler.runningTasks,
                                scheduler.tasksSlotCount,
                                "t_" + scheduler.id
                            )}
                        </div>
                        <div className="mb-3">
                            {scheduler.systemLoadAverage >= 0 ? (
                                renderLoadStatus(
                                    "CPU",
                                    scheduler.systemLoadAverage,
                                    100,
                                    "cpu" + scheduler.id,
                                    "%"
                                )
                            ) : (
                                <div>
                                    <div className="d-flex justify-content-between align-items-center mb-1">
                                        <small className="text-muted">CPU</small>
                                        <small className="text-muted">N/A</small>
                                    </div>
                                    <div className="progress">
                                        <div className="progress-bar bg-secondary" style={{ width: "100%" }}>
                                            System load unavailable
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>
                        <div>
                            {renderLoadStatus(
                                "Memory",
                                scheduler.usedHeap / 1024 / 1024,
                                scheduler.maxHeap / 1024 / 1024,
                                "memory" + scheduler.id,
                                "MB"
                            )}
                        </div>
                    </Col>

                    <Col lg={6}>
                        <div className="mb-2">
                            <small className="text-muted">Running Tasks History</small>
                        </div>
                        <ResponsiveContainer width="100%" height={180}>
                            <BarChart data={historyData}>
                                <CartesianGrid strokeDasharray="3 3" stroke="#e0e0e0" />
                                <XAxis
                                    dataKey="time"
                                    tick={{ fontSize: 11 }}
                                    stroke="#6c757d"
                                />
                                <YAxis
                                    tick={{ fontSize: 11 }}
                                    stroke="#6c757d"
                                />
                                <Tooltip />
                                <Bar dataKey="tasks" fill="#0d6efd" radius={[4, 4, 0, 0]} />
                            </BarChart>
                        </ResponsiveContainer>
                    </Col>
                </Row>
            </Card.Body>
        </Card>
    );
};

export default SchedulerStatusView;

const renderLoadStatus = (
    label: string,
    current: number,
    max: number,
    id: string,
    unit: string = ""
) => {
    const percentage = (current / max) * 100;
    const variant = getVariant(current, max);

    return (
        <div>
            <div className="d-flex justify-content-between align-items-center mb-1">
                <small className="text-muted">{label}</small>
                <small className="fw-semibold">
                    {Math.round(current)}{unit} / {Math.round(max)}{unit}
                </small>
            </div>
            <OverlayTrigger
                placement="top"
                overlay={(props) =>
                    renderTooltip(
                        props,
                        `${label}: ${Math.round(current)}${unit} of ${Math.round(max)}${unit} (${Math.round(percentage)}%)`
                    )
                }
            >
                <ProgressBar
                    id={`progress-${id}`}
                    variant={variant}
                    now={current}
                    max={max}
                    style={{ height: '8px' }}
                    className="rounded"
                />
            </OverlayTrigger>
        </div>
    );
};

function renderTooltip(props: any, label: string) {
    return (
        <Tooltip id="button-tooltip" {...props}>
            {label}
        </Tooltip>
    );
}

function durationSince(from: Date) {
    const now = DateTime.now();
    const diff = now.diff(DateTime.fromJSDate(from), [
        "days",
        "hours",
        "minutes",
        "seconds",
    ]);
    const result = [];
    if (diff.days > 0) result.push(`${Math.floor(diff.days)}d`);
    if (diff.hours > 0) result.push(`${Math.floor(diff.hours)}h`);
    if (diff.minutes > 0) result.push(`${Math.floor(diff.minutes)}min`);
    if (result.length === 0 || (diff.days === 0 && diff.seconds > 0))
        result.push(`${Math.floor(diff.seconds)}s`);
    return result.join(" ") + " ago";
}

function getVariant(value: number, max: number) {
    if (max <= 0 || value < 0) {
        console.warn(
            "Invalid input: max must be greater than zero and value must be non-negative."
        );
        return "danger";
    }
    const percentage = (value / max) * 100;
    return percentage < 80
        ? "success"
        : percentage < 100
        ? "warning"
        : "danger";
}
