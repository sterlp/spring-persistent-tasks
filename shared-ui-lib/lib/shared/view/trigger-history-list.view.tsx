import { formatDateTime, formatMs } from "@lib/shared/date.util";
import { Col, ListGroup, Row, Spinner } from "react-bootstrap";
import TriggerStatusView from "./running-trigger-status.view";
import { type Trigger } from "@lib/server-api";

const TriggerHistoryListView = ({ triggers }: { triggers?: Trigger[] }) => {
    if (!triggers) return <Spinner />;
    if (triggers.length === 0) return undefined;
    return (
        <ListGroup>
            <ListGroup.Item
                active
                key={"header-" + triggers.length + "" + triggers[0].id}
            >
                History ({triggers.length})
            </ListGroup.Item>
            {triggers.map((t) => (
                <ListGroup.Item key={t.id}>
                    <Row>
                        <Col>
                            <TriggerStatusView data={t} />
                        </Col>
                        <Col>
                            <strong>{formatDateTime(t.createdTime)}</strong>
                        </Col>
                        <Col>execution: {t.executionCount}</Col>
                        <Col>{formatMs(t.runningDurationInMs)}</Col>
                    </Row>
                </ListGroup.Item>
            ))}
        </ListGroup>
    );
};

export default TriggerHistoryListView;
