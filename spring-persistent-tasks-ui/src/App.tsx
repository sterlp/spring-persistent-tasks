import Router, { Route, Switch } from "crossroad";
import TriggersPage from "./trigger/triggers.page";
import { Container, Nav, Navbar } from "react-bootstrap";
import SchedulersPage from "./scheduler/scheduler.page";

const App = () => {
    return (
        <Router>
            <Navbar expand="lg" className="bg-body-tertiary">
                <Container>
                    <Navbar.Brand href="#home">
                        Persistent Tasks UI
                    </Navbar.Brand>

                    <Navbar.Toggle aria-controls="basic-navbar-nav" />
                    <Navbar.Collapse id="basic-navbar-nav">
                        <Nav className="me-auto">
                            <Nav.Link href="/task-ui">Home</Nav.Link>
                            <Nav.Link href="/task-ui/triggers">
                                Trigger
                            </Nav.Link>
                        </Nav>
                    </Navbar.Collapse>
                </Container>
            </Navbar>

            <Container as="main">
                <Switch>
                    <Route path="/task-ui" component={SchedulersPage} />
                    <Route path="/task-ui/triggers" component={TriggersPage} />
                </Switch>
            </Container>
        </Router>
    );
};

export default App;
