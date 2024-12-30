import { PagedModel, Trigger } from "@src/server-api";
import HttpErrorView from "@src/shared/view/http-error.view";
import { useServerObject } from "@src/shared/http-request";
import PageView from "@src/shared/view/page.view";
import ReloadButton from "@src/shared/view/reload-button.view";
import useAutoRefresh from "@src/shared/use-auto-refresh";
import TaskSelect from "@src/task/view/task-select.view";
import { useState } from "react";
import { Col, Row, Stack } from "react-bootstrap";
import TriggerItemView from "../shared/view/trigger-list-item.view";

const TriggersPage = () => {
    const [page, setPage] = useState(0);
    const [selectedTask, setSelectedTask] = useState("");
    const triggers = useServerObject<PagedModel<Trigger>>(
        "/spring-tasks-api/triggers"
    );

    const doReload = () => {
        triggers.doGet("?size=10&page=" + page + "&taskId=" + selectedTask);
    };

    useAutoRefresh(10000, doReload, [page, selectedTask]);

    return (
        <Stack gap={1}>
            <HttpErrorView error={triggers.error} />
            <Row className="align-items-center mb-2">
                <Col>
                    <TaskSelect onTaskChange={setSelectedTask} />
                </Col>
                <Col className="align-items-center">
                    <PageView onPage={(p) => setPage(p)} data={triggers.data} />
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
                <TriggerItemView
                    key={t.id + ""}
                    trigger={t}
                    afterCancel={doReload}
                />
            ))}
        </Stack>
    );
};

export default TriggersPage;
