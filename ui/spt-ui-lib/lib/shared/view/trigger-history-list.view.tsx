import { type HistoryTrigger, type PagedModel } from "@lib/server-api";
import { formatDateTime } from "@lib/shared/date.util";
import { useEffect } from "react";
import { Col, ListGroup, Row } from "react-bootstrap";
import { useServerObject } from "../http-request";
import HttpRequestView from "./http-request.view";
import TriggerStatusView from "./trigger-status.view";

const TriggerHistoryListView = ({ instanceId }: { instanceId: number }) => {
    const triggerHistory = useServerObject<PagedModel<HistoryTrigger>>(
        "/spring-tasks-api/history/instance/"
    );
    const doGet = triggerHistory.doGet;
    useEffect(() => doGet(instanceId), [instanceId]);

    return (
        <HttpRequestView
            request={triggerHistory}
            render={(history) => (
                <ListGroup variant="flush">
                    {history.content.map((t) => (
                        <ListGroup.Item
                            key={t.id}
                            style={{ paddingLeft: 0, paddingRight: 0 }}
                        >
                            <Row className="align-middle">
                                <Col md="3" xl="2">
                                    <strong>#{t.executionCount} - </strong>
                                    <TriggerStatusView status={t.status} />
                                </Col>
                                <Col md="3" xl="2">
                                    {formatDateTime(t.createdTime)}
                                </Col>
                                <Col
                                    md="6"
                                    xl="8"
                                    className="text-justify text-wrap"
                                >
                                    {t.message}
                                </Col>
                            </Row>
                        </ListGroup.Item>
                    ))}
                </ListGroup>
            )}
        />
    );
};

export default TriggerHistoryListView;
