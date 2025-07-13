import type { Trigger } from "@lib/server-api";
import { useState } from "react";
import { Accordion, Container } from "react-bootstrap";
import TriggerActionsView from "./trigger-actions.view";
import TriggerCompactView from "./trigger-compact.view";
import TriggerView from "./trigger.view";

interface TriggerListViewProps {
    triggers?: Trigger[];
    afterTriggerChanged?: () => void;
    afterTriggerReRun?: () => void;
    onFieldClick?: (key: string, value?: string) => void;
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

interface TriggerProps {
    trigger: Trigger;
    afterTriggerChanged?: () => void;
    afterTriggerReRun?: () => void;
    onFieldClick?: (key: string, value?: string) => void;
}

const TriggerListItemView = ({
    trigger,
    afterTriggerChanged,
    afterTriggerReRun,
    onFieldClick,
}: TriggerProps) => {
    const [body, setBody] = useState(false);

    return (
        <Accordion.Item eventKey={"trigger-" + trigger.id}>
            <Accordion.Header>
                <Container>
                    <TriggerCompactView
                        key={trigger.id + "TriggerCompactView"}
                        trigger={trigger}
                    />
                </Container>
            </Accordion.Header>
            <Accordion.Body
                onEnter={() => setBody(true)}
                onExit={() => setBody(false)}
            >
                {body ? (
                    <>
                        <TriggerActionsView
                            trigger={trigger}
                            afterTriggerChanged={afterTriggerChanged}
                            afterTriggerReRun={afterTriggerReRun}
                        />

                        <TriggerView
                            key={trigger.id + "-TriggerDetailsView"}
                            trigger={trigger}
                            onClick={onFieldClick}
                        />
                    </>
                ) : (
                    ""
                )}
            </Accordion.Body>
        </Accordion.Item>
    );
};
