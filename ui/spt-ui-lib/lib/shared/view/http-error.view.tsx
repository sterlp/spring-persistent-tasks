import { isAxiosError } from "axios";
import { Alert } from "react-bootstrap";
import StackTraceView from "./stacktrace.view";

interface SpringError {
    error: string;
    message: string;
    timestamp?: string;
    status?: number;
    path?: string;
    trace?: string;
}

function isSpringError(data: unknown): data is SpringError {
    return (
        !!data &&
        typeof data === "object" &&
        "error" in data &&
        "message" in data
    );
}

const HttpErrorView = ({ error }: { error: unknown }) => {
    if (!error) return undefined;
    console.warn("HttpErrorView", error);
    if (isAxiosError(error) && isSpringError(error.response?.data)) {
        return (
            <Alert variant="danger">
                <Alert.Heading>
                    {error.response?.data.status} {error.response?.data.error}
                </Alert.Heading>
                <p>{error.response?.data.message}</p>
                <StackTraceView
                    title={error.response?.data.error}
                    error={error.response?.data.trace}
                />
            </Alert>
        );
    }
    return (
        <Alert variant="danger">
            {isAxiosError(error) ? error.message : JSON.stringify(error)}
        </Alert>
    );
};

export default HttpErrorView;
