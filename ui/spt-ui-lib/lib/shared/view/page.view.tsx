import type { PagedModel } from "@lib/server-api";
import React from "react";
import { Pagination, Spinner } from "react-bootstrap";

interface PageViewProps {
    data?: PagedModel<unknown>;
    isLoading?: boolean;
    onPage: (page: number) => void;
}

const PageView: React.FC<PageViewProps> = ({
    data,
    isLoading = false,
    onPage,
}) => {
    if (!data || isLoading) return <PageViewLoaing />;

    const currentPage = data?.page.number ?? 0;
    const totalPages = data?.page.totalPages ?? 0;
    const totalElements = data?.page.totalElements ?? 0;
    const pageSize = data?.page.size ?? 0;
    const contentLength = data?.content?.length ?? 0;

    const handlePrevClick = () => {
        if (currentPage > 0) {
            onPage(currentPage - 1);
        }
    };

    const handleNextClick = () => {
        if (currentPage < totalPages - 1) {
            onPage(currentPage + 1);
        }
    };

    const displayCount = `${
        contentLength + pageSize * currentPage
    } / ${totalElements}`;

    return (
        <Pagination className="mb-0 mt-0 d-felx align-items-center justify-content-center">
            <Pagination.Prev
                data-testid="prev"
                onClick={handlePrevClick}
                disabled={currentPage === 0}
            />
            <strong className="ms-2 me-2">{displayCount}</strong>
            <Pagination.Next
                data-testid="next"
                onClick={handleNextClick}
                disabled={currentPage >= totalPages - 1}
            />
        </Pagination>
    );
};

export default PageView;

const PageViewLoaing = () => (
    <Pagination className="mb-0 mt-0 d-felx align-items-center justify-content-center">
        <Pagination.Prev data-testid="prev" disabled={true} className="me-2" />
        <Spinner animation="border" variant="primary" title="Loading ..." />
        <Pagination.Next data-testid="next" disabled={true} className="ms-2" />
    </Pagination>
);
