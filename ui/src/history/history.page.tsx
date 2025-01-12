import { PagedModel, Trigger } from "@src/server-api";
import { useServerObject } from "@src/shared/http-request";
import useAutoRefresh from "@src/shared/use-auto-refresh";
import HttpErrorView from "@src/shared/view/http-error.view";
import PageView from "@src/shared/view/page.view";
import ReloadButton from "@src/shared/view/reload-button.view";
import TriggerItemView from "@src/shared/view/trigger-list-item.view";
import TaskSelect from "@src/task/view/task-select.view";
import { useState } from "react";
import { Accordion, Col, Form, Row, Stack } from "react-bootstrap";

const HistoryPage = () => {
    const [page, setPage] = useState(0);
    const [taskName, setTaskName] = useState("");
    const [id, setId] = useState("");

    const triggers = useServerObject<PagedModel<Trigger>>(
        "/spring-tasks-api/history"
    );

    const doReload = () => {
        triggers.doGet(
            "?size=10&page=" + page + "&taskName=" + taskName + "&id=" + id
        );
    };

    useAutoRefresh(10000, doReload, [page, taskName, id]);

    return (
        <>
            <Stack gap={1}>
                <HttpErrorView error={triggers.error} />
                <Row>
                    <Col>
                        <Form.Control
                            type="text"
                            placeholder="Search..."
                            onKeyUp={(e) =>
                                e.key == "Enter"
                                    ? setId(
                                          (e.target as HTMLInputElement).value
                                      )
                                    : null
                            }
                        />
                    </Col>
                </Row>
                <Row className="align-items-center mb-2">
                    <Col>
                        <TaskSelect onTaskChange={setTaskName} />
                    </Col>
                    <Col>
                        <PageView
                            onPage={(p) => setPage(p)}
                            data={triggers.data}
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
                <Accordion>
                    {triggers.data?.content.map((t) => (
                        <TriggerItemView key={"history-" + t.id} trigger={t} />
                    ))}
                </Accordion>
            </Stack>
        </>
    );
};

export default HistoryPage;
