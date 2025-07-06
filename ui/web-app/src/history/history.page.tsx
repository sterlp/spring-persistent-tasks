import TriggerSearchView from "@src/trigger/views/trigger-search.view";
import { useUrl } from "crossroad";

const HistoryPage = () => {
    const [_, setUrl] = useUrl();

    return (
        <TriggerSearchView
            afterTriggerReRun={() => setUrl("/task-ui/triggers")}
            allowUpdateAndCancel={false}
            url="/spring-tasks-api/history"
        />
    );
};

export default HistoryPage;
