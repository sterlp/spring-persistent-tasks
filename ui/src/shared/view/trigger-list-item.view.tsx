import TriggerHistoryListView from "@src/history/view/trigger-history.view";
import { Trigger } from "@src/server-api";
import LabeledText from "@src/shared/view/labled-text.view";
import JsonView from "@uiw/react-json-view";
import {
    Accordion,
    Badge,
    Button,
    Col,
    Container,
    Form,
    Row,
} from "react-bootstrap";
import TriggerStatusView from "../../trigger/views/trigger-staus.view";
import { formatMs, formatShortDateTime } from "../date.util";
import { useServerObject } from "../http-request";
import HttpErrorView from "./http-error.view";
import StackTraceView from "./stacktrace-view";
import { useUrl } from "crossroad";
import { useEffect } from "react";
interface TriggerProps {
    trigger: Trigger;
    afterTriggerChanged?: () => void;
    showReRunButton: boolean;
}

const TriggerItemView = ({
    trigger,
    afterTriggerChanged,
    showReRunButton,
}: TriggerProps) => {
    // className="d-flex justify-content-between align-items-center"
    const [url, setUrl] = useUrl();

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
        <Accordion.Item
            eventKey={trigger.id + ""}
            onClick={() => triggerHistory.doGet()}
        >
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
                                        .doCall("/run-at", "POST", new Date())
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
                                .doCall("", "DELETE")
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
                                    .doCall("", "POST")
                                    .catch((e) => console.info(e));
                            }}
                        >
                            Run Trigger again
                        </Button>
                    ) : undefined}
                </div>
                <TriggerDetailsView
                    key={trigger.id + "TriggerDetailsView"}
                    trigger={trigger}
                    history={triggerHistory.data}
                />
            </Accordion.Body>
        </Accordion.Item>
    );
};

export default TriggerItemView;

function runningSince(value?: string) {
    if (!value) return "";
    const msRuntime = new Date().getTime() - new Date(value).getTime();
    return `since ${formatMs(msRuntime)}`;
}

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
            <LabeledText
                label="Run at"
                value={formatShortDateTime(trigger.runAt)}
            />
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
                <Col md="6" xl="4">
                    <LabeledText label="Key Id" value={trigger.key.id} />
                </Col>
                <Col md="6" xl="4">
                    <LabeledText label="Task" value={trigger.key.taskName} />
                </Col>
                <Col md="6" xl="4">
                    <LabeledText label="Priority" value={trigger.priority} />
                </Col>
            </Row>
            <Row>
                <Col md="6" xl="4">
                    <LabeledText
                        label="Correlation Id"
                        value={trigger.correlationId}
                    />
                </Col>
                <Col md="6" xl="4">
                    <LabeledText
                        label="Run at"
                        value={formatShortDateTime(trigger.runAt)}
                    />
                </Col>
            </Row>
            <Row>
                <Col md="6" xl="4">
                    <LabeledText
                        label="Started at"
                        value={formatShortDateTime(trigger.start)}
                    />
                </Col>
                <Col md="6" xl="4">
                    <LabeledText
                        label="Finished at"
                        value={formatShortDateTime(trigger.end)}
                    />
                </Col>
                <Col md="6" xl="4">
                    <LabeledText
                        label="Duration MS"
                        value={
                            trigger.runningDurationInMs
                                ? formatMs(trigger.runningDurationInMs)
                                : runningSince(trigger.start)
                        }
                    />
                </Col>
            </Row>
            {trigger.lastPing ? (
                <Row>
                    <Col md="12">
                        <LabeledText
                            label="Last keep alive ping"
                            value={formatShortDateTime(trigger.lastPing)}
                        />
                    </Col>
                </Row>
            ) : undefined}
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
