import { useEffect, useState } from "react";
import { Col, Form, Row, Spinner } from "react-bootstrap";
import { useServerObject } from "../../shared/http-request";

interface TaskSelectProps {
    onTaskChange?: (task: string) => void; // Define type for the callback
}

function TaskSelect({ onTaskChange }: TaskSelectProps) {
    const [selectedTask, setSelectedTask] = useState<string>("");
    const tasksState = useServerObject<string[]>("/spring-tasks-api/tasks");

    useEffect(tasksState.doGet, []);

    const handleTaskChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
        const newTask = event.target.value ?? "";
        if (newTask !== selectedTask) {
            setSelectedTask(newTask);
            if (onTaskChange) onTaskChange(newTask);
        }
    };

    if (tasksState.isLoading) {
        return (
            <Form.Group as={Row}>
                <Form.Label column sm="2">
                    Task
                </Form.Label>
                <Col sm="10">
                    <Spinner animation="border" size="sm" />
                </Col>
            </Form.Group>
        );
    }

    return (
        <Form.Group as={Row}>
            <Form.Label column sm="2">
                Task
            </Form.Label>
            <Col sm="10">
                <Form.Select aria-label="Select task" value={selectedTask || ""} onChange={handleTaskChange}>
                    <option value="">All</option>
                    {tasksState.data?.map((task, index) => (
                        <option key={index} value={task}>
                            {task}
                        </option>
                    ))}
                </Form.Select>
            </Col>
        </Form.Group>
    );
}

export default TaskSelect;
