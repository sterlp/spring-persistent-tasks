import TriggersSearchView from "@src/shared/view/trigger-search.view";

const TriggersPage = () => {
    return (
        <TriggersSearchView
            allowUpdateAnCancel={true}
            showReRunButton={false}
            url="/spring-tasks-api/triggers"
        />
    );
};

export default TriggersPage;
