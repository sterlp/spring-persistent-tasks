import type { PagedModel, TriggerGroup } from "@lib/server-api";
import { Col, Row } from "react-bootstrap";
import { useServerObject } from "../http-request";
import HttpErrorView from "./http-error.view";
import LabeledText from "./labled-text.view";
import LoadingView from "./loading.view";
import { useEffect } from "react";
import { formatDateTime, formatMs } from "../date.util";

/**
 * /spring-tasks-api/triggers-grouped
 * /spring-tasks-api/history-grouped
 */
interface TriggerGroupViewProps {
    url: string;
    onGroupClick?: (v: string) => void;
    filter?: Record<string, string>;
}

const TriggerGroupView = ({
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
            {triggers.data?.content?.map((t) => (
                <Row>
                    <Col sm="5">
                        <LabeledText
                            label="CorrelationId"
                            value={t.groupByValue}
                            onClick={
                                onGroupClick
                                    ? () => onGroupClick(t.groupByValue)
                                    : undefined
                            }
                        />
                    </Col>
                    <Col sm="1">
                        <LabeledText label="Steps" value={t.count} />
                    </Col>
                    <Col sm="2">
                        <LabeledText
                            label="Start"
                            value={formatDateTime(t.minStart)}
                        />
                    </Col>
                    <Col sm="2">
                        <LabeledText
                            label="End"
                            value={formatDateTime(t.maxEnd)}
                        />
                    </Col>
                    <Col sm="2">
                        <LabeledText
                            label="Duration"
                            value={formatMs(t.sumDurationMs)}
                        />
                    </Col>
                </Row>
            ))}
        </>
    );
};

export default TriggerGroupView;
