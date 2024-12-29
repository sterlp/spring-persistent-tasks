import axios from "axios";
import { useMemo, useState } from "react";

export interface ServerObject<T> {
    isLoading: boolean,
    data: T | undefined,
    error: any,
    doGet(id?: string): void
    doCall(urlPart?: string, method?: HttpMethod, dataToSend?: unknown): Promise<void>
}
export type HttpMethod = "GET" | "DELETE" | "POST" | "PUT" | "HEADER";

export const useServerObject = <T>(url: string, startValue?: T): ServerObject<T> => {
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [data, setData] = useState<T | undefined>(startValue);
    const [error, steError] = useState<any>(undefined);

    const doGet = (id?: string) => {
        const controller = new AbortController();
        const requestUrl = url + (id ?? "");
        steError(undefined);
        setIsLoading(true);
        axios
            .get(requestUrl, {
                signal: controller.signal,
            })
            .then((response) => setData(response.data as T))
            .catch((e) => {
                if (controller.signal.aborted) {
                //if (controller.signal.aborted) {
                    console.debug(requestUrl, "canceled", e);
                } else {
                    console.error(requestUrl, e);
                    steError(e);
                }
            })
            .finally(() => setIsLoading(false));
        return () => controller.abort();
    }

    const doCall = (urlPart?: string, method: HttpMethod = "GET", dataToSend?: unknown) => {
        return axios.request({
            baseURL: url + (urlPart ?? ""),
            data: dataToSend,
            method: method
        })
        .then((response) => setData(response.data as T))
        .catch(e => steError(e))
        .finally(() => setIsLoading(false));
    }
    return useMemo(
        () => ({ isLoading, data, error, doGet, doCall }),
        [isLoading, data, error, doCall]
    );
}