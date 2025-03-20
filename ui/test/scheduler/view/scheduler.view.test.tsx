import SchedulerStatusView from "@src/scheduler/views/scheduler.view";
import { render, screen } from "@testing-library/react";
import { expect, it } from "vitest";

describe("SchedulerStatusView Tests", () => {
    beforeAll(() => {
        const ResizeObserverMock = vi.fn(() => ({
            observe: vi.fn(),
            unobserve: vi.fn(),
            disconnect: vi.fn(),
        }));

        // Stub the global ResizeObserver
        vi.stubGlobal("ResizeObserver", ResizeObserverMock);
    });

    it("SchedulerStatusView happy", () => {
        // GIVEN
        const s = {
            id: "schedulerB",
            tasksSlotCount: 7,
            runningTasks: 1,
            systemLoadAverage: 15,
            maxHeap: 50 * 1024 * 1024,
            usedHeap: 150 * 1024 * 1024,
            lastPing: "2025-03-20T18:56:48.761835+01:00",
        };

        // WHEN
        render(<SchedulerStatusView scheduler={s} />);

        expect(screen.getByText("schedulerB")).toBeInTheDocument();

        expect(screen.getByText("Threads")).toBeInTheDocument();
        expect(screen.getByText("CPU")).toBeInTheDocument();

        expect(screen.getByText("150MB")).toBeInTheDocument();
        expect(screen.getByText("15%")).toBeInTheDocument();
    });
});
