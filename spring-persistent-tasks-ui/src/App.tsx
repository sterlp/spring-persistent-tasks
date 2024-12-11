import { useEffect, useState } from "react"
import { Card, Col, Container, Navbar, Row } from "react-bootstrap";

function App() {
    const [schedulers, setSchedulers] =  useState<string[] | undefined>(undefined);

    useEffect(() => {
        fetchSchedulers();
    }, []);
    
    const fetchSchedulers = async () => {
        const response = await fetch('/spring-tasks-api/schedulers');
        if (response.ok) {
            setSchedulers(await response.json() as string[]);
        }
    }

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
                        {schedulers?.map(i => <Col key={i}><Card><Card.Header>{i}</Card.Header></Card></Col>)}
                    </Row>
                </Container>
            </Container>
        </>
    )
}

export default App
