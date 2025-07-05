import type { TriggerGroup } from "@lib/server-api";
import { Col, Row } from "react-bootstrap";
import { formatDateTime, formatMs } from "../date.util";
import LabeledText from "./labled-text.view";

interface TriggerGroupViewProps {
    triggerGroup: TriggerGroup;
    onGroupClick?: (v: string) => void;
}

const TriggerGroupView = ({
    triggerGroup,
    onGroupClick,
}: TriggerGroupViewProps) => (
    <Row>
        <Col sm="5">
            <LabeledText
                label="CorrelationId"
                value={triggerGroup.groupByValue}
                onClick={
                    onGroupClick
                        ? () => onGroupClick(triggerGroup.groupByValue)
                        : undefined
                }
            />
        </Col>
        <Col sm="1">
            <LabeledText label="Steps" value={triggerGroup.count} />
        </Col>
        <Col sm="2">
            <LabeledText
                label="Start"
                value={formatDateTime(triggerGroup.minStart)}
            />
        </Col>
        <Col sm="2">
            <LabeledText
                label="End"
                value={formatDateTime(triggerGroup.maxEnd)}
            />
        </Col>
        <Col sm="2">
            <LabeledText
                label="Duration"
                value={formatMs(triggerGroup.sumDurationMs)}
            />
        </Col>
    </Row>
);

export default TriggerGroupView;
