import SchedulerStatusView from "@src/scheduler/views/scheduler.view";
import HttpErrorView from "@src/shared/http-error.view";
import { useServerObject } from "@src/shared/http-request";
import { useEffect } from "react";
import { Col, Row } from "react-bootstrap";

const SchedulersPage = () => {
    const schedulers = useServerObject<string[]>(
        "/spring-tasks-api/schedulers"
    );

    useEffect(schedulers.doGet, []);
    // Poll every 10 seconds
    useEffect(() => {
        const intervalId = setInterval(schedulers.doGet, 10000);
        return () => clearInterval(intervalId);
    }, []);

    return (
        <>
            <Row>
                <HttpErrorView error={schedulers.error} />
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
