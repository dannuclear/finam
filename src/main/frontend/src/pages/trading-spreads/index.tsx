import { useTradingSpreadsStart, useTradingSpreadsStatus, useTradingSpreadsStop, useTradingSpreadsSymbols } from "@features/trading-spreads"
import { Button } from "@mui/material"
import Grid from "@mui/material/Grid"
import { TIMEFRAMES, type TimeFrameConfig } from "@shared/model/timeframes"
import { BarChart } from "@shared/ui"
import { LineSeries, Pane, type SeriesApiRef } from "lightweight-charts-react-components"
import { useEffect, useRef, useState } from "react"

const AssetListPage = () => {
    const [timeFrame, setTimeFrame] = useState<TimeFrameConfig>(TIMEFRAMES[0]);

    const seriesRefs = useRef(
        new Map<string, SeriesApiRef<"Line">>()
    )
    const { data } = useTradingSpreadsSymbols()

    const { mutate: start, isPending: isStartPending } = useTradingSpreadsStart()
    const { mutate: stop, isPending: isStopPending } = useTradingSpreadsStop()
    const { data: isRunning } = useTradingSpreadsStatus()

    useEffect(() => {
        if (!isRunning) {
            return
        }

        const source = new EventSource(`/api/v1/spreads/subcribe`)

        source.addEventListener("quote", (event) => {
            const quote = JSON.parse(event.data)
            console.log(quote);

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

    return (
        <Grid container spacing={1}>
            <Grid size={6}>
                {data?.map(s => s).join('; ')}
            </Grid>
            <Grid size={6} container spacing={1}>
                <Button onClick={() => start({})} disabled={isRunning} loading={isStartPending}>Start</Button>
                <Button onClick={() => stop({})} disabled={!isRunning} loading={isStopPending}>Stop</Button>
            </Grid>
            <Grid size={12}>
                <BarChart
                    timeFrame={timeFrame}
                    onTimeFrameChange={(tf) => {
                        setTimeFrame(tf);
                    }}
                >
                    <Pane stretchFactor={2}>
                        {data?.map(symbol =>
                            <LineSeries
                                key={symbol}
                                data={[]}
                                options={{ lineWidth: 1, color: "#9ccaff" }}
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
                                options={{ lineWidth: 1, color: "#22f00f" }}
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
                                options={{ lineWidth: 1, color: "#f70d0d" }}
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

