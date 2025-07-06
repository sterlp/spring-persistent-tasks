import type { TriggerGroup } from "@lib/server-api";
import { Col, Row } from "react-bootstrap";
import { formatDateTime, formatMs } from "../date.util";
import LabeledText from "./labled-text.view";
import { forwardRef } from "react";

interface TriggerGroupViewProps extends React.ComponentPropsWithoutRef<"div"> {
    triggerGroup: TriggerGroup;
    onGroupClick?: (v: string) => void;
}

const TriggerGroupView = forwardRef<HTMLDivElement, TriggerGroupViewProps>(
    ({ triggerGroup, onGroupClick, ...rest }, ref) => (
        <Row ref={ref} {...rest}>
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
                    value={formatDateTime(
                        triggerGroup.minStart || triggerGroup.minRunAt
                    )}
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
    )
);

export default TriggerGroupView;
