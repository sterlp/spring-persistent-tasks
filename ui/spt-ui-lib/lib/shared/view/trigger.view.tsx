import type { Trigger } from "@lib/server-api";
import JsonView from "@uiw/react-json-view";
import { Col, Row, type ColProps } from "react-bootstrap";
import { formatShortDateTime } from "../date.util";
import LabeledText from "./labled-text.view";
import StackTraceView from "./stacktrace.view";
import TriggerHistoryListView from "./trigger-history-list.view";

const TriggerView = ({
    trigger,
    col = { md: 3, xs: 6 },
    onClick,
}: {
    trigger: Trigger;
    col?: ColProps;
    onClick?: (key: string, value?: string) => void;
}) => {
    return (
        <>
            <Row>
                <Col {...col}>
                    <LabeledText
                        label="Task"
                        value={trigger.key.taskName}
                        onClick={() =>
                            onClick
                                ? onClick("taskName", trigger.key.taskName)
                                : undefined
                        }
                    />
                </Col>
                <Col {...col}>
                    <LabeledText
                        label="Correlation Id"
                        value={trigger.correlationId}
                        onClick={() =>
                            onClick
                                ? onClick("search", trigger.correlationId)
                                : undefined
                        }
                    />
                </Col>
                <Col {...col}>
                    <LabeledText
                        label="Tag"
                        value={trigger.tag}
                        onClick={() =>
                            onClick ? onClick("search", trigger.tag) : undefined
                        }
                    />
                </Col>
                <Col {...col}>
                    <LabeledText label="Priority" value={trigger.priority} />
                </Col>

                <Col {...col}>
                    <LabeledText
                        label="Run at"
                        value={formatShortDateTime(trigger.runAt)}
                    />
                </Col>
                <Col {...col}>
                    <LabeledText
                        label="Started at"
                        value={formatShortDateTime(trigger.start)}
                    />
                </Col>
                <Col {...col}>
                    <LabeledText
                        label="Finished at"
                        value={formatShortDateTime(trigger.end)}
                    />
                </Col>
                <Col {...col}>
                    <LabeledText
                        label="Execution count"
                        value={trigger.executionCount}
                    />
                </Col>
                <Col {...col}>
                    <LabeledText
                        label="Created at"
                        value={formatShortDateTime(trigger.createdTime)}
                    />
                </Col>
                <Col {...col}>
                    <LabeledText
                        label="Last keep alive"
                        value={formatShortDateTime(trigger.lastPing)}
                    />
                </Col>
                <Col {...col}>
                    <LabeledText label="Running on" value={trigger.runningOn} />
                </Col>
            </Row>

            <Row className="mt-2">
                <Col>
                    <TriggerHistoryListView instanceId={trigger.instanceId} />
                </Col>
            </Row>
            <hr />
            <Row className="mt-2">
                <Col>
                    {isObject(trigger.state) ? (
                        <JsonView value={trigger.state} />
                    ) : (
                        <pre>{trigger.state}</pre>
                    )}
                </Col>
                <Col>
                    <StackTraceView
                        key={trigger.id + "-error-view"}
                        title={trigger.exceptionName}
                        error={trigger.lastException}
                    />
                </Col>
            </Row>
        </>
    );
};

export default TriggerView;

function isObject(value: unknown): boolean {
    if (value === undefined || value === null) return false;
    return typeof value === "object" || Array.isArray(value);
}
