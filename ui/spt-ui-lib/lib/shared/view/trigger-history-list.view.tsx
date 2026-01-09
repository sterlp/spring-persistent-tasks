import { type HistoryTrigger, type PagedModel } from "@lib/server-api";
import { formatDateTime } from "@lib/shared/date.util";
import { useEffect } from "react";
import { Col, Row } from "react-bootstrap";
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
                <div className="d-flex flex-column gap-2">
                    {history.content.map((t) => (
                        <div
                            key={t.id}
                            className="border rounded p-2 bg-body"
                        >
                            <Row className="align-items-center g-2">
                                <Col xs={12} md={3} lg={2}>
                                    <div className="d-flex align-items-center gap-2">
                                        <span className="badge bg-secondary">#{t.executionCount}</span>
                                        <TriggerStatusView status={t.status} />
                                    </div>
                                </Col>
                                <Col xs={12} md={3} lg={2}>
                                    <small className="text-muted">
                                        {formatDateTime(t.createdTime)}
                                    </small>
                                </Col>
                                <Col xs={12} md={6} lg={8}>
                                    <small>{t.message}</small>
                                </Col>
                            </Row>
                        </div>
                    ))}
                </div>
            )}
        />
    );
};

export default TriggerHistoryListView;
