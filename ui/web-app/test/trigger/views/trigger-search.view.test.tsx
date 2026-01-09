import TriggerSearchView from "@src/trigger/views/trigger-search.view";
import { render } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import Router from "crossroad";

describe("TriggerSearchView Tests", () => {
    it("renders successfully", () => {
        // GIVEN / WHEN
        const { container } = render(
            <Router>
                <TriggerSearchView
                    url="/spring-tasks-api/triggers"
                    allowUpdateAndCancel={true}
                />
            </Router>
        );

        // THEN
        expect(container).toBeInTheDocument();
    });
});
