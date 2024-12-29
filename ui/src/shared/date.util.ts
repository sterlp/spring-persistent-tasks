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