import type { Trigger } from "@lib/server-api";
import LabeledText from "@lib/shared/view/labled-text.view";
import { Badge, Col, Form, Row } from "react-bootstrap";
import { formatMs, formatShortDateTime, runningSince } from "../date.util";
import RunningTriggerStatusView from "./running-trigger-status.view";

const TriggerCompactView = ({ trigger }: { trigger: Trigger }) => (
    <Row className="align-items-center">
        <Col className="col-2">
            <RunningTriggerStatusView data={trigger} />
        </Col>
        <Col className="col-5">
            <Form.Text muted role="label">
                {trigger.key.id}
            </Form.Text>
            <div>{trigger.key.taskName}</div>
        </Col>
        <Col className="col-2">
            {isActive(trigger) ? (
                <LabeledText
                    label="Run at"
                    value={formatShortDateTime(trigger.runAt)}
                />
            ) : (
                <LabeledText
                    label="Finished at"
                    value={formatShortDateTime(trigger.end)}
                />
            )}
        </Col>
        <Col className="col-3">
            <TriggerExecutiomView trigger={trigger} />
        </Col>
    </Row>
);

const TriggerExecutiomView = ({ trigger }: { trigger: Trigger }) => {
    if (trigger.runningOn) {
        return (
            <LabeledText
                label={`Running on (${trigger.executionCount})`}
                value={trigger.runningOn + " " + runningSince(trigger.start)}
            />
        );
    }
    if (trigger.status == "WAITING" && trigger.runAt) {
        return (
            <LabeledText
                label={`Should start in`}
                value={formatMs(
                    new Date(trigger.runAt).getTime() - new Date().getTime()
                )}
            />
        );
    }
    return (
        <LabeledText
            label="Executions"
            value={
                <div className="d-flex justify-content-start align-items-center">
                    <Badge
                        bg={trigger.executionCount > 1 ? "warning" : "success"}
                    >
                        {trigger.executionCount}
                    </Badge>
                    <span className="ms-2">
                        {formatMs(trigger.runningDurationInMs)}
                    </span>
                </div>
            }
        />
    );
};

export default TriggerCompactView;

function isActive(trigger: Trigger): boolean {
    return trigger.status == "RUNNING" || trigger.status == "WAITING";
}
