import { PagedModel, Trigger } from "@src/server-api";
import { useServerObject } from "@src/shared/http-request";
import useAutoRefresh from "@src/shared/use-auto-refresh";
import HttpErrorView from "@src/shared/view/http-error.view";
import PageView from "@src/shared/view/page.view";
import ReloadButton from "@src/shared/view/reload-button.view";
import TaskSelect from "@src/task/view/task-select.view";
import { useState } from "react";
import { Col, Form, Row, Stack } from "react-bootstrap";
import TriggerItemView from "../shared/view/trigger-list-item.view";

const TriggersPage = () => {
    const [page, setPage] = useState(0);
    const [taskName, setTaskName] = useState("");
    const [id, setId] = useState("");
    const triggers = useServerObject<PagedModel<Trigger>>(
        "/spring-tasks-api/triggers"
    );

    const doReload = () => {
        triggers.doGet(
            "?size=10&page=" + page + "&taskName=" + taskName + "&id=" + id
        );
    };

    useAutoRefresh(10000, doReload, [page, taskName, id]);

    return (
        <Stack gap={1}>
            <HttpErrorView error={triggers.error} />
            <Row>
                <Col>
                    <Form.Control
                        type="text"
                        placeholder="Search..."
                        onKeyUp={(e) =>
                            e.key == "Enter"
                                ? setId((e.target as HTMLInputElement).value)
                                : null
                        }
                    />
                </Col>
            </Row>
            <Row className="align-items-center mb-2">
                <Col>
                    <TaskSelect onTaskChange={setTaskName} />
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
                    key={t.id + "-" + t.key.id}
                    trigger={t}
                    afterTriggerChanged={doReload}
                />
            ))}
        </Stack>
    );
};

export default TriggersPage;
