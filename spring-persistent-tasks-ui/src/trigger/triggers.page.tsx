import { Row } from "react-bootstrap";
import TriggersView from "./views/triggers-list.view";

const TriggersPage = () => {
    return (
        // className="py-4 px-0 mx-auto"
        <Row>
            <TriggersView />
        </Row>
    );
};

export default TriggersPage;
