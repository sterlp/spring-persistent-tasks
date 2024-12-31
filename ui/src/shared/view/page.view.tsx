import { PagedModel } from "@src/server-api";
import React from "react";
import { Pagination } from "react-bootstrap";

interface PageViewProps {
    data?: PagedModel<unknown>;
    onPage: (page: number) => void;
}

const PageView: React.FC<PageViewProps> = ({ data, onPage }) => {
    const isDataAvailable = !!data && !!data.content;
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

    const displayCount = isDataAvailable
        ? `${contentLength + pageSize * currentPage} / ${totalElements}`
        : "-";

    return (
        <Pagination className="mb-0 mt-0 align-items-center">
            <Pagination.Prev
                data-testid="prev"
                onClick={handlePrevClick}
                disabled={!isDataAvailable || currentPage === 0}
            />
            <strong className="ms-2 me-2">{displayCount}</strong>
            <Pagination.Next
                data-testid="next"
                onClick={handleNextClick}
                disabled={!isDataAvailable || currentPage >= totalPages - 1}
            />
        </Pagination>
    );
};

export default PageView;
