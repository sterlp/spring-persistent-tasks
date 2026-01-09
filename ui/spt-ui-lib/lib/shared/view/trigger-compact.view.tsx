import type { Trigger } from "@lib/server-api";
import LabeledText from "@lib/shared/view/labled-text.view";
import { Badge, Col, Row } from "react-bootstrap";
import { formatMs, formatShortDateTime, runningSince } from "../date.util";
import TriggerStatusIcon from "./trigger-status-icon.view";

const TriggerCompactView = ({ trigger }: { trigger: Trigger }) => (
    <Row className="align-items-center g-2 w-100">
        <Col xs={12} md={3} lg={2}>
            <TriggerStatusIcon trigger={trigger} />
        </Col>
        <Col xs={12} md={5} lg={4}>
            <small className="text-muted d-block">{trigger.key.id}</small>
            <div className="fw-semibold">{trigger.key.taskName}</div>
        </Col>
        <Col xs={6} md={4} lg={3}>
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
        <Col xs={6} md={12} lg={3}>
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
