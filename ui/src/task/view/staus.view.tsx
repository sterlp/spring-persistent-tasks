import { TriggerStatus } from "@src/server-api";
import { Badge } from "react-bootstrap";

interface Props {
    status: TriggerStatus;
    suffix?: string;
}
const StatusView = ({ status, suffix }: Props) => {
    if (status === "SUCCESS")
        return <Badge bg="success">Success{suffix ?? ""}</Badge>;
    if (status === "FAILED")
        return <Badge bg="danger">Failed{suffix ?? ""}</Badge>;
    if (status === "RUNNING") return <Badge>Running{suffix ?? ""}</Badge>;
    if (status === "WAITING")
        return <Badge bg="secondary">Wating{suffix ?? ""}</Badge>;
    if (status === "CANCELED")
        return <Badge bg="secondary">Canceled{suffix ?? ""}</Badge>;
    return <Badge>{status}</Badge>;
};

export default StatusView;
