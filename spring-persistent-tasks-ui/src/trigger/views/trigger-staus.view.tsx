import { Badge } from "react-bootstrap";
import { Trigger } from "../../server-api";

interface Props {
    data?: Trigger;
}
const TriggerStatusView = ({ data }: Props) => {
    if (!data) return undefined;

    if (data.status === "SUCCESS") return <Badge bg="success">SUCCESS</Badge>;

    if (data.end != null) {
        return <Badge bg="warning">RETRY</Badge>;
    }
    if (data.status === "NEW") return <Badge bg="secondary">WAITING</Badge>;

    return <Badge>{data.status}</Badge>;
};

export default TriggerStatusView;
