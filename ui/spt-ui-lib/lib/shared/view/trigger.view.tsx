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
        <div className="p-3 bg-body-secondary rounded">
            <div className="mb-3">
                <h6 className="text-secondary mb-3 border-bottom pb-2">Trigger Details</h6>
                <Row className="g-3">
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
            </div>

            <div className="mb-3">
                <h6 className="text-secondary mb-2 border-bottom pb-2">Execution History</h6>
                <TriggerHistoryListView instanceId={trigger.instanceId} />
            </div>

            <Row className="g-3">
                <Col md={6}>
                    <h6 className="text-secondary mb-2 border-bottom pb-2">State</h6>
                    {isObject(trigger.state) ? (
                        <JsonView value={trigger.state} style={{ fontSize: '0.875rem' }} />
                    ) : (
                        <pre className="bg-body p-2 rounded border">{trigger.state}</pre>
                    )}
                </Col>
                <Col md={6}>
                    <h6 className="text-secondary mb-2 border-bottom pb-2">Exception</h6>
                    <StackTraceView
                        key={trigger.id + "-error-view"}
                        title={trigger.exceptionName}
                        error={trigger.lastException}
                    />
                </Col>
            </Row>
        </div>
    );
};

export default TriggerView;

function isObject(value: unknown): boolean {
    if (value === undefined || value === null) return false;
    return typeof value === "object" || Array.isArray(value);
}
