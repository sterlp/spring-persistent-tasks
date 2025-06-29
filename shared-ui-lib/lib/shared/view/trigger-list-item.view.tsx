import LabeledText from "@lib/shared/view/labled-text.view";
import { useUrl } from "crossroad";
import { useEffect } from "react";
import {
    Accordion,
    Badge,
    Button,
    Col,
    Container,
    Form,
    Row,
} from "react-bootstrap";
import { formatMs, formatShortDateTime, runningSince } from "../date.util";
import { useServerObject } from "../http-request";
import HttpErrorView from "./http-error.view";
import TriggerView from "./trigger.view";
import type { Trigger } from "@lib/server-api";
import TriggerStatusView from "./trigger-staus.view";

interface TriggerProps {
    trigger: Trigger;
    afterTriggerChanged?: () => void;
    showReRunButton: boolean;
    onFieldClick: (key: string, value?: string) => void;
}

const TriggerListItemView = ({
    trigger,
    afterTriggerChanged,
    showReRunButton,
    onFieldClick,
}: TriggerProps) => {
    // className="d-flex justify-content-between align-items-center"
    const [_, setUrl] = useUrl();

    const triggerHistory = useServerObject<Trigger[]>(
        "/spring-tasks-api/history/instance/" + trigger.instanceId
    );

    const reRunTrigger = useServerObject<Trigger>(
        `/spring-tasks-api/history/${trigger.id}/re-run`
    );

    const editTrigger = useServerObject<Trigger[]>(
        "/spring-tasks-api/triggers/" +
            trigger.key.taskName +
            "/" +
            trigger.key.id
    );

    useEffect(() => {
        if (reRunTrigger.data && reRunTrigger.data.id) {
            setUrl("/task-ui/triggers");
        }
    }, [setUrl, reRunTrigger.data]);

    return (
        <Accordion.Item eventKey={trigger.id + ""}>
            <Accordion.Header>
                <Container>
                    <TriggerCompactView
                        key={trigger.id + "TriggerCompactView"}
                        trigger={trigger}
                    />
                </Container>
            </Accordion.Header>
            <Accordion.Body onEnter={() => triggerHistory.doGet()}>
                <HttpErrorView
                    error={
                        triggerHistory.error ||
                        editTrigger.error ||
                        reRunTrigger.error
                    }
                />
                <div className="d-flex gap-2 mb-2">
                    {trigger.status === "WAITING" && afterTriggerChanged ? (
                        <>
                            <Button
                                onClick={() => {
                                    editTrigger
                                        .doCall("/run-at", {
                                            method: "POST",
                                            dataToSend: new Date(),
                                        })
                                        .then(afterTriggerChanged)
                                        .catch((e) => console.info(e));
                                }}
                            >
                                Run now
                            </Button>
                        </>
                    ) : undefined}
                    <Button
                        variant="danger"
                        onClick={() => {
                            editTrigger
                                .doCall("", { method: "DELETE" })
                                .then(afterTriggerChanged)
                                .catch((e) => console.info(e));
                        }}
                    >
                        Cancel Trigger
                    </Button>
                    {showReRunButton ? (
                        <Button
                            variant="warning"
                            onClick={() => {
                                reRunTrigger
                                    .doCall("", { method: "POST" })
                                    .catch((e) => console.info(e));
                            }}
                        >
                            Run Trigger again
                        </Button>
                    ) : undefined}
                </div>
                <TriggerView
                    key={trigger.id + "-TriggerDetailsView"}
                    trigger={trigger}
                    history={triggerHistory.data}
                    onClick={onFieldClick}
                />
            </Accordion.Body>
        </Accordion.Item>
    );
};

export default TriggerListItemView;

const TriggerCompactView = ({ trigger }: { trigger: Trigger }) => (
    <Row className="align-items-center">
        <Col className="col-2">
            <TriggerStatusView data={trigger} />
        </Col>
        <Col className="col-5">
            <Form.Text muted role="label">
                {trigger.key.id}
            </Form.Text>
            <div>{trigger.key.taskName}</div>
        </Col>
        <Col className="col-2">
            {isActive(trigger) ? (
                <LabeledText
                    label="Run at"
                    value={formatShortDateTime(trigger.runAt)}
                />
            ) : (
                <LabeledText
                    label="Finished at"
                    value={formatShortDateTime(trigger.end)}
                />
            )}
        </Col>
        <Col className="col-3">
            <TriggerExecutiomView trigger={trigger} />
        </Col>
    </Row>
);

const TriggerExecutiomView = ({ trigger }: { trigger: Trigger }) => {
    if (trigger.runningOn) {
        return (
            <LabeledText
                label={`Running on (${trigger.executionCount})`}
                value={trigger.runningOn + " " + runningSince(trigger.start)}
            />
        );
    }
    if (trigger.status == "WAITING" && trigger.runAt) {
        return (
            <LabeledText
                label={`Should start in`}
                value={formatMs(
                    new Date(trigger.runAt).getTime() - new Date().getTime()
                )}
            />
        );
    }
    return (
        <LabeledText
            label="Executions"
            value={
                <div className="d-flex justify-content-start align-items-center">
                    <Badge
                        bg={trigger.executionCount > 1 ? "warning" : "success"}
                    >
                        {trigger.executionCount}
                    </Badge>
                    <span className="ms-2">
                        {formatMs(trigger.runningDurationInMs)}
                    </span>
                </div>
            }
        />
    );
};

function isActive(trigger: Trigger): boolean {
    return trigger.status == "RUNNING" || trigger.status == "WAITING";
}
