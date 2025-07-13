import { type Trigger } from "@lib/server-api";
import { formatDateTime, formatMs } from "@lib/shared/date.util";
import { useEffect } from "react";
import { Col, ListGroup, Row } from "react-bootstrap";
import { useServerObject } from "../http-request";
import HttpRequestView from "./http-request.view";
import RunningTriggerStatusView from "./running-trigger-status.view";

const TriggerHistoryListView = ({ instanceId }: { instanceId: number }) => {
    const triggerHistory = useServerObject<Trigger[]>(
        "/spring-tasks-api/history/instance/"
    );
    const doGet = triggerHistory.doGet;
    useEffect(() => doGet(instanceId), [instanceId]);

    return (
        <HttpRequestView
            request={triggerHistory}
            render={(history) => (
                <ListGroup variant="flush">
                    {history.map((t) => (
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
            )}
        />
    );
};

export default TriggerHistoryListView;
