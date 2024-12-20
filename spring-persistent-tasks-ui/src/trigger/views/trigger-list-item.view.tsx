import { Accordion, Col, Container, Row } from "react-bootstrap";
import { Trigger } from "../../server-api";
import TriggerStatusView from "./trigger-staus.view";
import LabeledText from "../../shared/labled-text";
import JsonView from "@uiw/react-json-view";

interface TriggerProps {
    trigger: Trigger;
}
const TriggerItemView = ({ trigger }: TriggerProps) => {
    // className="d-flex justify-content-between align-items-center"
    return (
        <Accordion>
            <Accordion.Item eventKey={trigger.id + ""}>
                <Accordion.Header>
                    <Container>
                        <Row>
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
                            <LabeledText label="ID" value={trigger.key.id} />
                        </Col>
                        <Col>
                            <LabeledText
                                label="Retrys"
                                value={trigger.executionCount}
                            />
                        </Col>
                    </Row>
                    <hr />
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

function formatDateTime(inputDate?: string | Date): string {
    if (!inputDate) return "";
    const date = inputDate instanceof Date ? inputDate : new Date(inputDate);

    const now = new Date();
    const isToday = date.toDateString() === now.toDateString();
    const options = {
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
        hour12: false, // Use 24-hour format
    } as Intl.DateTimeFormatOptions;

    if (!isToday) {
        options.year = "numeric";
        options.month = "numeric";
        options.day = "numeric";
    }
    return new Intl.DateTimeFormat(
        navigator.language || "en-US",
        options
    ).format(date);
}
