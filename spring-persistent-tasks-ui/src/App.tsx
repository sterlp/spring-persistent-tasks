import { useEffect } from "react";
import { Col, Container, Navbar, Row } from "react-bootstrap";
import SchedulerStatusView from "./scheduler/views/scheduler.view";
import { useServerObject } from "./shared/http-request";
import HttpErrorView from "./shared/http-error.view";
import TriggersView from "./trigger/views/triggers-list.view";

function App() {
    const schedulers = useServerObject<string[]>(
        "/spring-tasks-api/schedulers"
    );

    useEffect(schedulers.doGet, []);

    return (
        <>
            <Navbar expand="lg" className="bg-body-tertiary">
                <Container>
                    <Navbar.Brand href="#home">
                        Persistent Tasks UI
                    </Navbar.Brand>
                </Container>
            </Navbar>
            <Container as="main" className="py-4 px-0 mx-auto">
                <Container>
                    <Row>
                        <HttpErrorView error={schedulers.error} />
                    </Row>
                    <Row>
                        {schedulers.data?.map((i) => (
                            <Col key={i}>
                                <SchedulerStatusView name={i} />
                            </Col>
                        ))}
                    </Row>
                    <Row>
                        <TriggersView />
                    </Row>
                </Container>
            </Container>
        </>
    );
}

export default App;
