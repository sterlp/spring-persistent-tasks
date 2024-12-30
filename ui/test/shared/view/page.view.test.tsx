import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import PageView from "@src/shared/view/page.view";
import { PagedModel } from "@src/server-api";

const mockOnPage = vi.fn();

const renderComponent = (data?: PagedModel<unknown>) =>
    render(<PageView data={data} onPage={mockOnPage} />);

describe("PageView Component", () => {
    beforeEach(() => {
        mockOnPage.mockClear();
    });

    it("No data: should disable both buttons and display '-'", () => {
        renderComponent(undefined);

        expect(screen.getByText("-")).toBeInTheDocument();
        expect(screen.getByTestId("next").closest("li")).toHaveClass(
            "disabled"
        );
        expect(screen.getByTestId("prev").closest("li")).toHaveClass(
            "disabled"
        );
    });

    it("Middle page: should enable both Prev and Next", () => {
        renderComponent({
            page: { number: 1, size: 10, totalElements: 30, totalPages: 3 },
            content: new Array(10),
        });

        expect(screen.getByText("20 / 30")).toBeInTheDocument();
        expect(
            screen.getByRole("button", { name: "Previous" })
        ).not.toBeDisabled();
        expect(screen.getByRole("button", { name: "Next" })).not.toBeDisabled();

        fireEvent.click(screen.getByRole("button", { name: "Previous" }));
        expect(mockOnPage).toHaveBeenCalledWith(0);

        fireEvent.click(screen.getByRole("button", { name: "Next" }));
        expect(mockOnPage).toHaveBeenCalledWith(2);
    });

    it("Empty page: should disable both buttons and show count as 0 / total", () => {
        renderComponent({
            page: { number: 0, size: 10, totalElements: 10, totalPages: 1 },
            content: [],
        });

        expect(screen.getByText("0 / 10")).toBeInTheDocument();
        expect(screen.getByTestId("next").closest("li")).toHaveClass(
            "disabled"
        );
        expect(screen.getByTestId("prev").closest("li")).toHaveClass(
            "disabled"
        );
    });
});
