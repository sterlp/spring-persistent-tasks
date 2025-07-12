import {
    HourglassSplit,
    PauseCircle,
    ExclamationCircle,
    CheckCircle,
    XCircle,
} from "react-bootstrap-icons";
import { Spinner } from "react-bootstrap";
import type { Trigger, TriggerStatus } from "@lib/server-api";

const getIcon = (status: TriggerStatus) => {
    switch (status) {
        case "RUNNING":
            return <Spinner animation="border" size="sm" />;
        case "WAITING":
            return <HourglassSplit />;
        case "AWAITING_SIGNAL":
            return <PauseCircle />;
        case "FAILED":
        case "EXPIRED_SIGNAL":
            return <ExclamationCircle />;
        case "CANCELED":
            return <XCircle />;
        default:
            return <CheckCircle />;
    }
};

const getVariant = (trigger: Trigger): string => {
    if (["FAILED", "EXPIRED_SIGNAL"].includes(trigger.status)) return "danger";
    if (trigger.status === "CANCELED") return "secondary";
    if (trigger.status === "SUCCESS") return "success";
    if (trigger.executionCount > 1) return "warning";
    if (trigger.status === "RUNNING") return "primary";
    if (trigger.status === "WAITING") return "secondary";
    return "";
};

const TriggerStatusIcon = ({ trigger }: { trigger: Trigger }) => {
    const variant = getVariant(trigger);
    const icon = getIcon(trigger.status);

    return (
        <span className={`text-${variant} d-flex align-items-center gap-2`}>
            {icon}
            <span className="fw-medium">
                {trigger.status.replace(/_/g, " ")}
            </span>
        </span>
    );
};

export default TriggerStatusIcon;
