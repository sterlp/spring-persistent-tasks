import { Alert, Col, Row } from "react-bootstrap";

const HttpErrorView = ({ error }: { error: any }) => {
    if (!error) return undefined;
    return (
        <Row className="mt-2">
            <Col>
                <Alert variant="danger">
                    {error.message ? error.message : JSON.stringify(error)}
                </Alert>
            </Col>
        </Row>
    );
};

export default HttpErrorView;
