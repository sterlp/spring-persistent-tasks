export function formatShortDateTime(inputDate?: string | Date): string {
    if (!inputDate) return "";
    const date = inputDate instanceof Date ? inputDate : new Date(inputDate);

    const now = new Date();
    const isToday = date.toDateString() === now.toDateString();

    const secondsPast = Math.floor(now.getTime() - date.getTime() / 1000);
    if (secondsPast > 0 && secondsPast < 6) return "just now";
    if (secondsPast > 0 && secondsPast < 30) return secondsPast + "s";

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

export function formatDateTime(inputDate?: string | Date): string {
    if (!inputDate) return "";
    const date = inputDate instanceof Date ? inputDate : new Date(inputDate);
    return new Intl.DateTimeFormat(
        navigator.language || "en-US",
        {
            day: "2-digit",
            month: "2-digit",
            year: "2-digit",
            hour: "2-digit",
            minute: "2-digit",
            second: "2-digit",
            hour12: false, // Use 24-hour format
        }
    ).format(date);
}

export function formatMs(ms?: number) {
    if (ms === undefined || ms === null) return "-";
    if (ms === 0) return "0ms";

    if (ms < 9999) return Math.floor(ms) + "ms";

    const inS = Math.floor(ms / 1000);
    if (ms < 99999) {
        return inS + "s " + (ms - inS * 1000) + "ms";
    }
    const inMin = Math.floor(inS / 60);
    return inMin + "min " + (inS - inMin * 60) + "s";
}