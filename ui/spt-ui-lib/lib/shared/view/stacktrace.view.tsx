import { Accordion } from "react-bootstrap";

const StackTraceView = ({
    title,
    error,
}: {
    title?: string;
    error?: string;
}) => {
    if (!error) return undefined;

    return (
        <Accordion>
            <Accordion.Item eventKey={title + "stack"}>
                <Accordion.Header>{title}</Accordion.Header>
                <Accordion.Body>
                    <pre>
                        <small>
                            <code>{error}</code>
                        </small>
                    </pre>
                </Accordion.Body>
            </Accordion.Item>
        </Accordion>
    );
};

export default StackTraceView;
