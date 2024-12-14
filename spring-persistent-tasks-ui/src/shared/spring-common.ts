export interface Slice<T> {
    content: T[],
    page: Page
}

export interface Page {
    number: number,
    size: number,
    totalElements: number,
    totalPages: number,
}