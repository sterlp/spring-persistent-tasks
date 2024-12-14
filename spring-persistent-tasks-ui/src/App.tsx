import { useEffect } from "react";
import { Alert, Col, Container, Navbar, Row } from "react-bootstrap";
import SchedulerStatusView from "./scheduler/views/scheduler.view";
import { useServerObject } from "./shared/http-request";

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
                        {schedulers.error ? (
                            <Alert variant="danger">
                                {schedulers.error.message ??
                                    JSON.stringify(schedulers.error)}
                            </Alert>
                        ) : undefined}
                    </Row>
                    <Row>
                        {schedulers.data?.map((i) => (
                            <Col key={i}>
                                <SchedulerStatusView name={i} />
                            </Col>
                        ))}
                    </Row>
                </Container>
            </Container>
        </>
    );
}

export default App;
