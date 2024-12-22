import { PagedModel } from "@src/server-api";
import { Form, Pagination } from "react-bootstrap";

const PageView = ({
    data,
    className,
    onPage,
}: {
    className: string;
    data?: PagedModel<unknown>;
    onPage(page: number): void;
}) => {
    if (!data || !data.content)
        return (
            <Pagination className={className}>
                <Pagination.Prev disabled />
                <Form.Label className="mt-2 ms-2 me-1">-</Form.Label>
                <Pagination.Next disabled />
            </Pagination>
        );
    return (
        <Pagination className="mt-2 mb-2">
            <Pagination.Prev
                onClick={() => onPage(data.page.number - 1)}
                disabled={data.page.number === 0}
            />
            <Form.Label className="mt-2 ms-2 me-1">
                {data.content.length +
                    data.page.size * data.page.number +
                    " / " +
                    data.page.totalElements}
            </Form.Label>
            <Pagination.Next
                onClick={() => onPage(data.page.number + 1)}
                disabled={data.page.number >= data.page.totalPages - 1}
            />
        </Pagination>
    );
};

export default PageView;
