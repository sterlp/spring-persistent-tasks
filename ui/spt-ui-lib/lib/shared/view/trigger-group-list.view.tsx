import type { PagedModel, TriggerGroup } from "@lib/server-api";
import { useEffect } from "react";
import { Card, Stack } from "react-bootstrap";
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
    useEffect(
        () => triggers.doGet("?" + new URLSearchParams(filter).toString()),
        [filter]
    );

    return (
        <>
            {triggers.isLoading ? <LoadingView /> : undefined}
            <HttpErrorView error={triggers.error} />
            <Stack gap={2}>
                {triggers.data?.content?.map((t, i) => (
                    <Card>
                        <Card.Body>
                            <TriggerGroupView
                                key={t.groupByValue + i}
                                triggerGroup={t}
                                onGroupClick={onGroupClick}
                            />
                        </Card.Body>
                    </Card>
                ))}
            </Stack>
        </>
    );
};

export default TriggerGroupListView;
