import { Badge } from "react-bootstrap";
import { Trigger } from "@src/server-api";

interface Props {
    data?: Trigger;
    pill?: boolean;
}
const TriggerStatusView = ({ data, pill = false }: Props) => {
    if (!data) return undefined;

    if (data.status === "SUCCESS") return <Badge bg="success">Success</Badge>;
    if (data.status === "FAILED") return <Badge bg="danger">Failed</Badge>;
    if (data.status === "RUNNING") return <Badge>Running</Badge>;

    if (data.executionCount > 0 && data.status === "WAITING") {
        return <Badge bg="warning">Retry</Badge>;
    }
    if (data.status === "WAITING") return <Badge bg="secondary">Wating</Badge>;
    if (data.status === "CANCELED")
        return <Badge bg="secondary">Canceled</Badge>;

    return <Badge pill={pill}>{data.status}</Badge>;
};

export default TriggerStatusView;
