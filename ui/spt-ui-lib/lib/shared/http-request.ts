import axios, { AxiosError, type AxiosResponse } from "axios";
import { useCallback, useMemo, useRef, useState } from "react";

export interface ServerObject<T> {
    isLoading: boolean;
    data: T | undefined;
    error: Error | AxiosError | unknown;
    doGet(id?: string | number, options?: GetOptions): void;
    doCall(
        urlPart?: string,
        options?: Options
    ): Promise<void | AxiosResponse<T, AxiosError | unknown>>;
}
export interface GetOptions extends Options {
    cache?: boolean;
}
export interface Options {
    method?: HttpMethod;
    dataToSend?: unknown;
    controller?: AbortController;
}

export type HttpMethod = "GET" | "DELETE" | "POST" | "PUT" | "HEADER";

export const useServerObject = <T>(
    url: string,
    startValue?: T
): ServerObject<T> => {
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [data, setData] = useState<T | undefined>(startValue);
    const [error, setError] = useState<AxiosError | unknown>(undefined);

    const cache = useRef<Record<string, T>>({}); // In-memory cache

    const doCall = useCallback(
        (urlPart?: string | number, options?: Options) => {
            setError(undefined);
            setIsLoading(true);
            const requestUrl = url + (urlPart ?? "");

            return axios
                .request<T>({
                    baseURL: requestUrl,
                    data: options?.dataToSend,
                    method: options?.method || "GET",
                    signal: options?.controller?.signal,
                })
                .then((response) => {
                    setData(response.data);
                    return response;
                })
                .catch((e) => {
                    if (options?.controller?.signal.aborted) {
                        console.debug(requestUrl, "canceled", e);
                    } else {
                        console.error(requestUrl, e);
                        setError(e);
                    }
                })
                .finally(() => setIsLoading(false));
        },
        [url]
    );

    const doGet = useCallback(
        (id?: string | number, options?: GetOptions) => {
            if (!options) options = {};
            options.controller = new AbortController();
            const requestUrl = url + (id ?? "");

            if (options?.cache && cache.current[requestUrl]) {
                setData(cache.current[requestUrl]);
            } else {
                doCall(id, options)
                    .then((r) => {
                        if (options?.cache && isAxiosResponse(r)) {
                            cache.current[requestUrl] = r.data;
                        }
                    })
                    .catch((e) => console.error("doGet failed", e));
            }
            return () => options.controller?.abort();
        },
        [url, doCall]
    );

    return useMemo(
        () => ({ isLoading, data, error, doGet, doCall }),
        [isLoading, data, error, doGet, doCall]
    );
};

export function isAxiosResponse<T = unknown>(
    value: unknown
): value is AxiosResponse<T> {
    return (
        typeof value === "object" &&
        value !== null &&
        "status" in value &&
        "data" in value
    );
}
