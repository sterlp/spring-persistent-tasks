import { type Trigger } from "@lib/server-api";
import { formatDateTime, formatMs } from "@lib/shared/date.util";
import { Alert, Col, ListGroup, Row } from "react-bootstrap";
import RunningTriggerStatusView from "./running-trigger-status.view";

const TriggerHistoryListView = ({ triggers }: { triggers?: Trigger[] }) => {
    if (!triggers || triggers.length === 0)
        return <Alert variant="info">No trigger history available yet.</Alert>;
    return (
        <ListGroup variant="flush">
            {triggers.map((t) => (
                <ListGroup.Item
                    key={t.id}
                    style={{ paddingLeft: 0, paddingRight: 0 }}
                >
                    <Row>
                        <Col>
                            <RunningTriggerStatusView data={t} />
                        </Col>
                        <Col>{formatDateTime(t.createdTime)}</Col>
                        <Col>execution: {t.executionCount}</Col>
                        <Col>{formatMs(t.runningDurationInMs)}</Col>
                    </Row>
                </ListGroup.Item>
            ))}
        </ListGroup>
    );
};

export default TriggerHistoryListView;
