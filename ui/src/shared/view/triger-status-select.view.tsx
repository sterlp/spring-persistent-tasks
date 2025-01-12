import { TriggerStatus } from "@src/server-api";
import { Form } from "react-bootstrap";

interface TaskSelectProps {
    value: string;
    onTaskChange: (task: string) => void;
}

function TriggerStatusSelect({ value = "", onTaskChange }: TaskSelectProps) {
    const handleTaskChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
        const newTask = event.target.value ?? "";
        if (newTask !== value) {
            if (onTaskChange) onTaskChange(newTask);
        }
    };

    return (
        <Form.Select value={value} onChange={handleTaskChange}>
            <option value={"" as TriggerStatus}>Any state</option>
            <option value={"WAITING" as TriggerStatus}>Waiting</option>
            <option value={"RUNNING" as TriggerStatus}>Running</option>
            <option value={"SUCCESS" as TriggerStatus}>Success</option>
            <option value={"FAILED" as TriggerStatus}>Failed</option>
            <option value={"CANCELED" as TriggerStatus}>Canceled</option>
        </Form.Select>
    );
}

export default TriggerStatusSelect;
