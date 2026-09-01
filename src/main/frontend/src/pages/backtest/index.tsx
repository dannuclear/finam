import { StrategySelect } from "@entities/strategy"
import { AssetSelect } from "@features/asset/ui/asset-select"
import { OptimizerParametersDialog, StrategyParametersDialog, useOptimize, useRunBacktest } from "@features/backtest"
import { Button, Table, TableBody, TableCell, TableHead, TableRow, TextField } from "@mui/material"
import Grid from "@mui/material/Grid"
import { LineChart } from '@mui/x-charts/LineChart'
import { DateTimePicker } from "@mui/x-date-pickers/DateTimePicker"
import { fetchClient } from "@shared/api/instance"
import type { Asset, OptimizerStrategyParameter, Strategy, StrategyParameterWithValue } from "@shared/api/schema"
import { TIMEFRAMES, type TimeFrameConfig } from "@shared/model/timeframes"
import BarChart from "@shared/ui/BarChart"
import type { Dayjs } from "dayjs"
import dayjs from "dayjs"
import type { LineData, LineWidth, SeriesMarker, UTCTimestamp } from "lightweight-charts"
import { LineSeries, Markers, Pane, TimeScale, TimeScaleFitContentTrigger } from "lightweight-charts-react-components"
import { useEffect, useMemo, useState } from "react"
import type { FieldValues } from "react-hook-form"

const BacktestPage = () => {
    const [strategy, setStrategy] = useState<Strategy | null>(null)
    const [assets, setAssets] = useState<Asset[]>([])
    const [timeFrame, setTimeFrame] = useState<TimeFrameConfig>(TIMEFRAMES[7]);
    const [startTime, setStartTime] = useState<Dayjs>(dayjs().subtract(timeFrame.maxDays, "day"))
    const [endTime, setEndTime] = useState<Dayjs>(dayjs())
    const [transactionCost, setTransactionCost] = useState<number>(0.1)

    const [distributionMa, setDistributionMa] = useState<number>(30)

    const [parameters, setParameters] = useState<StrategyParameterWithValue[]>()
    const [optimizerParameters, setOptimizerParameters] = useState<OptimizerStrategyParameter[]>()

    const [parametersOpen, setParametersOpen] = useState<boolean>(false)
    const [optimizerParametersOpen, setOptimizerParametersOpen] = useState<boolean>(false)

    const { data, mutate: run, isPending } = useRunBacktest()
    const { mutate: optimize, isPending: isPendingOptimize } = useOptimize()

    useEffect(() => {
        if (strategy && strategy?.id) {
            fetchClient.GET("/api/v1/strategies/{id}/parameters", {
                params: {
                    path: {
                        id: strategy?.id ?? 0
                    }
                }
            }).then((response) => {
                setParameters(response?.data?.map(p => p as StrategyParameterWithValue))
                setOptimizerParameters(response?.data?.map(p => ({ id: p.id, name: p.name })))
            })
        }
    }, [strategy]);

    const runInternal = () => {
        setParametersOpen(true)
    }

    const optimizeInternal = () => {
        setOptimizerParametersOpen(true)
    }

    const onOptimizerSuccess = (formData: FieldValues) => {
        const result = Object.entries(formData).map(([id, value]) => ({
            id,
            ...value,
        }))

        if (strategy)
            optimize({
                params: {
                    path: {
                        id: Number(strategy?.id)
                    },
                    query: {
                        assetId: assets.map(asset => asset.symbol ?? "")[0],
                        timeFrame: timeFrame.value,
                        startTime: startTime.format(),
                        endTime: endTime.format(),
                        transactionCost: transactionCost
                    }
                },
                body: result
            }, {
                onSuccess: (data) => {
                    setOptimizerParametersOpen(false)
                    setOptimizerParameters(old => old?.map(p => ({ id: p.id, name: p.name, ...formData?.[p.id!] })))
                    setParameters(old => old?.map(p => ({ ...p, value: data?.parameters?.[p.id!] })))
                    setParametersOpen(true)
                }
            })
    }

    const onParametersSuccess = (data: FieldValues) => {
        if (strategy)
            run({
                params: {
                    path: {
                        id: Number(strategy?.id)
                    },
                    query: {
                        assets: assets.map(asset => asset.symbol ?? ""),
                        timeFrame: timeFrame.value,
                        startTime: startTime.format(),
                        endTime: endTime.format(),
                        distributionMa: distributionMa,
                        transactionCost: transactionCost,
                    },
                },
                body: data
            }, {
                onSuccess: () => {
                    setDistributionMa(Number(data?.['slowMa'] ?? 0))
                    setParameters(old => old?.map(p => ({ ...p, value: data[p.id!] })))
                    setParametersOpen(false)
                }
            })
    }

    const { series } = useMemo(() => {
        const seriesResult = [];

        for (const item of data?.series ?? []) {
            const preparedItem = {
                ...item,
                enabled: true,
                bars: (item.values ?? [])
                    .filter((bar) =>
                        bar.close != null &&
                        bar.seconds != null
                    )
                    .map<LineData>((bar) => ({
                        time: bar.seconds as UTCTimestamp,
                        value: bar.close as number
                    })),
                trades: (item.trades ?? [])
                    .map<SeriesMarker<UTCTimestamp>>(signal => ({
                        time: signal.seconds as UTCTimestamp,
                        position: signal.action === "BUY" ? "belowBar" : "aboveBar",
                        shape: signal.action === "BUY" ? "arrowUp" : "arrowDown",
                        color: signal.action === "BUY" ? "#005200" : "#ff0000",
                        text: '' + signal.price
                    }))
            };

            seriesResult.push(preparedItem);

            for (const ind of item.indicators ?? []) {
                const preparedItem = {
                    ...ind,
                    enabled: true,
                    bars: (ind.values ?? [])
                        .filter((val) =>
                            val.value != null &&
                            val.seconds != null
                        )
                        .map<LineData>((bar) => ({
                            time: bar.seconds as UTCTimestamp,
                            value: bar.value as number
                        })),
                    trades: []
                };

                seriesResult.push(preparedItem);
            }
        }

        return {
            series: seriesResult
        };
    }, [data]);

    return (<>
        <Grid container spacing={1} sx={{ pt: 1 }}>
            <Grid size={12}>
                <StrategySelect
                    value={strategy}
                    onChange={(_, val) => setStrategy(val)} />
            </Grid>
            <Grid size={12}>
                <AssetSelect value={assets} onChange={(_, g) => setAssets(g)} multiple />
            </Grid>
            <Grid size={{ xs: 6, lg: 2 }}>
                <DateTimePicker label="Период с" value={startTime} onChange={val => val && setStartTime(val)} />
            </Grid>
            <Grid size={{ xs: 6, lg: 2 }}>
                <DateTimePicker label="Период по" value={endTime} onChange={val => val && setEndTime(val)} />
            </Grid>
            <Grid size={{ xs: 6, lg: 1 }}>
                <TextField label="Комиссия %" value={transactionCost} onChange={e => setTransactionCost(Number(e.target.value))} />
            </Grid>
            <Grid size={{ xs: 3, lg: 1 }}>
                <TextField label="Средняя распределения" value={distributionMa} onChange={e => setDistributionMa(Number(e.target.value))} />
            </Grid>
            {/* <Grid size={12}></Grid> */}
            <Grid size={{ xs: 3, lg: 6 }} container spacing={1} alignContent="center">
                <Button color="success" onClick={runInternal} loading={isPending}>Запустить</Button>
                <Button color="success" onClick={optimizeInternal} loading={isPendingOptimize}>Оптимизация</Button>
            </Grid>
            <Grid size={6} >
                {data?.series?.map(series =>
                    <LineChart
                        key={series.id}
                        dataset={series.normalDistribution}
                        // Настройка оси X (Цены)
                        xAxis={[
                            {
                                dataKey: 'x',
                                valueFormatter: (value: number) => value.toFixed(2), // Округление цен под графиком
                            },
                        ]}
                        // Настройка оси Y (Плотность вероятности)
                        yAxis={[
                            {
                                label: 'Плотность',
                            },
                        ]}
                        // Настройка самой линии распределения
                        series={[
                            {
                                dataKey: 'y',
                                color: '#1976d2',
                                showMark: false,
                                curve: 'natural',  // Сглаживание линии для идеального колокола
                                area: true,        // Заливка под графиком для наглядности плотности
                            },
                        ]}
                        height={150}
                        margin={{
                            top: 0,     // Минимальный отступ сверху, чтобы верхушка колокола не резалась
                            bottom: 0,  // Отступ снизу для подписей оси X
                            left: 0,    // Отступ слева для подписей оси Y
                            right: 0    // Минимальный отступ справа
                        }}
                    />
                )}

            </Grid>
            <Grid size={4}>
                <Table sx={{ minWidth: 100 }}>
                    <TableHead>
                        <TableRow>
                            <TableCell>Показатель</TableCell>
                            <TableCell align="right">Значение</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {data?.series?.map((row) => (
                            Object.entries(row?.statistics ?? {}).map(([name, value]) => (
                                <TableRow key={row.name + name}>
                                    <TableCell>{name}</TableCell>
                                    <TableCell align="right">{String(value)}</TableCell>
                                </TableRow>))
                        ))}
                    </TableBody>
                </Table>
            </Grid>

            <Grid size={12} >
                <BarChart
                    timeFrame={timeFrame}
                    onTimeFrameChange={(tf, start, end) => {
                        setTimeFrame(tf);
                        setStartTime(start);
                        setEndTime(end);
                    }}
                    isLoading={isPending}
                >
                    <Pane>
                        {series.filter(s => s.enabled).map(s =>
                            <LineSeries
                                key={`${s.name}-bars`}
                                data={s.bars ?? []}
                                options={{
                                    lineWidth: (s.lineWidth ?? 1) as LineWidth,
                                    color: s.lineColor ?? "#000000",
                                    lastValueVisible: false,
                                    priceLineVisible: false,
                                    // crosshairMarkerVisible: false
                                }}
                                alwaysReplaceData
                            >
                                <Markers
                                    key={`${s.id}-${timeFrame.value}-markers`}
                                    markers={s.trades ?? []} />
                            </LineSeries>
                        )}
                    </Pane>
                    <TimeScale>
                        <TimeScaleFitContentTrigger deps={[data]} />
                    </TimeScale>
                </BarChart>
            </Grid>
        </Grid>

        <StrategyParametersDialog
            open={parametersOpen}
            pending={isPending}
            onSuccess={onParametersSuccess}
            onCancel={() => setParametersOpen(false)}
            parameters={parameters} />

        <OptimizerParametersDialog
            open={optimizerParametersOpen}
            pending={isPendingOptimize}
            onSuccess={onOptimizerSuccess}
            onCancel={() => setOptimizerParametersOpen(false)}
            parameters={optimizerParameters} />
    </>)
}

export { BacktestPage as Component }

