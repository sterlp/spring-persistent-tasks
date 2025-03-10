import TriggersSearchView from "@src/shared/view/trigger-search.view";

const HistoryPage = () => {
    return (
        <TriggersSearchView
            allowUpdateAnCancel={false}
            showReRunButton={true}
            url="/spring-tasks-api/history"
        />
    );
};

export default HistoryPage;
