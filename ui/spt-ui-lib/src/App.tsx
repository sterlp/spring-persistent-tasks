import { PageView, TriggerGroupListView } from "@lib/shared";
import { Tab, Tabs } from "react-bootstrap";
import { Hr } from "react-bootstrap-icons";
function App() {
    return (
        <>
            <PageView onPage={(i) => console.log("paging", i)} />
            <Hr />
            <Tabs defaultActiveKey="active" className="mb-3">
                <Tab eventKey="active" title="Active">
                    <TriggerGroupListView
                        url="/spring-tasks-api/triggers-grouped"
                        onGroupClick={(t) => {
                            window.location.href =
                                "http://localhost:8080/task-ui/triggers?search=" +
                                t;
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
        </>
    );
}

export default App;
