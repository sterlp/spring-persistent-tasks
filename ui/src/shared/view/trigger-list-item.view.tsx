import { Accordion, Col, Container, Row } from "react-bootstrap";
import TriggerStatusView from "../../trigger/views/trigger-staus.view";
import JsonView from "@uiw/react-json-view";
import { Trigger } from "@src/server-api";
import LabeledText from "@src/shared/view/labled-text.view";
import TriggerHistoryListView from "@src/history/view/trigger-history.view";
import { formatDateTime } from "../date.util";
import { useServerObject } from "../http-request";

interface TriggerProps {
    trigger: Trigger;
}
const TriggerItemView = ({ trigger }: TriggerProps) => {
    // className="d-flex justify-content-between align-items-center"
    const triggerHistory = useServerObject<Trigger[]>(
        "/spring-tasks-api/history/instance/" + trigger.instanceId
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
                        <Row className="align-items-center">
                            <Col xs="3" xl="1">
                                <TriggerStatusView data={trigger} />
                            </Col>
                            <Col>
                                <small className="text-truncate text-muted">
                                    {trigger.key.id}
                                </small>
                                <br />
                                {" " + trigger.key.taskName}
                            </Col>
                            <Col>
                                <LabeledText
                                    label="Run at"
                                    value={formatDateTime(trigger.runAt)}
                                />
                            </Col>
                            <Col className="d-none d-lg-block">
                                <LabeledText
                                    label="Retrys"
                                    value={trigger.executionCount}
                                />
                            </Col>
                        </Row>
                    </Container>
                </Accordion.Header>
                <Accordion.Body>
                    <Row>
                        <Col>
                            <LabeledText
                                label="Task"
                                value={trigger.key.taskName}
                            />
                        </Col>
                        <Col>
                            <LabeledText
                                label="Key Id"
                                value={trigger.key.id}
                            />
                        </Col>
                        <Col>
                            <LabeledText
                                label="Retrys"
                                value={trigger.executionCount}
                            />
                        </Col>
                        <Col>
                            <LabeledText
                                label="Priority"
                                value={trigger.priority}
                            />
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
                                value={trigger.runningDurationInMs}
                            />
                        </Col>
                    </Row>
                    <Row className="mt-2">
                        <Col>
                            <TriggerHistoryListView
                                triggers={triggerHistory.data}
                            />
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
                            <ExceptionView
                                key={trigger.id + "-error-view"}
                                trigger={trigger}
                            />
                        </Col>
                    </Row>
                </Accordion.Body>
            </Accordion.Item>
        </Accordion>
    );
};

export default TriggerItemView;

const ExceptionView = ({ trigger }: TriggerProps) => {
    if (!trigger.exceptionName) return undefined;

    return (
        <>
            <b>{trigger.exceptionName}</b>
            <br />
            <pre>
                <small>
                    <code>{trigger.lastException}</code>
                </small>
            </pre>
        </>
    );
};

function isObject(value: any): boolean {
    if (value === undefined || value === null) return false;
    return typeof value === "object" || Array.isArray(value);
}
