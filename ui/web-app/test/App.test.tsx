import App from "@src/App";
import { render } from "@testing-library/react";
import { describe, expect, it } from "vitest";

describe("App Tests", () => {
    it("renders successfully", () => {
        // GIVEN / WHEN
        const { container } = render(<App />);

        // THEN
        expect(container).toBeInTheDocument();
    });
});
