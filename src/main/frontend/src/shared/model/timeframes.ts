import type { TimeFrame } from "@shared/api/schema";

export type TimeFrameConfig = {
    label: string;
    value: TimeFrame;
    maxDays: number;
    intervalSec: number;
    offset?: number
};

export const TIMEFRAMES: TimeFrameConfig[] = [
    { label: "1m", value: "TIME_FRAME_M1", maxDays: 7, intervalSec: 60 },
    { label: "5m", value: "TIME_FRAME_M5", maxDays: 30, intervalSec: 5 * 60 },
    { label: "15m", value: "TIME_FRAME_M15", maxDays: 30, intervalSec: 15 * 60 },
    { label: "30m", value: "TIME_FRAME_M30", maxDays: 30, intervalSec: 30 * 60 },
    { label: "1h", value: "TIME_FRAME_H1", maxDays: 30, intervalSec: 60 * 60 },
    { label: "2h", value: "TIME_FRAME_H2", maxDays: 30, intervalSec: 2 * 60 * 60 },
    { label: "4h", value: "TIME_FRAME_H4", maxDays: 30, intervalSec: 4 * 60 * 60, offset: 1 },
    { label: "8h", value: "TIME_FRAME_H8", maxDays: 30, intervalSec: 8 * 60 * 60, offset: 5 },
    { label: "D", value: "TIME_FRAME_D", maxDays: 365, intervalSec: 23 * 60 * 10, offset: 1 },
    { label: "W", value: "TIME_FRAME_W", maxDays: 365 * 5, intervalSec: 7 * 24 * 60 * 60 },
    { label: "M", value: "TIME_FRAME_MN", maxDays: 365 * 5, intervalSec: 30 * 24 * 60 * 60 }, // грубо
    { label: "Q", value: "TIME_FRAME_QR", maxDays: 365 * 5, intervalSec: 3 * 30 * 24 * 60 * 60 } // грубо
];