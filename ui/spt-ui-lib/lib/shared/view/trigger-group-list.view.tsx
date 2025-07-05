import type { PagedModel, TriggerGroup } from "@lib/server-api";
import { useEffect } from "react";
import { Stack } from "react-bootstrap";
import { useServerObject } from "../http-request";
import HttpErrorView from "./http-error.view";
import LoadingView from "./loading.view";
import TriggerGroupView from "./trigger-group.view";

/**
 * /spring-tasks-api/triggers-grouped
 * /spring-tasks-api/history-grouped
 */
interface TriggerGroupViewProps {
    url: string;
    onGroupClick?: (v: string) => void;
    filter?: Record<string, string>;
}

const TriggerGroupListView = ({
    url,
    onGroupClick,
    filter,
}: TriggerGroupViewProps) => {
    const triggers = useServerObject<PagedModel<TriggerGroup>>(url);
    const query = "?" + new URLSearchParams(filter).toString();
    const doGet = triggers.doGet;

    useEffect(() => doGet(query), [query, doGet]);

    return (
        <>
            {triggers.isLoading ? <LoadingView /> : undefined}
            <HttpErrorView error={triggers.error} />
            <Stack gap={2}>
                {triggers.data?.content?.map((t, i) => (
                    <TriggerGroupView
                        className={i % 2 === 0 ? "bg-light" : ""}
                        key={t.groupByValue + i}
                        triggerGroup={t}
                        onGroupClick={onGroupClick}
                    />
                ))}
            </Stack>
        </>
    );
};

export default TriggerGroupListView;
