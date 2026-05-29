import { Box, Grid, Switch, TextField } from '@mui/material'
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import { TIMEFRAMES, type TimeFrameConfig } from '@shared/model/timeframes'
import { BarChart, DefaultDialog, type DefaultDialogProps } from '@shared/ui'
import Legend, { type LegendItem } from '@widgets/chart/ui/legend'
import dayjs, { Dayjs } from 'dayjs'
import { useTradeGroupReferenceAssetsBars } from '@entities/trade-group'
import { type ISeriesApi, type LineData, type LineWidth, type MouseEventParams, type UTCTimestamp } from 'lightweight-charts'
import {
    LineSeries,
    Pane,
    TimeScale,
    TimeScaleFitContentTrigger
} from "lightweight-charts-react-components"
import { useCallback, useMemo, useRef, useState, type FC, type ReactNode } from 'react'

type InfoProps = {
    children: ReactNode
}

const Info: FC<InfoProps> = ({ children }) => {
    return (<Box
        sx={{
            position: "absolute",
            top: 0,
            left: 0,
            padding: 2,
            zIndex: 10
        }}>
        {children}
    </Box>)
}

interface TradeGroupQuotesDialogProps extends Omit<DefaultDialogProps, 'dialogContent'> {
    tradedGroupId: number | null
}

const TradeGroupQuotesDialog = ({
    tradedGroupId,
    title = "Котировки",
    ...props }: TradeGroupQuotesDialogProps) => {

    const seriesRefs = useRef<Map<ISeriesApi<"Line">, string>>(new Map<ISeriesApi<"Line">, string>)
    const [info, setInfo] = useState<string>("")

    const [timeFrame, setTimeFrame] = useState<TimeFrameConfig>(TIMEFRAMES[7]);
    const [startTime, setStartTime] = useState<Dayjs>(dayjs().subtract(timeFrame.maxDays, "day"))
    const [endTime, setEndTime] = useState<Dayjs>(dayjs())
    const [smaBarCount, setSmaBarCount] = useState<number>(30)
    const [showSma, setShowSma] = useState<boolean>(false)
    const [enabledBars, setEnabledBars] = useState<Record<string, boolean>>({})

    const { data, isFetching } = useTradeGroupReferenceAssetsBars({
        id: tradedGroupId,
        timeFrame: timeFrame?.value,
        startTime,
        endTime,
        smaBarCount
    });

    const { series, legendOptions } = useMemo(() => {
        const seriesResult = [];
        const legendResult: LegendItem[] = [];

        const lastPrices = (data ?? [])
            .filter(item => enabledBars[item.id as string] ?? item.enabled ?? true)
            .map(item => (item?.bars?.at(-1)?.close ?? 0) + (item.priceOffset ?? 0.0))
            .filter((v): v is number => v !== undefined && v !== null);

        const avgOffset =
            lastPrices.length > 0
                ? lastPrices.reduce((sum, val) => sum + val, 0) / lastPrices.length
                : 0;
        console.log(lastPrices);
        

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
                        value: bar.close as number + (item.priceOffset ?? 0.0) - avgOffset,
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

    const toggleEnabled = (item: LegendItem) => {
        setEnabledBars(prev => ({
            ...prev,
            [item.id]: !item.enabled
        }))
    }

    const onCrosshairMove = useCallback((param: MouseEventParams) => {
        const lines: string[] = []
        param.seriesData.forEach((value, key) => {
            if (value.customValues == undefined || !("realPrice" in value.customValues))
                return
            const seriesRef = key as ISeriesApi<"Line">
            lines.push(`${seriesRefs.current.get(seriesRef)}: ${value.customValues.realPrice}`)
        })

        const formatted = lines
            .reduce((rows: string[], line, index) => {
                if (index % 3 === 0) {
                    rows.push(line);
                } else {
                    rows[rows.length - 1] += ` \t ${line}`;
                }
                return rows;
            }, [])
            .join("\n");

        setInfo(formatted);
    }, [])

    return (
        <DefaultDialog
            maxWidth="lg"
            fullWidth
            title={title}
            dialogContent={
                <Grid container spacing={1}>
                    <Grid size={{ xs: 6, lg: 3 }}>
                        <DateTimePicker value={startTime} onChange={val => val && setStartTime(val)} />
                    </Grid>
                    <Grid size={{ xs: 6, lg: 3 }}>
                        <DateTimePicker value={endTime} onChange={val => val && setEndTime(val)} />
                    </Grid>
                    <Grid size={{ xs: 2, lg: 1 }}>
                        <TextField label="SMA" value={smaBarCount} onChange={val => setSmaBarCount(Number(val.target.value))} />
                    </Grid>
                    <Grid size={{ xs: 2, lg: 1 }}>
                        <Switch value={showSma} onChange={e => setShowSma(e.target.checked)} />
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
                            onCrosshairMove={onCrosshairMove}
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
                                        ref={(ref) => {
                                            const apiRef = ref?.api()
                                            if (s.id && apiRef)
                                                seriesRefs.current.set(apiRef, s.name ?? "")
                                        }} />
                                )}
                            </Pane>
                            <TimeScale>
                                <TimeScaleFitContentTrigger deps={[data]} />
                            </TimeScale>
                            <Info>
                                <small style={{ whiteSpace: "pre-line" }}>{info}</small>
                            </Info>
                        </BarChart>
                    </Grid>
                </Grid>
            }
            {...props}
        />
    )
}

export default TradeGroupQuotesDialog