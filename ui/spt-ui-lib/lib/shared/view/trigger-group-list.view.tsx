import type { PagedModel, TriggerGroup } from "@lib/server-api";
import { useEffect, useState } from "react";
import { Stack } from "react-bootstrap";
import { useServerObject } from "../http-request";
import HttpErrorView from "./http-error.view";
import LoadingView from "./loading.view";
import TriggerGroupView from "./trigger-group.view";
import PageView from "./page.view";

/**
 * /spring-tasks-api/triggers-grouped
 * /spring-tasks-api/history-grouped
 */
interface TriggerGroupViewProps {
    url: string;
    onGroupClick?: (v: string) => void;
    pageSize?: number;
    filter?: Record<string, string>;
}

const TriggerGroupListView = ({
    url,
    onGroupClick,
    pageSize = 20,
    filter,
}: TriggerGroupViewProps) => {
    const [page, setPage] = useState(0);
    const triggers = useServerObject<PagedModel<TriggerGroup>>(url);
    const query = `?size=${pageSize}&page=${page}&${new URLSearchParams(
        filter
    ).toString()}`;
    const doGet = triggers.doGet;

    useEffect(() => doGet(query), [query, doGet]);

    return (
        <>
            <HttpErrorView error={triggers.error} />
            {triggers.data ? (
                <Stack gap={2}>
                    {triggers.isLoading ? (
                        <LoadingView style={{ height: 38 }} />
                    ) : (
                        <PageView data={triggers.data} onPage={setPage} />
                    )}

                    {triggers.data?.content?.map((t, i) => (
                        <TriggerGroupView
                            className={i % 2 === 0 ? "bg-light" : ""}
                            key={t.groupByValue + i}
                            triggerGroup={t}
                            onGroupClick={onGroupClick}
                        />
                    ))}
                </Stack>
            ) : undefined}
        </>
    );
};

export default TriggerGroupListView;
