import { useEffect } from "react";
import { Accordion, Col, Container, Row, Stack } from "react-bootstrap";
import { PagedModel, Trigger } from "../../server-api";
import { useServerObject } from "../../shared/http-request";
import LabeledText from "../../shared/labled-text";
import TriggerStatusView from "./trigger-staus.view";

function TriggersView() {
    const triggers = useServerObject<PagedModel<Trigger>>(
        "/spring-tasks-api/triggers"
    );

    useEffect(triggers.doGet, []);
    useEffect(() => {
        const intervalId = setInterval(triggers.doGet, 10000);
        return () => clearInterval(intervalId);
    }, []);

    return (
        <Stack gap={1}>
            {triggers.data?.content.map((t) => (
                <TriggerItemView key={t.id.id + t.id.name} trigger={t} />
            ))}
        </Stack>
    );
}

export default TriggersView;

interface TriggerProps {
    trigger: Trigger;
}
function TriggerItemView({ trigger }: TriggerProps) {
    // className="d-flex justify-content-between align-items-center"
    return (
        <Accordion>
            <Accordion.Item eventKey={trigger.key}>
                <Accordion.Header>
                    <Container>
                        <Row>
                            <Col>
                                <small className="text-truncate text-muted">
                                    {trigger.id.id}
                                </small>
                                {" " + trigger.id.name}
                            </Col>
                            <Col>
                                <TriggerStatusView data={trigger} />
                            </Col>
                            <Col>
                                <LabeledText
                                    label="Run at"
                                    value={formatDateTime(trigger.runAt)}
                                />
                            </Col>
                        </Row>
                    </Container>
                </Accordion.Header>
                <Accordion.Body>
                    <Row>
                        <Col>
                            <LabeledText
                                label="Retrys"
                                value={trigger.executionCount}
                            />
                        </Col>
                        <Col>
                            <LabeledText
                                label="Run at"
                                value={formatDateTime(trigger.runAt)}
                            />
                        </Col>
                        <Col>
                            <LabeledText
                                label="Started at"
                                value={formatDateTime(trigger.start)}
                            />
                        </Col>
                        <Col>
                            <LabeledText
                                label="Finished at"
                                value={formatDateTime(trigger.end)}
                            />
                        </Col>
                        <Col>
                            <LabeledText
                                label="Duration MS"
                                value={trigger.runningDurationInMs}
                            />
                        </Col>
                    </Row>
                </Accordion.Body>
            </Accordion.Item>
            <StateView key={trigger.id + "state-view"} trigger={trigger} />
            <ExcptionView key={trigger.id + "error-view"} trigger={trigger} />
        </Accordion>
    );
}

function StateView({ trigger }: TriggerProps) {
    if (!trigger.state) return undefined;

    return (
        <Accordion.Item eventKey={trigger.key + "-state"}>
            <Accordion.Header>
                <Container>
                    <Row>
                        <Col xs="2" md="1">
                            State
                        </Col>
                        <Col className="text-truncate text-muted">
                            <small>{trigger.state}</small>
                        </Col>
                    </Row>
                </Container>
            </Accordion.Header>
            <Accordion.Body>{trigger.state}</Accordion.Body>
        </Accordion.Item>
    );
}

function ExcptionView({ trigger }: TriggerProps) {
    if (!trigger.exceptionName) return undefined;

    return (
        <Accordion.Item eventKey={trigger.key + "-error"}>
            <Accordion.Header>
                <Container>
                    <Row>
                        <Col className="text-danger">
                            {trigger.exceptionName}
                        </Col>
                    </Row>
                </Container>
            </Accordion.Header>
            <Accordion.Body>{trigger.lastException}</Accordion.Body>
        </Accordion.Item>
    );
}

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
    const browserLanguage = navigator.language || "en-US";
    return new Intl.DateTimeFormat(browserLanguage, options).format(date);
}
