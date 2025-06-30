import TriggerSearchView from "./views/trigger-search.view";

const TriggersPage = () => {
    return (
        <TriggerSearchView
            allowUpdateAndCancel={true}
            url="/spring-tasks-api/triggers"
        />
    );
};

export default TriggersPage;
