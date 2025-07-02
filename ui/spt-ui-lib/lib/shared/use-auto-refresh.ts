import { useEffect, type EffectCallback } from "react";

const useAutoRefresh = (
    delayInMs: number,
    effect: EffectCallback,
    deps?: readonly unknown[]
) => {
    useEffect(() => effect(), deps);

    useEffect(() => {
        const intervalId = setInterval(effect, delayInMs);
        return () => clearInterval(intervalId);
    }, deps);
};

export default useAutoRefresh;
