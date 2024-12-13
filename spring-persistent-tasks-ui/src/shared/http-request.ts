import axios from "axios";
import { useState } from "react";

export interface ServerObject<T> {
    isLoading: boolean,
    data: T | undefined,
    error: any,
    doGet(id?: string): AbortController
}
export const useServerObject = <T>(url: string, startValue?: T): ServerObject<T> => {
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [data, setData] = useState<T | undefined>(startValue);
    const [error, steError] = useState<any>(undefined);

    const doGet = (id?: string): AbortController => {
        const controller = new AbortController();
        const requestUrl = url + (id ?? "");
        steError(undefined);
        setIsLoading(true);
        axios
            .get(requestUrl, {
                signal: controller.signal
            })
            .then(response => setData(response.data))
            .catch(e => {
                console.error(requestUrl, e)
                steError(error);
            })
            .finally(() => setIsLoading(false));
        return controller;
    }
    return {isLoading, data, error, doGet};
}