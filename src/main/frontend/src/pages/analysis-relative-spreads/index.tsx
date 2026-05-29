import { useAnalysisRelativeSpreads } from "@entities/analysis";
import { Grid } from "@mui/material";
import { DatePicker } from "@mui/x-date-pickers/DatePicker";
import { DateTimePicker } from "@mui/x-date-pickers/DateTimePicker";
import type { Analysis } from "@shared/api/schema";
import { TIMEFRAMES, type TimeFrameConfig } from "@shared/model/timeframes";
import { BarChart } from "@shared/ui";
import { AnalysisEditableSelect } from "@widgets/analysis";
import type { LegendItem } from "@widgets/chart/ui/legend";
import Legend from "@widgets/chart/ui/legend";
import type { Dayjs } from "dayjs";
import dayjs from "dayjs";
import { type LineData, type LineWidth, type UTCTimestamp } from "lightweight-charts";
import { LineSeries, Pane, TimeScale, TimeScaleFitContentTrigger } from "lightweight-charts-react-components";
import { useMemo, useState } from "react";

const Index = () => {
    const [timeFrame, setTimeFrame] = useState<TimeFrameConfig>(TIMEFRAMES[7]);
    const [startTime, setStartTime] = useState<Dayjs>(dayjs().subtract(timeFrame.maxDays, "day"))
    const [endTime, setEndTime] = useState<Dayjs>(dayjs())
    // const [averageTimeFrame, setAverageTimeFrame] = useState<TimeFrameConfig>(TIMEFRAMES[8]);
    const [averageStartTime, setAverageStartTime] = useState<Dayjs>(dayjs().subtract(TIMEFRAMES[8].maxDays/2-40, "day"))
    const [averageEndTime, setAverageEndTime] = useState<Dayjs>(dayjs())
    const [enabledBars, setEnabledBars] = useState<Record<string, boolean>>({})

    const [analysis, setAnalysis] = useState<Analysis | null>(null)

    const { data, isFetching } = useAnalysisRelativeSpreads({
        analysisId: analysis?.id,
        timeFrame: timeFrame.value,
        startTime: startTime,
        endTime: endTime,
        averageTimeFrame: TIMEFRAMES[8].value,
        averageStartTime: averageStartTime,
        averageEndTime: averageEndTime
    })

    const toggleEnabled = (item: LegendItem) => {
        setEnabledBars(prev => ({
            ...prev,
            [item.id]: !item.enabled
        }))
    }

    const { series, legendOptions } = useMemo(() => {
        const seriesResult = [];
        const legendResult: LegendItem[] = [];

        for (const item of data ?? []) {
            const id = item.id as string;

            const enabled = enabledBars[id] ?? item.enabled ?? true;

            const preparedItem = {
                ...item,
                enabled,
                bars: (item.bars ?? [])
                    .filter((bar) =>
                        bar.close != null &&
                        bar.mills != null
                    )
                    .map<LineData>((bar) => ({
                        time: bar.mills as UTCTimestamp,
                        value: bar.close as number,
                        customValues: {
                            realPrice: bar.close as number
                        }
                    })),
            };

            seriesResult.push(preparedItem);

            legendResult.push({
                id,
                label: item.name ?? "no-name",
                color: item.lineColor ?? "rgba(0, 0, 0, 0)",
                enabled: enabled
            });
        }

        return {
            series: seriesResult,
            legendOptions: legendResult,
        };
    }, [data, enabledBars]);

    return (
        <Grid container spacing={1} sx={{ pt: 1 }}>
            <Grid size={{ xs: 6, lg: 2 }}>
                <DateTimePicker label="Период данных с" value={startTime} onChange={val => val && setStartTime(val)} />
            </Grid>
            <Grid size={{ xs: 6, lg: 2 }}>
                <DateTimePicker label="Период данных по" value={endTime} onChange={val => val && setEndTime(val)} />
            </Grid>
            <Grid size={{ xs: 12, lg: 8 }}>
                <AnalysisEditableSelect
                    value={analysis}
                    onChange={(_, value) => setAnalysis(value)} />
            </Grid>
            <Grid size={{ xs: 6, lg: 2 }}>
                <DatePicker label="Средняя дневная с" value={averageStartTime} onChange={val => val && setAverageStartTime(val)} />
            </Grid>
            <Grid size={{ xs: 6, lg: 2 }}>
                <DatePicker label="Средняя дневная по" value={averageEndTime} onChange={val => val && setAverageEndTime(val)} />
            </Grid>

            <Grid size={12} >
                <BarChart
                    timeFrame={timeFrame}
                    onTimeFrameChange={(tf, start, end) => {
                        setTimeFrame(tf);
                        setStartTime(start);
                        setEndTime(end);
                    }}
                    legend={
                        <Legend
                            options={legendOptions}
                            onOptionClick={toggleEnabled}
                        />}
                    isLoading={isFetching}
                >
                    <Pane>
                        {series.filter(s => s.enabled).map(s =>
                            <LineSeries
                                key={`${s.id}-bars`}
                                data={s.bars ?? []}
                                options={{
                                    lineWidth: (s.lineWidth ?? 1) as LineWidth,
                                    color: s.lineColor ?? "#009F00",
                                    lastValueVisible: false,
                                    priceLineVisible: false
                                }}
                                alwaysReplaceData
                            />
                        )}
                    </Pane>
                    <TimeScale>
                        <TimeScaleFitContentTrigger deps={[data]} />
                    </TimeScale>
                </BarChart>
            </Grid>
        </Grid>
    )
}

export { Index as Component };

