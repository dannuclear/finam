import dayjs from "dayjs";
import {
    LineSeries,
    TickMarkType,
    createChart,
    type IChartApi,
    type ISeriesApi,
    type LineData,
    type Time,
    type TimeRangeChangeEventHandler
} from "lightweight-charts";
import { useEffect, useRef } from "react";

type ChartProps = {
    data: LineData[],
    lastPoint?: LineData,
    scrollHandler?: TimeRangeChangeEventHandler<Time>;
};

export const Chart = ({
    data,
    lastPoint,
    scrollHandler
}: ChartProps) => {
    const containerRef = useRef<HTMLDivElement | null>(null);
    const chartRef = useRef<IChartApi | null>(null);
    const seriesRef = useRef<ISeriesApi<"Line"> | null>(null);

    useEffect(() => {
        if (!containerRef.current) return;
        console.log('chart mount')
        const chart = createChart(containerRef.current, {
            layout: {
                background: {
                    color: "#ffffff"
                },
                textColor: "#333",
                attributionLogo: false
            },
            grid: {
                vertLines: { color: "#eee" },
                horzLines: { color: "#eee" }
            },
            timeScale: {
                timeVisible: true,
                tickMarkFormatter: (time: Time, tickMarkType: TickMarkType, locale: string) => {
                    let pattern = "DD.MM"
                    if (tickMarkType === TickMarkType.Time)
                        pattern = "HH:mm";
                    else if (tickMarkType === TickMarkType.DayOfMonth)
                        pattern = "DD.MM";
                    else if (tickMarkType === TickMarkType.Month)
                        pattern = "MMM YYYY";
                    else if (tickMarkType === TickMarkType.Year)
                        pattern = "YYYY";
                    else if (tickMarkType === TickMarkType.TimeWithSeconds)
                        pattern = "HH:mm:ss";

                    return dayjs.unix(time as number).locale(locale).format(pattern)
                }
            },
            localization: {
                timeFormatter: (time: Time) => {
                    return dayjs.unix(time as number).format("DD.MM.YYYY HH:mm");
                },
                dateFormat: "DD.MM.YYYY"
            }
        });

        const series = chart.addSeries(LineSeries, { lineWidth: 1 });

        chartRef.current = chart;
        seriesRef.current = series;

        series.setData(data);
        chart.timeScale().fitContent();

        if (scrollHandler)
            chart.timeScale().subscribeVisibleTimeRangeChange(scrollHandler);

        return () => {
            if (scrollHandler)
                chart.timeScale().unsubscribeVisibleTimeRangeChange(scrollHandler);
            chart.remove();
        }
    }, []);

    useEffect(() => {
        if (lastPoint && seriesRef.current) {
            seriesRef.current.update(lastPoint);
        }
    }, [lastPoint]);

    useEffect(() => {
        seriesRef.current?.setData(data);
        chartRef.current?.timeScale().fitContent();
    }, [data]);

    return <div
        ref={containerRef}
        style={{
            height: "500px"
        }} />;
};
