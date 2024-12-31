export function formatDateTime(inputDate?: string | Date): string {
    if (!inputDate) return "";
    const date = inputDate instanceof Date ? inputDate : new Date(inputDate);

    const now = new Date();
    const isToday = date.toDateString() === now.toDateString();
    const options = {
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
        hour12: false, // Use 24-hour format
    } as Intl.DateTimeFormatOptions;

    if (!isToday) {
        options.year = "numeric";
        options.month = "numeric";
        options.day = "numeric";
    }
    return new Intl.DateTimeFormat(
        navigator.language || "en-US",
        options
    ).format(date);
}

export function formatMs(ms?: number) {
    if (ms === 0) return "0ms";
    if (!ms) return "";
    if (ms < 9999) return ms + "ms";

    const inS = Math.floor(ms / 1000);
    if (ms < 99999) {
        return inS + "s " + (ms - inS * 1000) + "ms";
    }
    const inMin = Math.floor(inS / 60);
    return inMin + "min " + (inS - inMin * 60) + "s";
}