import { TriggerStatus } from "@src/server-api";
import { Badge } from "react-bootstrap";

interface Props {
    status: TriggerStatus;
    suffix?: string;
    pill?: boolean;
}
const StatusView = ({ status, pill = false, suffix }: Props) => {
    if (status === "SUCCESS") {
        return (
            <Badge pill={pill} bg="success">
                Success{suffix ?? ""}
            </Badge>
        );
    }
    if (status === "FAILED") {
        return (
            <Badge pill={pill} bg="danger">
                Failed{suffix ?? ""}
            </Badge>
        );
    }
    if (status === "RUNNING") {
        return <Badge pill={pill}>Running{suffix ?? ""}</Badge>;
    }
    if (status === "WAITING")
        return (
            <Badge pill={pill} bg="secondary">
                Wating{suffix ?? ""}
            </Badge>
        );
    if (status === "CANCELED") {
        return (
            <Badge pill={pill} bg="secondary">
                Canceled{suffix ?? ""}
            </Badge>
        );
    }
    return <Badge pill={pill}>{status}</Badge>;
};

export default StatusView;
