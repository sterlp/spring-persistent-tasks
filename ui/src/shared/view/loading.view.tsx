import { Spinner } from "react-bootstrap";

const LoadingView = () => (
    <div className="d-flex flex-row justify-content-center align-items-center">
        <Spinner animation="border" />
        <div className="ps-2">Loading...</div>
    </div>
);

export default LoadingView;
