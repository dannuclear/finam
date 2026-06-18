import { StrategySelect } from "@entities/strategy"
import { useRunBacktest } from "@features/backtest"
import { Button } from "@mui/material"
import Grid from "@mui/material/Grid"
import { DateTimePicker } from "@mui/x-date-pickers/DateTimePicker"
import type { Strategy } from "@shared/api/schema"
import { TIMEFRAMES, type TimeFrameConfig } from "@shared/model/timeframes"
import type { Dayjs } from "dayjs"
import dayjs from "dayjs"
import { useState } from "react"

const BacktestPage = () => {
    const [strategy, setStrategy] = useState<Strategy | null>(null)
    const [timeFrame] = useState<TimeFrameConfig>(TIMEFRAMES[7]);
    const [startTime, setStartTime] = useState<Dayjs>(dayjs().subtract(timeFrame.maxDays, "day"))
    const [endTime, setEndTime] = useState<Dayjs>(dayjs())

    const { mutate: run } = useRunBacktest()

    const runInternal = () => {
        if (strategy)
            run({
                params: {
                    path: {
                        id: Number(strategy?.id)
                    },
                    query: {
                        timeFrame: timeFrame.value,
                        startTime: startTime.format(),
                        endTime: endTime.format(),
                    }
                }
            })
    }

    return (<>
        <Grid container spacing={1} sx={{ pt: 1 }}>
            <Grid size={12}>
                <StrategySelect
                    value={strategy}
                    onChange={(_, val) => setStrategy(val)} />
            </Grid>
            <Grid size={{ xs: 6, lg: 2 }}>
                <DateTimePicker label="Период с" value={startTime} onChange={val => val && setStartTime(val)} />
            </Grid>
            <Grid size={{ xs: 6, lg: 2 }}>
                <DateTimePicker label="Период по" value={endTime} onChange={val => val && setEndTime(val)} />
            </Grid>
            <Grid size={12}>
                <Button color="success" onClick={runInternal}>Запустить</Button>
            </Grid>
        </Grid>
    </>)
}

export { BacktestPage as Component }

