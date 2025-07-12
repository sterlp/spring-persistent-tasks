import { Alert } from "react-bootstrap";
import type { ServerObject } from "../http-request";
import HttpErrorView from "./http-error.view";
import LoadingView from "./loading.view";

type HttpRequestViewProps<T> = {
    request: ServerObject<T>;
    render: (data: T) => React.ReactNode;
    emptyMessage?: React.ReactNode;
};

const HttpRequestView = <T,>({
    request,
    render,
    emptyMessage = <Alert variant="info">No data found</Alert>,
}: HttpRequestViewProps<T>) => {
    if (request.error) return <HttpErrorView error={request.error} />;
    if (request.isLoading) return <LoadingView />;
    if (!request.data) return <>{emptyMessage}</>;

    return <>{render(request.data)}</>;
};

export default HttpRequestView;
