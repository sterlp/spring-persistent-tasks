import { useEffect } from "react";
import { TriggerEntity } from "../../server-api";
import { useServerObject } from "../../shared/http-request";
import { Accordion, Badge, Col, Container, Form, Row } from "react-bootstrap";
import { Slice } from "../../shared/spring-common";
import LabeledText from "../../shared/labled-text";
import TriggerStatusView from "./trigger-staus.view";

function TriggersView() {
    const triggers = useServerObject<Slice<TriggerEntity>>(
        "/spring-tasks-api/triggers"
    );

    useEffect(triggers.doGet, []);

    return triggers.data?.content.map((t) => (
        <TriggerView key={t.id.id + t.id.name} trigger={t} />
    ));
}

export default TriggersView;

function TriggerView({ trigger }: { trigger: TriggerEntity }) {
    return (
        <Accordion>
            <Accordion.Item eventKey={trigger.id.id + trigger.id.name}>
                <Accordion.Header className="d-flex justify-content-between align-items-center">
                    <Container>
                        <Row>
                            <Col>{trigger.id.name}</Col>
                            <Col>
                                <TriggerStatusView data={trigger.data} />
                            </Col>
                            <Col>{formatDateTime(trigger.data.start)}</Col>
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
                                value={trigger.data.executionCount}
                            />
                        </Col>
                        <Col>
                            <LabeledText
                                label="Start"
                                value={formatDateTime(trigger.data.start)}
                            />
                        </Col>
                        <Col>
                            <LabeledText
                                label="End"
                                value={formatDateTime(trigger.data.end)}
                            />
                        </Col>
                        <Col>
                            <LabeledText
                                label="Duration MS"
                                value={trigger.data.runningDurationInMs}
                            />
                        </Col>
                    </Row>
                </Accordion.Body>
            </Accordion.Item>
        </Accordion>
    );
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
        options.month = "short";
        options.day = "numeric";
    }
    const browserLanguage = navigator.language || "en-US";
    return new Intl.DateTimeFormat(browserLanguage, options).format(date);
}
