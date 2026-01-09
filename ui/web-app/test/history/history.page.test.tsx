import HistoryPage from "@src/history/history.page";
import { render } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import Router from "crossroad";

describe("HistoryPage Tests", () => {
    it("renders successfully", () => {
        // GIVEN / WHEN
        const { container } = render(
            <Router>
                <HistoryPage />
            </Router>
        );

        // THEN
        expect(container).toBeInTheDocument();
    });
});
