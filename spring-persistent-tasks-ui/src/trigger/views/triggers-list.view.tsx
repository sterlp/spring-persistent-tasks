import { useEffect, useState } from "react";
import { Col, Form, Pagination, Row, Stack } from "react-bootstrap";
import { PagedModel, Trigger } from "../../server-api";
import { useServerObject } from "../../shared/http-request";
import ReloadButton from "../../shared/reload-button";
import TaskSelect from "../../task/view/task-select.view";
import TriggerItemView from "./trigger-list-item.view";

const TriggersView = () => {
    const [page, setPage] = useState(0);
    const [selectedTask, setSelectedTask] = useState("");
    const triggers = useServerObject<PagedModel<Trigger>>(
        "/spring-tasks-api/triggers"
    );

    const doReload = () => {
        triggers.doGet("?size=5&page=" + page + "&taskId=" + selectedTask);
    };

    useEffect(doReload, [page, selectedTask]);
    useEffect(() => {
        const intervalId = setInterval(doReload, 10000);
        return () => clearInterval(intervalId);
    }, [page, selectedTask]);

    // className="d-flex justify-content-between align-items-center"
    return (
        <Stack gap={1}>
            <Row className="align-items-center">
                <Col>
                    <TaskSelect onTaskChange={setSelectedTask} />
                </Col>
                <Col>
                    <PageView
                        onPage={(p) => setPage(p)}
                        data={triggers.data}
                        className="mt-2 mb-2"
                    />
                </Col>
                <Col>
                    <ReloadButton
                        className="float-end"
                        isLoading={triggers.isLoading}
                        onClick={doReload}
                    />
                </Col>
            </Row>
            {triggers.data?.content.map((t) => (
                <TriggerItemView key={t.id + ""} trigger={t} />
            ))}
        </Stack>
    );
};

export default TriggersView;

const PageView = ({
    data,
    className,
    onPage,
}: {
    className: string;
    data?: PagedModel<unknown>;
    onPage(page: number): void;
}) => {
    if (!data || !data.content)
        return (
            <Pagination className={className}>
                <Pagination.Prev disabled />
                <Form.Label className="mt-2 ms-2 me-1">-</Form.Label>
                <Pagination.Next disabled />
            </Pagination>
        );
    return (
        <Pagination className="mt-2 mb-2">
            <Pagination.Prev
                onClick={() => onPage(data.page.number - 1)}
                disabled={data.page.number === 0}
            />
            <Form.Label className="mt-2 ms-2 me-1">
                {data.content.length +
                    data.page.size * data.page.number +
                    " / " +
                    data.page.totalElements}
            </Form.Label>
            <Pagination.Next
                onClick={() => onPage(data.page.number + 1)}
                disabled={data.page.number >= data.page.totalPages - 1}
            />
        </Pagination>
    );
};
