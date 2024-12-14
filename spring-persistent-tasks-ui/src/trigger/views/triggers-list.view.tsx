import { useEffect } from "react";
import { TriggerEntity } from "../../server-api";
import { useServerObject } from "../../shared/http-request";
import {
    Accordion,
    Badge,
    Col,
    Container,
    Form,
    Row,
    Stack,
} from "react-bootstrap";
import { Slice } from "../../shared/spring-common";
import LabeledText from "../../shared/labled-text";
import TriggerStatusView from "./trigger-staus.view";

function TriggersView() {
    const triggers = useServerObject<Slice<TriggerEntity>>(
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
                <TriggerView key={t.id.id + t.id.name} trigger={t} />
            ))}
        </Stack>
    );
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
                            <Col>
                                {formatDateTime(trigger.data.triggerTime)}
                            </Col>
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
                                label="Run at"
                                value={formatDateTime(trigger.data.triggerTime)}
                            />
                        </Col>
                        <Col>
                            <LabeledText
                                label="Started at"
                                value={formatDateTime(trigger.data.start)}
                            />
                        </Col>
                        <Col>
                            <LabeledText
                                label="Finished at"
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
