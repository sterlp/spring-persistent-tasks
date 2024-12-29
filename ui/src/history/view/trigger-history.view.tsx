import { Trigger } from "@src/server-api";
import { formatDateTime } from "@src/shared/date.util";
import TriggerStatusView from "@src/trigger/views/trigger-staus.view";
import { Col, ListGroup, Row, Spinner } from "react-bootstrap";

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
                            <strong>
                                {" " + formatDateTime(t.createdTime)}:
                            </strong>
                        </Col>
                        <Col>
                            {formatDateTime(t.start)} - {formatDateTime(t.end)}{" "}
                        </Col>
                        <Col>{t.executionCount}</Col>
                        <Col>{t.runningDurationInMs}ms</Col>
                    </Row>
                </ListGroup.Item>
            ))}
        </ListGroup>
    );
};

export default TriggerHistoryListView;
