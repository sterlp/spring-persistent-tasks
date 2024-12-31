import { formatMs } from "@src/shared/date.util";


describe("Date Util Test", () => {
    it("formatMs", () => {
        expect(formatMs(undefined)).toBe("");
        expect(formatMs(9000)).toBe("9000ms");
        expect(formatMs(59*1000 + 50)).toBe("59s 50ms");
        expect(formatMs(7*60*1000 + 50)).toBe("7min 0s");
        expect(formatMs(3*60*60*1000 + 51000)).toBe("180min 51s");
    });
});