import TriggersPage from "@src/trigger/triggers.page";
import { render } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import Router from "crossroad";

describe("TriggersPage Tests", () => {
    it("renders successfully", () => {
        // GIVEN / WHEN
        const { container } = render(
            <Router>
                <TriggersPage />
            </Router>
        );

        // THEN
        expect(container).toBeInTheDocument();
    });
});
