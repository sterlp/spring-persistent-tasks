import { Badge } from "react-bootstrap";
import type { Trigger } from "@lib/server-api";
import TriggerStatusView from "./trigger-status.view";

interface Props {
    data?: Trigger;
    pill?: boolean;
}
const RunningTriggerStatusView = ({ data, pill = false }: Props) => {
    if (!data) return undefined;

    if (data.executionCount > 0 && data.status === "WAITING") {
        return (
            <Badge pill={pill} bg="warning">
                Retry
            </Badge>
        );
    }

    return <TriggerStatusView status={data.status} pill={pill} />;
};

export default RunningTriggerStatusView;
