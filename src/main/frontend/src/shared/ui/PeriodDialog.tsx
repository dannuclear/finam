import { Grid } from '@mui/material'
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import { useState } from 'react'
import { DefaultDialog, type DefaultDialogProps } from '.'

export interface PeriodDialogProps extends Omit<DefaultDialogProps, 'dialogContent' | 'onOk'> {
    onOk: (startTime: Dayjs | null, endTime: Dayjs | null) => void,
}

const PeriodDialog = ({
    title = "Торговый набор",
    onOk,
    ...props }: PeriodDialogProps) => {

    const [startTime, setStartTime] = useState<Dayjs | null>(dayjs())
    const [endTime, setEndTime] = useState<Dayjs | null>(dayjs())

    const onOkInternal = () => {
        if (!onOk)
            return;
        onOk(startTime, endTime);
    }

    return (
        <DefaultDialog
            maxWidth="sm"
            fullWidth
            title={title}
            onOk={onOkInternal}
            dialogContent={<>
                <Grid container spacing={1}>
                    <Grid size={6}>
                        <DateTimePicker
                            value={startTime}
                            onChange={val => setStartTime(val)}
                            label="Начало периода" />
                    </Grid>
                    <Grid size={6}>
                        <DateTimePicker
                            value={endTime}
                            onChange={val => setEndTime(val)}
                            label="Конец периода" />
                    </Grid>
                </Grid>
            </>}
            {...props}
        />
    )
}

export default PeriodDialog