import { useEffect, useState } from "react"

function App() {
    const [schedulers, setSchedulers] =  useState<string[] | undefined>(undefined);

    useEffect(() => {
        fetchSchedulers();
    }, []);
    
    const fetchSchedulers = async () => {
        const response = await fetch('/spring-tasks-api/schedulers');
        if (response.ok) {
            setSchedulers(await response.json() as string[]);
        }
    }

    return (
        <>
            {schedulers?.map(i => <div>{i}</div>)}
        </>
    )
}

export default App
