import CronJobsPage from "@src/cron/cron.page";
import CronJobsView from "@src/cron/views/cron-jobs.view";
import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import Router from "crossroad";

describe("CronJobsPage Tests", () => {
    it("renders page successfully", () => {
        // GIVEN / WHEN
        const { container } = render(
            <Router>
                <CronJobsPage />
            </Router>
        );

        // THEN
        expect(container).toBeInTheDocument();
    });

    it("renders view with expected elements", () => {
        // GIVEN / WHEN
        render(
            <Router>
                <CronJobsView />
            </Router>
        );

        // THEN
        expect(screen.getByText("Cron Jobs")).toBeInTheDocument();
        expect(screen.getByPlaceholderText("Search by ID, task name, schedule, or tag...")).toBeInTheDocument();
        expect(screen.getByText("Manage scheduled cron triggers. View configurations, suspend/resume jobs, and navigate to their triggers or history.")).toBeInTheDocument();
    });
});
