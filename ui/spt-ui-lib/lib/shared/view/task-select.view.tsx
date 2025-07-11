import { useServerObject } from "@lib/shared/http-request";
import { useEffect } from "react";
import { Col, Form, Row, Spinner } from "react-bootstrap";

interface TaskSelectProps {
    value?: string;
    onTaskChange?: (task: string) => void; // Define type for the callback
}

function TaskSelect({ value = "", onTaskChange }: TaskSelectProps) {
    const tasksState = useServerObject<string[]>("/spring-tasks-api/tasks");
    const doGet = tasksState.doGet;

    useEffect(doGet, [doGet]);

    const handleTaskChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
        const newTask = event.target.value ?? "";
        if (newTask !== value) {
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
            <Form.Label column sm="3">
                Task
            </Form.Label>
            <Col sm="9">
                <Form.Select
                    aria-label="Select task"
                    value={value}
                    onChange={handleTaskChange}
                >
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
