import JsonView from "@uiw/react-json-view";
import { Col, Row } from "react-bootstrap";
import { formatMs, formatShortDateTime, runningSince } from "../date.util";
import LabeledText from "./labled-text.view";
import StackTraceView from "./stacktrace.view";
import TriggerHistoryListView from "./trigger-history-list.view";
import type { Trigger } from "@lib/server-api";

const TriggerView = ({
    trigger,
    history,
    onClick,
}: {
    trigger: Trigger;
    history?: Trigger[];
    onClick: (key: string, value?: string) => void;
}) => {
    return (
        <>
            <Row>
                <Col md="6" xl="4">
                    <LabeledText
                        label="Key Id"
                        value={trigger.key.id}
                        onClick={() => onClick("search", trigger.key.id)}
                    />
                </Col>
                <Col md="6" xl="4">
                    <LabeledText
                        label="Task"
                        value={trigger.key.taskName}
                        onClick={() =>
                            onClick("taskName", trigger.key.taskName)
                        }
                    />
                </Col>
                <Col md="6" xl="4">
                    <LabeledText label="Priority" value={trigger.priority} />
                </Col>
            </Row>
            <Row>
                <Col md="6" xl="4">
                    <LabeledText
                        label="Correlation Id"
                        value={trigger.correlationId}
                        onClick={() => onClick("search", trigger.key.id)}
                    />
                </Col>
                <Col md="6" xl="4">
                    <LabeledText
                        label="Tag"
                        value={trigger.tag}
                        onClick={() => onClick("search", trigger.key.id)}
                    />
                </Col>
                <Col md="6" xl="4">
                    <LabeledText
                        label="Run at"
                        value={formatShortDateTime(trigger.runAt)}
                    />
                </Col>
            </Row>
            <Row>
                <Col md="6" xl="4">
                    <LabeledText
                        label="Started at"
                        value={formatShortDateTime(trigger.start)}
                    />
                </Col>
                <Col md="6" xl="4">
                    <LabeledText
                        label="Finished at"
                        value={formatShortDateTime(trigger.end)}
                    />
                </Col>
                <Col md="6" xl="4">
                    <LabeledText
                        label="Duration MS"
                        value={
                            trigger.runningDurationInMs
                                ? formatMs(trigger.runningDurationInMs)
                                : runningSince(trigger.start)
                        }
                    />
                </Col>
            </Row>
            {trigger.lastPing ? (
                <Row>
                    <Col md="12">
                        <LabeledText
                            label="Last keep alive ping"
                            value={formatShortDateTime(trigger.lastPing)}
                        />
                    </Col>
                </Row>
            ) : undefined}
            <Row className="mt-2">
                <Col>
                    <TriggerHistoryListView triggers={history} />
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
