import { AssetSelect } from "@features/asset/ui/asset-select"
import { useTradingSpreadsData, useTradingSpreadsStart, useTradingSpreadsStatus, useTradingSpreadsStop, useTradingSpreadsSymbols } from "@features/trading-spreads"
import { Button, FormControlLabel, Switch, TextField } from "@mui/material"
import Grid from "@mui/material/Grid"
import type { Asset } from "@shared/api/schema"
import { TIMEFRAMES, type TimeFrameConfig } from "@shared/model/timeframes"
import { BarChart } from "@shared/ui"
import Legend from "@widgets/chart/ui/legend"
import { LineStyle, type LineData, type Time } from "lightweight-charts"
import { LineSeries, Pane, type SeriesApiRef } from "lightweight-charts-react-components"
import { useEffect, useMemo, useRef, useState } from "react"

const SERIES_COLORS = [
    "#1565C0", // blue
    "#C62828", // red
    "#2E7D32", // green
    "#6A1B9A", // purple
    "#E65100", // orange
    "#00838F", // cyan
    "#AD1457", // pink
    "#4E342E", // brown
    "#283593", // indigo
    "#558B2F", // lime-green
    "#4527A0", // deep-purple
    "#00695C", // teal
];

const defaultAssets: Asset[] = [
    { name: "ОФЗ 26248", symbol: "SU26248RMFS3@MISX" },
    { name: "ОФЗ 26238", symbol: "SU26238RMFS4@MISX" },
    { name: "ОФЗ 26230", symbol: "SU26230RMFS1@MISX" },
    { name: "ОФЗ 26254", symbol: "SU26254RMFS1@MISX" },
    { name: "ОФЗ 26253", symbol: "SU26253RMFS3@MISX" },
    { name: "ОФЗ 26247", symbol: "SU26247RMFS5@MISX" },
    { name: "ОФЗ 26245", symbol: "SU26245RMFS9@MISX" },
]

const generateColor = (index: number): string => {
    return SERIES_COLORS[index % SERIES_COLORS.length];
};

const AssetListPage = () => {
    const [timeFrame, setTimeFrame] = useState<TimeFrameConfig>(TIMEFRAMES[0]);

    const seriesRefs = useRef(
        new Map<string, SeriesApiRef<"Line">>()
    )
    const { data } = useTradingSpreadsSymbols()
    const { data: predata } = useTradingSpreadsData()

    const { mutate: start, isPending: isStartPending } = useTradingSpreadsStart()
    const { mutate: stop, isPending: isStopPending } = useTradingSpreadsStop()
    const { data: isRunning } = useTradingSpreadsStatus()

    const [assets, setAssets] = useState<Asset[]>(defaultAssets)
    const [fastMaCount, setFastMaCount] = useState<string>("5")
    const [daysCount, setDaysCount] = useState<string>("10")
    const [spread, setSpread] = useState<string>("0.25")
    const [showPrice, setShowPrice] = useState<boolean>(false)

    const startInternal = () => {
        if (!assets) {
            return;
        }

        start({
            params: {
                query: {
                    assets: assets?.map(a => a.symbol ?? ""),
                    fastMaCount: Number(fastMaCount),
                    daysCount: Number(daysCount),
                    spread: Number(spread)
                }
            }
        })
    }

    useEffect(() => {
        if (!isRunning) {
            return
        }

        const source = new EventSource(`/api/v1/spreads/subcribe`)

        source.addEventListener("spread-trader", (event) => {
            const quotes = JSON.parse(event.data) as Record<
                string,
                {
                    timestamp: string;
                    value: number;
                    seconds: number;
                }
            >;

            Object.entries(quotes).forEach(([symbol, quote]) => {
                const ref = seriesRefs.current.get(symbol);

                if (!ref) {
                    return;
                }

                const series = ref.api();

                if (!series) {
                    return;
                }

                series.update({
                    time: quote.seconds as Time,
                    value: quote.value
                });
            });
        })

        return () => {
            source.close()
        }

    }, [isRunning])

    const initialData = useMemo(() => {
        const map = new Map<string, { data: LineData[], lineColor: string }>();

        if (!predata) {
            return map;
        }

        Object.entries(predata).forEach(([_symbol, seriesList], index) => {
            map.set(
                _symbol,
                {
                    data: seriesList?.values?.map((p) => ({
                        time: p.seconds as Time,
                        value: p.value ?? 0,
                    })) ?? [],
                    lineColor: generateColor(index)
                }
            )
        });

        return map;
    }, [predata]);

    const legendOptions = assets?.map(item => {
        const id = item.symbol ?? "";

        return {
            id,
            label: item.name ?? "no-name",
            color: initialData?.get(id)?.lineColor ?? "rgba(0, 0, 0, 0)",
            enabled: true
        };
    }) ?? [];

    return (
        <Grid container spacing={1}>
            <Grid size={12}>
                <AssetSelect
                    value={assets}
                    onChange={(_, v) => (setAssets(v), console.log(v))}
                    multiple />
            </Grid>
            <Grid size={1}>
                <TextField
                    label="Быстрая средняя"
                    value={fastMaCount}
                    onChange={e => setFastMaCount(e.target.value)} />
            </Grid>
            <Grid size={1}>
                <TextField
                    label="Дней средней"
                    value={daysCount}
                    onChange={e => setDaysCount(e.target.value)} />
            </Grid>
            <Grid size={1}>
                <TextField
                    label="Спред"
                    value={spread}
                    onChange={e => setSpread(e.target.value)} />
            </Grid>
            <Grid size={1} textAlign="center">
                <FormControlLabel control={<Switch
                    title="Цены"
                    value={showPrice}
                    onChange={e => setShowPrice(e.target.checked)} />} label="Цены" />

            </Grid>
            <Grid size={6} container spacing={1}>
                <Button onClick={() => startInternal()} disabled={isRunning} loading={isStartPending}>Start</Button>
                <Button onClick={() => stop({})} disabled={!isRunning} loading={isStopPending}>Stop</Button>
            </Grid>
            <Grid size={12}>
                <BarChart
                    timeFrame={timeFrame}
                    onTimeFrameChange={(tf) => {
                        setTimeFrame(tf);
                    }}
                    legend={
                        <Legend
                            options={legendOptions}
                        />}
                    showTimeframes={false}
                >
                    <Pane stretchFactor={2}>
                        {showPrice && data?.map(symbol =>
                            <LineSeries
                                key={symbol}
                                data={[]}
                                options={{
                                    lineWidth: 1,
                                    color: initialData?.get(symbol)?.lineColor ?? "#9ccaff",
                                    priceLineVisible: false,
                                    lastValueVisible: false
                                }}
                                ref={(ref) => {
                                    if (ref) {
                                        seriesRefs.current.set(symbol, ref)
                                    } else {
                                        seriesRefs.current.delete(symbol)
                                    }
                                }} >

                            </LineSeries>
                        )}

                        {data?.map(symbol =>
                            <LineSeries
                                key={`${symbol}-fast-ma`}
                                data={initialData?.get(symbol)?.data ?? []}
                                options={{
                                    lineWidth: 2,
                                    lineStyle: LineStyle.Solid,
                                    color: initialData?.get(symbol)?.lineColor ?? "#9ccaff",
                                    priceLineVisible: false,
                                    lastValueVisible: false,
                                }}
                                ref={(ref) => {
                                    if (ref) {
                                        seriesRefs.current.set(`${symbol}-fast-ma`, ref)
                                    } else {
                                        seriesRefs.current.delete(`${symbol}-fast-ma`)
                                    }
                                }} >
                            </LineSeries>
                        )}

                        {data?.map(symbol =>
                            <LineSeries
                                key={`${symbol}-offset-ma`}
                                data={[]}
                                options={{
                                    lineWidth: 1,
                                    lineStyle: LineStyle.Dotted,
                                    color: initialData?.get(symbol)?.lineColor ?? "#9ccaff",
                                    priceLineVisible: false,
                                    lastValueVisible: false,
                                }}
                                ref={(ref) => {
                                    if (ref) {
                                        seriesRefs.current.set(`${symbol}-offset-ma`, ref)
                                    } else {
                                        seriesRefs.current.delete(`${symbol}-offset-ma`)
                                    }
                                }} >
                            </LineSeries>
                        )}
                    </Pane>
                </BarChart>
            </Grid>
        </Grid>
    )
}

export { AssetListPage as Component }

