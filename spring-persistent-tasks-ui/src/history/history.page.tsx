import { useEffect, useState } from "react";
import { useServerObject } from "@src/shared/http-request";
import { PagedModel, Trigger } from "@src/server-api";
import { Col, Row, Stack } from "react-bootstrap";
import HttpErrorView from "@src/shared/http-error.view";
import PageView from "@src/shared/page.view";
import ReloadButton from "@src/shared/reload-button";
import TriggerItemView from "@src/trigger/views/trigger-list-item.view";

const HistoryPage = () => {
    const [page, setPage] = useState(0);
    const triggers = useServerObject<PagedModel<Trigger>>(
        "/spring-tasks-api/history"
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
        <>
            <Row>
                <HttpErrorView error={triggers.error} />
            </Row>
            <Stack gap={1}>
                <Row className="align-items-center">
                    <div></div>
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
                    <TriggerItemView key={"history-" + t.id} trigger={t} />
                ))}
            </Stack>
        </>
    );
};

export default HistoryPage;
