import { formatMs } from "@lib/shared";
import { describe, expect, it } from "vitest";

describe("Date Util Test", () => {
    it("formatMs", () => {
        expect(formatMs(undefined)).toBe("-");
        expect(formatMs(9000)).toBe("9000ms");
        expect(formatMs(59 * 1000 + 50)).toBe("59s 50ms");
        expect(formatMs(7 * 60 * 1000 + 50)).toBe("7min 0s");
        expect(formatMs(3 * 60 * 60 * 1000 + 51000)).toBe("180min 51s");

        expect(formatMs(3 * 24 * 60 * 60 * 1000 + 2 * 60 * 60 * 1000)).toBe(
            "3d 2h"
        );
    });
});
