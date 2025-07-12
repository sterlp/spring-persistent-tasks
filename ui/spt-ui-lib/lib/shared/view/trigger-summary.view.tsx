import type { Trigger } from "@lib/server-api";
import { Badge, Col, Row, type ColProps } from "react-bootstrap";
import LabeledText from "./labled-text.view";
import { formatMs, formatShortDateTime, runningSince } from "../date.util";

const TriggerSummaryView = ({
    trigger,
    col = { md: 3, xs: 6 },
}: {
    trigger: Trigger;
    col?: ColProps;
}) => {
    return (
        <Row>
            <Col {...col}>
                <LabeledText
                    label="Start"
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
                <TriggerExecutiomView trigger={trigger} />
            </Col>
            <Col {...col}>
                <LabeledText
                    label="Correlation Id"
                    value={trigger.correlationId}
                />
            </Col>
            <Col {...col}>
                <LabeledText label="Tag" value={trigger.tag} />
            </Col>
            <Col {...col}>
                <LabeledText label="Priority" value={trigger.priority} />
            </Col>
            <Col {...col}>
                <LabeledText
                    label="Last keep alive ping"
                    value={formatShortDateTime(trigger.lastPing)}
                />
            </Col>
        </Row>
    );
};

export default TriggerSummaryView;

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
            label="Count & Time"
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
