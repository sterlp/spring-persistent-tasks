import { useEffect, useState } from "react";
import {
    Accordion,
    Button,
    Card,
    Col,
    Container,
    Form,
    Pagination,
    Row,
    Spinner,
    Stack,
} from "react-bootstrap";
import { PagedModel, Trigger } from "../../server-api";
import { useServerObject } from "../../shared/http-request";
import LabeledText from "../../shared/labled-text";
import TriggerStatusView from "./trigger-staus.view";
import JsonView from "@uiw/react-json-view";
import ReloadButton from "../../shared/reload-button";
import TriggerItemView from "./trigger-list-item.view";

const TriggersView = () => {
    const [page, setPage] = useState(0);
    const triggers = useServerObject<PagedModel<Trigger>>(
        "/spring-tasks-api/triggers"
    );

    const doReload = () => {
        triggers.doGet("?size=5&page=" + page);
    };

    useEffect(doReload, [page]);
    useEffect(() => {
        const intervalId = setInterval(doReload, 10000);
        return () => clearInterval(intervalId);
    }, [page]);

    return (
        <Stack gap={1}>
            <Row>
                <Col className="d-flex justify-content-between align-items-center">
                    <div>
                        <TaskSelect />
                    </div>
                    <PageView
                        onPage={(p) => setPage(p)}
                        data={triggers.data}
                        className="mt-2 mb-2"
                    />
                    <ReloadButton
                        isLoading={triggers.isLoading}
                        onClick={doReload}
                    />
                </Col>
            </Row>
            {triggers.data?.content.map((t) => (
                <TriggerItemView key={t.key} trigger={t} />
            ))}
        </Stack>
    );
};

export default TriggersView;

function TaskSelect() {
    const tasksState = useServerObject<string[]>("/spring-tasks-api/tasks");
    useEffect(tasksState.doGet, []);
    const options = tasksState.data?.map((t) => <option>{t}</option>);
    return (
        <>
            {tasksState.isLoading ? (
                <Spinner />
            ) : (
                <Form.Select aria-label="Default select example">
                    <option value="">All</option>

                    <option value="2">{tasksState.data?.length}</option>
                    <option value="3">{tasksState.data}</option>
                </Form.Select>
            )}
        </>
    );
}

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
