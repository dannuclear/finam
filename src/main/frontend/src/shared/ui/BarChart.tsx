import { TIMEFRAMES, type TimeFrameConfig } from "@shared/model/timeframes";
import TimeFrameSwitcher from "@widgets/chart/ui/time-frame-switcher";
import dayjs, { Dayjs } from "dayjs";
import { CrosshairMode, TickMarkType, type ChartOptions, type DeepPartial, type Time } from "lightweight-charts";
import { Chart, type ChartProps } from "lightweight-charts-react-components";
import { type PropsWithChildren } from "react";

const formatMap: Record<TickMarkType, string> = {
    [TickMarkType.Time]: "HH:mm",
    [TickMarkType.TimeWithSeconds]: "HH:mm:ss",
    [TickMarkType.DayOfMonth]: "DD.MM",
    [TickMarkType.Month]: "MMM YYYY",
    [TickMarkType.Year]: "YYYY",
};

const formatTime = (time: Time, pattern: string, locale?: string) =>
    dayjs.unix(time as number).locale(locale || "ru").format(pattern);


const chartOptions: DeepPartial<ChartOptions> = {
    layout: {
        background: { color: "#ffffff" },
        textColor: "#333",
        attributionLogo: false
    },
    grid: {
        vertLines: { color: "#eee" },
        horzLines: { color: "#eee" }
    },
    timeScale: {
        timeVisible: true,
        tickMarkFormatter: (time: Time, type: TickMarkType, locale: string) => {
            const pattern = formatMap[type] ?? "DD.MM";
            return formatTime(time, pattern, locale);
        }
    },
    localization: {
        timeFormatter: (time: Time) =>
            formatTime(time, "DD.MM.YYYY HH:mm"),
        dateFormat: "DD.MM.YYYY"
    },
    crosshair: {
        mode: CrosshairMode.Magnet
    }
};

type BarChartProps = PropsWithChildren<{
    onTimeFrameChange?: (tf: TimeFrameConfig, start: Dayjs, end: Dayjs) => void;
    timeFrame: TimeFrameConfig,
    showTimeframes?: boolean;
    legend?: React.ReactNode,
    isLoading?: boolean
} & Omit<ChartProps, "options">>;

const BarChart = ({
    children,
    onTimeFrameChange,
    timeFrame,
    showTimeframes = true,
    legend,
    isLoading,
    ...props
}: BarChartProps) => {
    const setTimeFrameWithInterval = (tfc: TimeFrameConfig) => {
        const now = dayjs().startOf('minute');
        const start = now.subtract(tfc.maxDays, "day");
        onTimeFrameChange?.(tfc, start, now);
    };

    return (
        <>
            {showTimeframes && (
                <TimeFrameSwitcher
                    timeFrame={timeFrame}
                    options={TIMEFRAMES}
                    onChange={tf => setTimeFrameWithInterval(tf)}
                    disabled={isLoading}
                />
            )}

            <Chart
                {...props}
                options={chartOptions}
                containerProps={{
                    style: { height: "500px", flexGrow: 1, position: "relative" },
                    ...props.containerProps
                }}
            >

                {isLoading && (
                    <div
                        style={{
                            position: "absolute",
                            inset: 0,
                            zIndex: 10,
                            opacity: 0.1,
                            backgroundColor: "gray"
                        }}
                    />
                )}

                {children}
            </Chart>

            {legend && (<div style={{ marginTop: 8, textAlign: "center" }}>
                {legend}
            </div>
            )}
        </>
    );
};

export default BarChart;