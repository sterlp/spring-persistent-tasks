import Router, { Route, Switch, usePath } from "crossroad";
import { lazy, Suspense } from "react";
import { Container, Nav, Navbar } from "react-bootstrap";
import { LoadingView } from "spring-persistent-tasks-ui";

const SchedulersPage = lazy(() => import("./scheduler/scheduler.page"));
const TriggersPage = lazy(() => import("./trigger/triggers.page"));
const HistoryPage = lazy(() => import("./history/history.page"));

const routes: MenuItem[] = [
    { label: "Home", path: "", component: SchedulersPage },
    { label: "Triggers", path: "/triggers", component: TriggersPage },
    { label: "History", path: "/history", component: HistoryPage },
];
const BASE = "/task-ui";

const App = () => {
    return (
        <Router>
            <Navbar expand="lg" bg="primary" data-bs-theme="dark">
                <Container>
                    <Navbar.Brand>Persistent Tasks UI</Navbar.Brand>
                    <Navbar.Toggle aria-controls="basic-navbar-nav" />
                    <Navbar.Collapse id="basic-navbar-nav">
                        <Nav className="me-auto">
                            {routes.map((i) => (
                                <MenuLink key={"link-" + i.path} item={i} />
                            ))}
                        </Nav>
                    </Navbar.Collapse>
                </Container>
            </Navbar>

            <Container as="main" className="mt-3">
                <Suspense fallback={<LoadingView />}>
                    <Switch>
                        {routes.map((i) => (
                            <Route
                                key={BASE + i.path}
                                path={BASE + i.path}
                                component={i.component}
                            />
                        ))}
                    </Switch>
                </Suspense>
            </Container>
        </Router>
    );
};

interface MenuItem {
    label: string;
    path: string;
    component: React.FunctionComponent;
}

const MenuLink = ({ item }: { item: MenuItem }) => {
    const [path] = usePath();
    const pagePath = BASE + item.path;
    const active = pagePath == path;
    return (
        <Nav.Link active={active} key={pagePath} href={pagePath}>
            {item.label}
        </Nav.Link>
    );
};

export default App;
