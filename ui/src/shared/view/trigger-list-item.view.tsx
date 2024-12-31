import TriggerHistoryListView from "@src/history/view/trigger-history.view";
import { Trigger } from "@src/server-api";
import LabeledText from "@src/shared/view/labled-text.view";
import JsonView from "@uiw/react-json-view";
import { Accordion, Button, Col, Container, Row } from "react-bootstrap";
import TriggerStatusView from "../../trigger/views/trigger-staus.view";
import { formatDateTime, formatMs } from "../date.util";
import { useServerObject } from "../http-request";
import HttpErrorView from "./http-error.view";
import StackTraceView from "./stacktrace-view";
interface TriggerProps {
    trigger: Trigger;
    afterTriggerChanged?: () => void;
}

const TriggerItemView = ({ trigger, afterTriggerChanged }: TriggerProps) => {
    // className="d-flex justify-content-between align-items-center"

    const triggerHistory = useServerObject<Trigger[]>(
        "/spring-tasks-api/history/instance/" + trigger.instanceId
    );

    const editTrigger = useServerObject<Trigger[]>(
        "/spring-tasks-api/triggers/" +
            trigger.key.taskName +
            "/" +
            trigger.key.id
    );

    return (
        <Accordion
            onClick={() => {
                if (!triggerHistory.data) triggerHistory.doGet();
            }}
        >
            <Accordion.Item eventKey={trigger.id + ""}>
                <Accordion.Header>
                    <Container>
                        <TriggerCompactView
                            key={trigger.id + "TriggerCompactView"}
                            trigger={trigger}
                        />
                    </Container>
                </Accordion.Header>
                <Accordion.Body>
                    <HttpErrorView
                        error={triggerHistory.error || editTrigger.error}
                    />
                    {trigger.status === "WAITING" && afterTriggerChanged ? (
                        <div className="d-flex gap-2 mb-2">
                            <Button
                                onClick={() => {
                                    editTrigger
                                        .doCall("/run-at", "POST", new Date())
                                        .then(afterTriggerChanged)
                                        .catch((e) => console.info(e));
                                }}
                            >
                                Run now
                            </Button>
                            <Button
                                variant="danger"
                                onClick={() => {
                                    editTrigger
                                        .doCall("", "DELETE")
                                        .then(afterTriggerChanged)
                                        .catch((e) => console.info(e));
                                }}
                            >
                                Cancel Trigger
                            </Button>
                        </div>
                    ) : undefined}
                    <TriggerDetailsView
                        key={trigger.id + "TriggerDetailsView"}
                        trigger={trigger}
                        history={triggerHistory.data}
                    />
                </Accordion.Body>
            </Accordion.Item>
        </Accordion>
    );
};

export default TriggerItemView;

const TriggerCompactView = ({ trigger }: { trigger: Trigger }) => (
    <Row className="align-items-center">
        <Col xs="3" xl="1">
            <TriggerStatusView data={trigger} />
        </Col>
        <Col>
            <small className="text-truncate text-muted">{trigger.key.id}</small>
            <br />
            {" " + trigger.key.taskName}
        </Col>
        <Col>
            <LabeledText label="Run at" value={formatDateTime(trigger.runAt)} />
        </Col>
        <Col className="d-none d-lg-block">
            <LabeledText label="Retrys" value={trigger.executionCount} />
        </Col>
    </Row>
);

const TriggerDetailsView = ({
    trigger,
    history,
}: {
    trigger: Trigger;
    history?: Trigger[];
}) => {
    return (
        <>
            <Row>
                <Col>
                    <LabeledText label="Task" value={trigger.key.taskName} />
                </Col>
                <Col>
                    <LabeledText label="Key Id" value={trigger.key.id} />
                </Col>
                <Col>
                    <LabeledText
                        label="Retrys"
                        value={trigger.executionCount}
                    />
                </Col>
                <Col>
                    <LabeledText label="Priority" value={trigger.priority} />
                </Col>
            </Row>
            <Row>
                <Col md="6" xl="3">
                    <LabeledText
                        label="Run at"
                        value={formatDateTime(trigger.runAt)}
                    />
                </Col>
                <Col md="6" xl="3">
                    <LabeledText
                        label="Started at"
                        value={formatDateTime(trigger.start)}
                    />
                </Col>
                <Col md="6" xl="3">
                    <LabeledText
                        label="Finished at"
                        value={formatDateTime(trigger.end)}
                    />
                </Col>
                <Col md="6" xl="3">
                    <LabeledText
                        label="Duration MS"
                        value={formatMs(trigger.runningDurationInMs)}
                    />
                </Col>
            </Row>
            <Row className="mt-2">
                <Col>
                    <TriggerHistoryListView triggers={history} />
                </Col>
            </Row>
            <hr />
            <Row className="mt-2">
                <Col>
                    {isObject(trigger.state) ? (
                        <JsonView value={trigger.state} />
                    ) : (
                        <pre>{trigger.state}</pre>
                    )}
                </Col>
                <Col>
                    <StackTraceView
                        key={trigger.id + "-error-view"}
                        title={trigger.exceptionName}
                        error={trigger.lastException}
                    />
                </Col>
            </Row>
        </>
    );
};

function isObject(value: any): boolean {
    if (value === undefined || value === null) return false;
    return typeof value === "object" || Array.isArray(value);
}
