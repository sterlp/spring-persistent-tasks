import { useEffect, useState } from "react";
import { useServerObject } from "@src/shared/http-request";
import { PagedModel, Trigger } from "@src/server-api";
import { Col, Row, Stack } from "react-bootstrap";
import HttpErrorView from "@src/shared/http-error.view";
import TaskSelect from "@src/task/view/task-select.view";
import PageView from "@src/shared/page.view";
import ReloadButton from "@src/shared/reload-button";
import TriggerItemView from "./views/trigger-list-item.view";

const TriggersPage = () => {
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

    return (
        <Stack gap={1}>
            <HttpErrorView error={triggers.error} />
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

export default TriggersPage;
