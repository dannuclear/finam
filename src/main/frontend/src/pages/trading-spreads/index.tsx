import { AssetSelect } from "@features/asset/ui/asset-select"
import { useTradingSpreadsStart, useTradingSpreadsStatus, useTradingSpreadsStop, useTradingSpreadsSymbols } from "@features/trading-spreads"
import { Button, FormControlLabel, Switch, TextField } from "@mui/material"
import Grid from "@mui/material/Grid"
import type { Asset } from "@shared/api/schema"
import { TIMEFRAMES, type TimeFrameConfig } from "@shared/model/timeframes"
import { BarChart } from "@shared/ui"
import Legend from "@widgets/chart/ui/legend"
import { LineStyle } from "lightweight-charts"
import { LineSeries, Pane, type SeriesApiRef } from "lightweight-charts-react-components"
import { useEffect, useRef, useState } from "react"

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
    { name: "ОФЗ 26254", symbol: "SU26254RMFS1@MISX" },
    { name: "ОФЗ 26238", symbol: "SU26238RMFS4@MISX" },
    { name: "ОФЗ 26253", symbol: "SU26253RMFS3@MISX" },
    { name: "ОФЗ 26247", symbol: "SU26247RMFS5@MISX" },
    { name: "ОФЗ 26230", symbol: "SU26230RMFS1@MISX" },
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

    const { mutate: start, isPending: isStartPending } = useTradingSpreadsStart()
    const { mutate: stop, isPending: isStopPending } = useTradingSpreadsStop()
    const { data: isRunning } = useTradingSpreadsStatus()

    const [assets, setAssets] = useState<Asset[]>(defaultAssets)
    const [fastMaCount, setFastMaCount] = useState<number>(4)
    const [daysCount, setDaysCount] = useState<number>(6)
    const [spread, setSpread] = useState<number>(0.2)
    const [showPrice, setShowPrice] = useState<boolean>(false)

    const [seriesColors, setSeriesColors] = useState<Record<string, string>>({});

    const startInternal = () => {
        if (!assets) {
            return;
        }

        const colors: Record<string, string> = {};

        assets.forEach((asset, index) => {
            const symbol = asset.symbol ?? "";
            colors[symbol] = generateColor(index);
        });

        setSeriesColors(colors);

        start({
            params: {
                query: {
                    assets: assets?.map(a => a.symbol ?? ""),
                    fastMaCount: fastMaCount,
                    daysCount: daysCount,
                    spread: spread
                }
            }
        })
    }

    useEffect(() => {
        if (!isRunning) {
            return
        }

        const source = new EventSource(`/api/v1/spreads/subcribe`)

        source.addEventListener("quote", (event) => {
            const quote = JSON.parse(event.data)
            // console.log(quote);

            const ref = seriesRefs.current.get(quote.symbol)

            if (ref) {
                const series = ref.api();
                if (!series || !quote.last) return;

                series.update({
                    time: quote.seconds,
                    value: quote.last
                });
                // console.log(quote.symbol, ref)
                // console.log(quote.symbol, quote.mills, quote.last)
            }

            const fastMaRef = seriesRefs.current.get(`${quote.symbol}-fast-ma`)

            if (fastMaRef) {
                const series = fastMaRef.api();
                if (!series || !quote.fastMa) return;

                series.update({
                    time: quote.seconds,
                    value: quote.fastMa
                });
            }

            const slowMaRef = seriesRefs.current.get(`${quote.symbol}-slow-ma`)

            if (slowMaRef) {
                const series = slowMaRef.api();
                if (!series || !quote.slowMa) return;

                series.update({
                    time: quote.seconds,
                    value: quote.slowMa
                });
            }

            const offsetMaRef = seriesRefs.current.get(`${quote.symbol}-offset`)

            if (offsetMaRef) {
                const series = offsetMaRef.api();
                if (!series || !quote.offset) return;

                series.update({
                    time: quote.seconds,
                    value: quote.offset
                });
            }
        })

        return () => {
            source.close()
        }

    }, [isRunning])

    const legendOptions = assets?.map(item => {
        const id = item.symbol ?? "";

        return {
            id,
            label: item.name ?? "no-name",
            color: seriesColors[id] ?? "rgba(0, 0, 0, 0)",
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
                    onChange={e => setFastMaCount(Number(e.target.value))} />
            </Grid>
            <Grid size={1}>
                <TextField
                    label="Дней средней"
                    value={daysCount}
                    onChange={e => setDaysCount(Number(e.target.value))} />
            </Grid>
            <Grid size={1}>
                <TextField
                    label="Спред"
                    value={spread}
                    onChange={e => setSpread(Number(e.target.value))} />
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
                                    color: seriesColors[symbol] ?? "#9ccaff",
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
                                data={[]}
                                options={{
                                    lineWidth: 1,
                                    lineStyle: LineStyle.Dotted,
                                    color: seriesColors[symbol] ?? "#9ccaff",
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
                                key={`${symbol}-offset`}
                                data={[]}
                                options={{
                                    lineWidth: 1,
                                    color: seriesColors[symbol] ?? "#9ccaff",
                                    priceLineVisible: false,
                                    lastValueVisible: false,
                                }}
                                ref={(ref) => {
                                    if (ref) {
                                        seriesRefs.current.set(`${symbol}-offset`, ref)
                                    } else {
                                        seriesRefs.current.delete(`${symbol}-offset`)
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

