import SchedulersPage from "@src/scheduler/scheduler.page";
import { render } from "@testing-library/react";
import { describe, expect, it } from "vitest";

describe("SchedulersPage Tests", () => {
    it("renders successfully", () => {
        // GIVEN / WHEN
        const { container } = render(<SchedulersPage />);

        // THEN
        expect(container).toBeInTheDocument();
    });
});
