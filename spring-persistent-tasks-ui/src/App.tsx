import { useEffect } from "react";
import {
    Alert,
    Badge,
    Card,
    Col,
    Container,
    Navbar,
    Row,
} from "react-bootstrap";
import { useServerObject } from "./shared/http-request";
import { SchedulerEntity } from "./server-api";

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

function SchedulerStatusView({ name }: { name: string }) {
    const status = useServerObject<SchedulerEntity>(
        `/spring-tasks-api/schedulers/${name}`
    );

    useEffect(status.doGet, [name]);

    return (
        <Card>
            <Card.Header>
                {name}{" "}
                {status.data ? (
                    <Badge pill bg="success">
                        {status.data?.status}
                    </Badge>
                ) : undefined}
            </Card.Header>
            <Card.Body>{JSON.stringify(status.data)}</Card.Body>
        </Card>
    );
}
