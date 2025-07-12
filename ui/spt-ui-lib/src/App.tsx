import type { PagedModel, Trigger } from "@lib/server-api";
import {
    TriggerGroupListView,
    TriggerListView,
    useServerObject,
} from "@lib/shared";
import { useEffect } from "react";
import { Container, Tab, Tabs } from "react-bootstrap";

function App() {
    const triggers = useServerObject<PagedModel<Trigger>>(
        "/spring-tasks-api/triggers?size=5",
        {
            page: { number: 0, size: 0, totalElements: 0, totalPages: 0 },
            content: [],
        }
    );
    useEffect(triggers.doGet, []);

    return (
        <Container>
            <TriggerListView triggers={triggers.data?.content} />
            <GroupView />
        </Container>
    );
}

const GroupView = () => (
    <Tabs defaultActiveKey="active" className="mb-3">
        <Tab eventKey="active" title="Active">
            <TriggerGroupListView
                url="/spring-tasks-api/triggers-grouped"
                onGroupClick={(t) => {
                    window.location.href =
                        "http://localhost:8080/task-ui/triggers?search=" + t;
                }}
            />
        </Tab>
        <Tab eventKey="history" title="History">
            <h2>check-warehouse</h2>
            <TriggerGroupListView
                url="/spring-tasks-api/history-grouped"
                onGroupClick={(t) => {
                    window.location.href =
                        "http://localhost:8080/task-ui/history?search=" +
                        t +
                        "&tag=check-warehouse";
                }}
                filter={{ tag: "check-warehouse" }}
            />
        </Tab>
    </Tabs>
);

export default App;
