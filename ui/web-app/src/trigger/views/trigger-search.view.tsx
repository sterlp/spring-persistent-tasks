import { useQuery } from "crossroad";
import { useEffect, useState } from "react";
import { Col, Form, Row, Stack } from "react-bootstrap";
import {
    HttpErrorView,
    PagedModel,
    PageView,
    ReloadButton,
    TaskSelect,
    Trigger,
    TriggerListView,
    TriggerStatusSelect,
    useAutoRefresh,
    useServerObject,
} from "spring-persistent-tasks-ui";

interface Props {
    url: string;
    afterTriggerReRun?: () => void;
    allowUpdateAndCancel: boolean;
}
const TriggerSearchView = ({
    url,
    afterTriggerReRun,
    allowUpdateAndCancel,
}: Props) => {
    const [query, setQuery] = useQuery();
    const [search, setSearch] = useState(query.search || "");
    const triggers = useServerObject<PagedModel<Trigger>>(url, {
        page: { number: 0, size: 0, totalElements: 0, totalPages: 0 },
        content: [],
    });

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
    const doSetPage = (page: number) => {
        setQuery((prev) => ({
            ...prev,
            page: page + "",
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
                    <PageView onPage={doSetPage} data={triggers.data} />
                </Col>
                <Col>
                    <ReloadButton
                        className="float-end"
                        isLoading={triggers.isLoading}
                        onClick={doReload}
                    />
                </Col>
            </Row>
            <TriggerListView
                triggers={triggers.data?.content}
                afterTriggerReRun={afterTriggerReRun}
                afterTriggerChanged={
                    allowUpdateAndCancel ? doReload : undefined
                }
                onFieldClick={(k, v) => doUpdateQuery({ [k]: v })}
            />
        </Stack>
    );
};

export default TriggerSearchView;
