import type { Trigger } from "@lib/server-api";
import { Accordion, Button, Container } from "react-bootstrap";
import { useServerObject } from "../http-request";
import HttpErrorView from "./http-error.view";
import TriggerCompactView from "./trigger-compact.view";
import TriggerView from "./trigger.view";

interface TriggerProps {
    trigger: Trigger;
    afterTriggerChanged?: () => void;
    afterTriggerReRun?: () => void;
    onFieldClick: (key: string, value?: string) => void;
}

const TriggerListItemView = ({
    trigger,
    afterTriggerChanged,
    afterTriggerReRun,
    onFieldClick,
}: TriggerProps) => {
    const triggerHistory = useServerObject<Trigger[]>(
        "/spring-tasks-api/history/instance/" + trigger.instanceId
    );

    return (
        <Accordion.Item eventKey={"trigger-" + trigger.id}>
            <Accordion.Header>
                <Container>
                    <TriggerCompactView
                        key={trigger.id + "TriggerCompactView"}
                        trigger={trigger}
                    />
                </Container>
            </Accordion.Header>
            <Accordion.Body onEnter={() => triggerHistory.doGet()}>
                <HttpErrorView error={triggerHistory.error} />

                <TriggerActionsView
                    onFieldClick={onFieldClick}
                    trigger={trigger}
                    afterTriggerChanged={afterTriggerChanged}
                    afterTriggerReRun={afterTriggerReRun}
                />

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

const TriggerActionsView = ({
    trigger,
    afterTriggerChanged,
    afterTriggerReRun,
}: TriggerProps) => {
    const reRunTrigger = useServerObject<Trigger>(
        `/spring-tasks-api/history/${trigger.id}/re-run`
    );

    const editTrigger = useServerObject<Trigger[]>(
        "/spring-tasks-api/triggers/" +
            trigger.key.taskName +
            "/" +
            trigger.key.id
    );

    return (
        <>
            <HttpErrorView error={reRunTrigger.error || editTrigger.error} />
            <div className="d-flex gap-2 mb-2">
                {trigger.status === "WAITING" && afterTriggerChanged ? (
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
                ) : undefined}
                {afterTriggerChanged ? (
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
                ) : undefined}
                {afterTriggerReRun ? (
                    <Button
                        variant="warning"
                        onClick={() => {
                            reRunTrigger
                                .doCall("", { method: "POST" })
                                .then(afterTriggerReRun)
                                .catch((e) => console.info(e));
                        }}
                    >
                        Run Trigger again
                    </Button>
                ) : undefined}
            </div>
        </>
    );
};

export default TriggerListItemView;
