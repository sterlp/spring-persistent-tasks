import { useServerObject } from "@lib/shared/http-request";
import useAutoRefresh from "@lib/shared/use-auto-refresh";
import HttpErrorView from "@lib/shared/view/http-error.view";
import PageView from "@lib/shared/view/page.view";
import ReloadButton from "@lib/shared/view/reload-button.view";
import TriggerStatusSelect from "@lib/shared/view/trigger-status-select.view";
import { useQuery } from "crossroad";
import { Accordion, Col, Form, Row, Stack } from "react-bootstrap";
import TriggerListItemView from "./trigger-list-item.view";
import { useEffect, useState } from "react";
import type { PagedModel, Trigger } from "@lib/server-api";
import TaskSelect from "./task-select.view";

interface Props {
    url: string;
    allowUpdateAnCancel: boolean;
    showReRunButton: boolean;
}
const TriggersSearchView = ({
    url,
    allowUpdateAnCancel,
    showReRunButton,
}: Props) => {
    const [query, setQuery] = useQuery();
    const [search, setSearch] = useState(query.search || "");
    const triggers = useServerObject<PagedModel<Trigger>>(url);

    useEffect(() => {
        setSearch(query.search || "");
    }, [query]);

    const doReload = () => {
        return triggers.doGet(
            "?size=10&" + new URLSearchParams(query).toString()
        );
    };
    const doUpdateQuery = (search: object) => {
        setQuery((prev) => ({
            ...prev,
            page: 0 + "",
            ...search,
        }));
    };

    useAutoRefresh(10000, doReload, [query]);

    return (
        <Stack gap={1}>
            <HttpErrorView error={triggers.error} />
            <Row>
                <Col>
                    <Form.Control
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        type="text"
                        placeholder="ID search, '*' any string, '_' any character ..."
                        onKeyUp={(e) =>
                            e.key == "Enter" ? doUpdateQuery({ search }) : null
                        }
                    />
                </Col>
                <Col>
                    <TriggerStatusSelect
                        value={query.status}
                        onTaskChange={(status) => doUpdateQuery({ status })}
                    />
                </Col>
            </Row>
            <Row className="align-items-center mb-2">
                <Col>
                    <TaskSelect
                        value={query.taskName}
                        onTaskChange={(taskName) => doUpdateQuery({ taskName })}
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
                    <TriggerListItemView
                        key={t.id + "-" + t.key.id}
                        trigger={t}
                        showReRunButton={showReRunButton}
                        afterTriggerChanged={
                            allowUpdateAnCancel ? doReload : undefined
                        }
                        onFieldClick={(k, v) => doUpdateQuery({ [k]: v })}
                    />
                ))}
            </Accordion>
        </Stack>
    );
};

export default TriggersSearchView;
