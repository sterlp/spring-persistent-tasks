import { Badge } from "react-bootstrap";
import { Trigger } from "@src/server-api";
import StatusView from "@src/task/view/staus.view";

interface Props {
    data?: Trigger;
    pill?: boolean;
}
const TriggerStatusView = ({ data, pill = false }: Props) => {
    if (!data) return undefined;

    if (data.executionCount > 0 && data.status === "WAITING") {
        return (
            <Badge pill={pill} bg="warning">
                Retry
            </Badge>
        );
    }

    return <StatusView status={data.status} pill={pill} />;
};

export default TriggerStatusView;
