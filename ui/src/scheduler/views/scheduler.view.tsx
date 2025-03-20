import { SchedulerEntity } from "@src/server-api";
import { DateTime } from "luxon";
import { useEffect, useState } from "react";
import {
    Card,
    Col,
    Form,
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
    }, [scheduler.lastPing]); // Depend only on lastPing

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
                    {/* Left Column: ProgressBars */}
                    <Col>
                        <p>
                            <strong>Last Ping:</strong>{" "}
                            {durationSince(new Date(scheduler.lastPing))}
                        </p>
                        {renderLoadStatus(
                            "Threads",
                            scheduler.runningTasks,
                            scheduler.tasksSlotCount,
                            "t_" + scheduler.id
                        )}
                        {renderLoadStatus(
                            "CPU",
                            scheduler.systemLoadAverage,
                            100,
                            "cpu" + scheduler.id,
                            "%"
                        )}
                        {renderLoadStatus(
                            "Memory",
                            scheduler.usedHeap / 1024 / 1024,
                            scheduler.maxHeap / 1024 / 1024,
                            "memory" + scheduler.id,
                            "MB"
                        )}
                    </Col>

                    {/* Right Column: BarChart */}
                    <Col>
                        <strong>Running Tasks:</strong>
                        <ResponsiveContainer width="100%" height={200}>
                            <BarChart data={historyData}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis
                                    dataKey="time"
                                    label={{
                                        value: "Time",
                                        position: "insideBottom",
                                        offset: -5,
                                    }}
                                />
                                <YAxis
                                    label={{
                                        value: "Tasks",
                                        angle: -90,
                                        position: "insideLeft",
                                    }}
                                />
                                <Tooltip />
                                <Bar dataKey="tasks" fill="#007bff" />
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
) => (
    <>
        <Form.Label htmlFor={`label-${id}`}>{label}</Form.Label>
        <OverlayTrigger
            placement="top"
            overlay={(props) =>
                renderTooltip(
                    props,
                    `${label}: ${Math.round(current)}${unit} of ${Math.round(
                        max
                    )}${unit}`
                )
            }
        >
            <ProgressBar
                id={`progress-${id}`}
                animated
                striped
                variant={getVariant(current, max)}
                now={current}
                max={max}
                label={`${Math.round(current)}${unit}`}
            />
        </OverlayTrigger>
    </>
);

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
