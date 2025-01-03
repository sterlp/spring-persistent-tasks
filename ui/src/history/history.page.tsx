import { useEffect, useState } from "react";
import { useServerObject } from "@src/shared/http-request";
import { PagedModel, Trigger } from "@src/server-api";
import { Col, Row, Stack } from "react-bootstrap";
import HttpErrorView from "@src/shared/view/http-error.view";
import PageView from "@src/shared/view/page.view";
import ReloadButton from "@src/shared/view/reload-button.view";
import TriggerItemView from "@src/shared/view/trigger-list-item.view";

const HistoryPage = () => {
    const [page, setPage] = useState(0);
    const triggers = useServerObject<PagedModel<Trigger>>(
        "/spring-tasks-api/history"
    );

    const doReload = () => {
        triggers.doGet("?size=10&page=" + page);
    };

    useEffect(doReload, [page]);
    useEffect(() => {
        const intervalId = setInterval(doReload, 10000);
        return () => clearInterval(intervalId);
    }, [page]);

    return (
        <>
            <Stack gap={1}>
                <HttpErrorView error={triggers.error} />
                <Row className="align-items-center">
                    <div></div>
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
                {triggers.data?.content.map((t) => (
                    <TriggerItemView key={"history-" + t.id} trigger={t} />
                ))}
            </Stack>
        </>
    );
};

export default HistoryPage;
