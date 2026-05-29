import { Grid } from '@mui/material'
import { TIMEFRAMES, type TimeFrameConfig } from '@shared/model/timeframes'
import { BarChart, DefaultDialog, type DefaultDialogProps } from '@shared/ui'
import dayjs, { Dayjs } from 'dayjs'
import { useBars } from '@entities/bars'
import { type LineData, type UTCTimestamp } from 'lightweight-charts'
import { LineSeries, Pane, type SeriesApiRef } from 'lightweight-charts-react-components'
import { useEffect, useMemo, useRef, useState } from 'react'

interface QuoteChartDialogProps extends Omit<DefaultDialogProps, 'dialogContent'> {
    symbol: string
}

function roundToInterval(mills: number, intervalSec: number): UTCTimestamp {
    return Math.floor(mills / (intervalSec * 1000)) * intervalSec * 1000 as UTCTimestamp;
}

const QuoteChartDialog = ({
    symbol,
    title = "Котировки",
    ...props }: QuoteChartDialogProps) => {

    const [timeFrame, setTimeFrame] = useState<TimeFrameConfig>(TIMEFRAMES[8]);
    const [startTime, setStartTime] = useState<Dayjs>(dayjs().subtract(timeFrame.maxDays, "day"))
    const [endTime, setEndTime] = useState<Dayjs>(dayjs())

    const seriesRef = useRef<SeriesApiRef<"Line"> | null>(null)
    const timeFrameRef = useRef<TimeFrameConfig>(timeFrame)

    const { data } = useBars({
        symbols: symbol ? [symbol] : [],
        timeFrame: timeFrame.value,
        startTime: startTime,
        endTime: endTime,
    })

    useEffect(() => {
        if (symbol != null) {
            console.log('subscribe');
            const source = new EventSource(`/api/v1/finam/assets/${symbol}/subcribe`)

            source.addEventListener("quote", (event) => {
                const quote = JSON.parse(event.data);
                if (!seriesRef.current) return;
                const series = seriesRef.current.api();
                if (!series) return;
                if (!quote.last) return;
                const time = roundToInterval(quote.mills, timeFrameRef.current.intervalSec)
                console.log(time)
                console.log(dayjs(time).locale('ru').format("DD.MM.YYYY HH:mm:ss"));
                console.log(quote.last)

                series.update({
                    time: time,
                    value: quote.last!
                });
            })

            return () => {
                console.log('close stream');
                source.close()
            }
        }
    }, [symbol])

    useEffect(() => {
        timeFrameRef.current = timeFrame;
    }, [timeFrame])

    const values: LineData[] = useMemo(() => {
        const firstSeries = data ? Object.values(data)[0] : undefined;

        return firstSeries?.map(bar => ({
            time: bar.mills as UTCTimestamp,
            value: bar.close ?? 0
        })) ?? [];
    }, [data])

    return (
        <DefaultDialog
            maxWidth="lg"
            fullWidth
            title={title}
            minHeight={600}
            dialogContent={
                <Grid container spacing={1}>
                    <Grid size={12}>
                        <BarChart
                            timeFrame={timeFrame}
                            onTimeFrameChange={(tf, start, end) => {
                                setTimeFrame(tf);
                                setStartTime(start);
                                setEndTime(end);
                            }}>
                            <Pane stretchFactor={2}>
                                <LineSeries data={values} options={{ lineWidth: 1 }} ref={seriesRef} />
                            </Pane>
                        </BarChart>
                    </Grid>
                </Grid>
            }
            {...props}
        />
    )
}

export default QuoteChartDialog