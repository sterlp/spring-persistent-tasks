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

function TriggerItemView({ trigger }: { trigger: Trigger }) {
    return (
        <Accordion>
            <Accordion.Item eventKey={trigger.id.id + trigger.id.name}>
                <Accordion.Header className="d-flex justify-content-between align-items-center">
                    <Container>
                        <Row>
                            <Col>{trigger.id.name}</Col>
                            <Col>
                                <TriggerStatusView data={trigger} />
                            </Col>
                            <Col>{formatDateTime(trigger.runAt)}</Col>
                        </Row>
                    </Container>
                </Accordion.Header>
                <Accordion.Body>
                    <Row>
                        <Col>
                            <LabeledText label="Id" value={trigger.id.id} />
                        </Col>
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
                    {trigger.state ? (
                        <Row className="mt-2">
                            <Col>
                                <LabeledText
                                    label="State"
                                    value={trigger.state}
                                />
                            </Col>
                        </Row>
                    ) : undefined}
                    {trigger.exceptionName ? (
                        <Row className="mt-2">
                            <ExcptionView
                                label={trigger.exceptionName}
                                stack={trigger.lastException}
                            />
                        </Row>
                    ) : undefined}
                </Accordion.Body>
            </Accordion.Item>
        </Accordion>
    );
}

function ExcptionView({ label, stack }: { label?: string; stack?: string }) {
    if (!label) return undefined;

    return (
        <Accordion>
            <Accordion.Header>
                <div className="text-danger">{label}</div>
            </Accordion.Header>
            <Accordion.Body>{stack}</Accordion.Body>
        </Accordion>
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
