import { useBars } from '@entities/bars'
import { useOrders } from '@entities/order'
import { Grid } from '@mui/material'
import type { OrderState } from '@shared/api/schema'
import { TIMEFRAMES, type TimeFrameConfig } from '@shared/model/timeframes'
import { BarChart, DefaultDialog, type DefaultDialogProps } from '@shared/ui'
import dayjs, { Dayjs } from 'dayjs'
import { type LineData, type UTCTimestamp } from 'lightweight-charts'
import { LineSeries, Pane, PriceLine, type SeriesApiRef } from 'lightweight-charts-react-components'
import { useEffect, useMemo, useRef, useState } from 'react'

interface QuoteChartDialogProps extends Omit<DefaultDialogProps, 'dialogContent'> {
    symbol: string
}

function roundToInterval(mills: number, intervalSec: number, offsetHours: number = 0): UTCTimestamp {
    const offsetMills = offsetHours * 60 * 60 * 1000;
    // Сдвигаем шкалу назад -> округляем -> возвращаем шкалу вперед
    return (Math.floor((mills) / (intervalSec * 1000)) * intervalSec * 1000 + offsetMills) as UTCTimestamp;
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

    const { data: orders, update, remove } = useOrders(symbol)

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

                // console.clear()
                // if (data) {
                //    const bars = data['SMLT@MISX']
                //    const lastBar = bars[bars.length - 1]
                //    console.log('Last data: ' + dayjs(lastBar.mills).locale('ru').format("DD.MM.YYYY HH:mm:ss"));
                // }

                console.log('New data: ' + dayjs(quote.mills).locale('ru').format("DD.MM.YYYY HH:mm:ss"));
                const time = roundToInterval(quote.mills, timeFrameRef.current.intervalSec, timeFrameRef.current.offset ?? 0)
                // console.log('Rounded: ' + quote.mills + '/' + timeFrameRef.current.intervalSec + ' result: ' + dayjs(time).locale('ru').format("DD.MM.YYYY HH:mm:ss"));

                series.update({
                    time: time,
                    value: quote.last!
                });
            });

            source.addEventListener("order", (event) => {
                const order: OrderState = JSON.parse(event.data);
                if (order.status == 'ORDER_STATUS_CANCELED' || order.status == 'ORDER_STATUS_FAILED')
                    remove(order.orderId!)
                else
                    update(order)
            });

            return () => {
                console.log('close stream');
                source.close()
            }
        }
    }, [symbol, remove, update])

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
                                <LineSeries data={values} options={{ lineWidth: 1 }} ref={seriesRef} >
                                    {
                                        (orders ?? []).map(os =>
                                            <PriceLine
                                                key={os.orderId}
                                                price={os.order?.limitPrice ?? 0}
                                                options={{ color: "#FF0000" }}
                                            />)
                                    }
                                </LineSeries>
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