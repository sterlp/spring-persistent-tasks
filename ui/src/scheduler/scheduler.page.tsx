import SchedulerStatusView from "@src/scheduler/views/scheduler.view";
import HttpErrorView from "@src/shared/view/http-error.view";
import { useServerObject } from "@src/shared/http-request";
import useAutoRefresh from "@src/shared/use-auto-refresh";
import { useEffect } from "react";
import { Col, Row } from "react-bootstrap";

const SchedulersPage = () => {
    const schedulers = useServerObject<string[]>(
        "/spring-tasks-api/schedulers"
    );
    const tasks = useServerObject<string[]>("/spring-tasks-api/tasks");

    useEffect(tasks.doGet, []);
    useAutoRefresh(10000, schedulers.doGet, []);

    return (
        <>
            <Row>
                <Col>
                    <HttpErrorView error={schedulers.error} />
                </Col>
            </Row>
            <Row>
                {schedulers.data?.map((i) => (
                    <Col key={i} xl="6" md="12" className="mb-2">
                        <SchedulerStatusView name={i} />
                    </Col>
                ))}
            </Row>
        </>
    );
};

export default SchedulersPage;
