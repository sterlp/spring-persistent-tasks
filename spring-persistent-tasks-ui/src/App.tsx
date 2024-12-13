import { useEffect } from "react"
import { Alert, Card, Col, Container, Navbar, Row } from "react-bootstrap";
import { useServerObject } from "./shared/http-request";

function App() {
    const schedulers = useServerObject<string[]>('/spring-tasks-api/schedulers');

    useEffect(() => {
        const c = schedulers.doGet();
        return () => c.abort();
    }, []);

    return (
        <>
            <Navbar expand="lg" className="bg-body-tertiary">
                <Container>
                    <Navbar.Brand href="#home">Persistent Tasks UI</Navbar.Brand>
                </Container>
            </Navbar>
            <Container as="main" className="py-4 px-0 mx-auto">
                <Container>
                    <Row>
                        {schedulers.error ?
                            <Alert variant="danger">
                                {JSON.stringify(schedulers.error)}
                            </Alert>
                        : undefined}
                    </Row>
                    <Row>
                        {schedulers.data?.map(i => <Col key={i}><Card><Card.Header>{i}</Card.Header></Card></Col>)}
                    </Row>
                </Container>
            </Container>
        </>
    )
}

export default App
