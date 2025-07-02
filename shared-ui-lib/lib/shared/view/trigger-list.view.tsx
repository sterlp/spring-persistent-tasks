import type { Trigger } from "@lib/server-api";
import { Accordion } from "react-bootstrap";
import TriggerListItemView from "./trigger-list-item.view";

interface TriggerListViewProps {
    triggers?: Trigger[];
    afterTriggerChanged?: () => void;
    afterTriggerReRun?: () => void;
    onFieldClick: (key: string, value?: string) => void;
}

const TriggerListView = ({
    triggers,
    afterTriggerChanged,
    afterTriggerReRun,
    onFieldClick,
}: TriggerListViewProps) => {
    if (!triggers || triggers.length === 0) {
        return <div className="text-center">Nothing found</div>;
    }
    return (
        <Accordion flush>
            {triggers.map((t) => (
                <TriggerListItemView
                    key={t.id + "-" + t.key.id}
                    trigger={t}
                    afterTriggerReRun={afterTriggerReRun}
                    afterTriggerChanged={afterTriggerChanged}
                    onFieldClick={onFieldClick}
                />
            ))}
        </Accordion>
    );
};

export default TriggerListView;
