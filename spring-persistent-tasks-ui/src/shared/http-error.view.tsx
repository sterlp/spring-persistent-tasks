import { Alert } from "react-bootstrap";

const HttpErrorView = ({ error }: { error?: any }) => {
    if (!error) return undefined;

    return (
        <Alert variant="danger">{error.message ?? JSON.stringify(error)}</Alert>
    );
};

export default HttpErrorView;
