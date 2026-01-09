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
        <div className="mb-3">
            <HttpErrorView error={reRunTrigger.error || editTrigger.error} />
            <div className="d-flex flex-wrap gap-2">
                {trigger.status === "WAITING" && afterTriggerChanged && (
                    <Button
                        variant="primary"
                        size="sm"
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
                )}
                {afterTriggerChanged && (
                    <Button
                        variant="outline-danger"
                        size="sm"
                        onClick={() => {
                            editTrigger
                                .doCall("", { method: "DELETE" })
                                .then(afterTriggerChanged)
                                .catch((e) => console.info(e));
                        }}
                    >
                        Cancel Trigger
                    </Button>
                )}

                {afterTriggerReRun && (
                    <Button
                        variant="outline-warning"
                        size="sm"
                        onClick={() => {
                            reRunTrigger
                                .doCall("", { method: "POST" })
                                .then(afterTriggerReRun)
                                .catch((e) => console.info(e));
                        }}
                    >
                        Run Trigger again
                    </Button>
                )}
            </div>
        </div>
    );
};

export default TriggerActionsView;
