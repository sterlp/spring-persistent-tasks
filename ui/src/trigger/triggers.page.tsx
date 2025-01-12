import { PagedModel, Trigger } from "@src/server-api";
import { useServerObject } from "@src/shared/http-request";
import useAutoRefresh from "@src/shared/use-auto-refresh";
import HttpErrorView from "@src/shared/view/http-error.view";
import PageView from "@src/shared/view/page.view";
import ReloadButton from "@src/shared/view/reload-button.view";
import TriggerStatusSelect from "@src/shared/view/triger-status-select.view";
import TaskSelect from "@src/task/view/task-select.view";
import { useQuery } from "crossroad";
import { Accordion, Col, Form, Row, Stack } from "react-bootstrap";
import TriggerItemView from "../shared/view/trigger-list-item.view";

const TriggersPage = () => {
    const [query, setQuery] = useQuery();
    const triggers = useServerObject<PagedModel<Trigger>>(
        "/spring-tasks-api/triggers"
    );
    const doReload = () => {
        return triggers.doGet(
            "?size=10&" + new URLSearchParams(query).toString()
        );
    };
    console.info(query);
    useAutoRefresh(10000, doReload, [query]);

    return (
        <Stack gap={1}>
            <HttpErrorView error={triggers.error} />
            <Row>
                <Col>
                    <Form.Control
                        value={query.id || ""}
                        type="text"
                        placeholder="Search..."
                        onKeyUp={(e) =>
                            e.key == "Enter"
                                ? setQuery((prev) => ({
                                      ...prev,
                                      id: (e.target as HTMLInputElement).value,
                                  }))
                                : null
                        }
                    />
                </Col>
                <Col>
                    <TriggerStatusSelect
                        value={query.status}
                        onTaskChange={(status) =>
                            setQuery((prev) => ({
                                ...prev,
                                status,
                            }))
                        }
                    />
                </Col>
            </Row>
            <Row className="align-items-center mb-2">
                <Col>
                    <TaskSelect
                        value={query.taskName}
                        onTaskChange={(taskName) =>
                            setQuery((prev) => ({
                                ...prev,
                                taskName: taskName,
                            }))
                        }
                    />
                </Col>
                <Col className="align-items-center">
                    <PageView
                        onPage={(page) =>
                            setQuery((prev) => ({
                                ...prev,
                                page: page + "",
                            }))
                        }
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
                    <TriggerItemView
                        key={t.id + "-" + t.key.id}
                        trigger={t}
                        afterTriggerChanged={doReload}
                    />
                ))}
            </Accordion>
        </Stack>
    );
};

export default TriggersPage;
