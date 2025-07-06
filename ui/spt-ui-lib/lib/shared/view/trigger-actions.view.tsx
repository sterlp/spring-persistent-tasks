import type { Trigger } from "@lib/server-api";
import { Button } from "react-bootstrap";
import { useServerObject } from "../http-request";
import HttpErrorView from "./http-error.view";

interface TriggerActionsViewProps {
    trigger: Trigger;
    afterTriggerChanged?: () => void;
    afterTriggerReRun?: () => void;
}

const TriggerActionsView = ({
    trigger,
    afterTriggerChanged,
    afterTriggerReRun,
}: TriggerActionsViewProps) => {
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

export default TriggerActionsView;
