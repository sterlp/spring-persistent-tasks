import CronJobsPage from "@src/cron/cron.page";
import { render } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import Router from "crossroad";

describe("CronJobsPage Tests", () => {
    it("renders successfully", () => {
        // GIVEN / WHEN
        const { container } = render(
            <Router>
                <CronJobsPage />
            </Router>
        );

        // THEN
        expect(container).toBeInTheDocument();
    });
});
