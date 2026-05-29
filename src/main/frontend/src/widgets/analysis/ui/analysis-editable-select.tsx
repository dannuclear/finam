import { AnalysisSelect, useAnalysis, type AnalysisSelectProps } from '@entities/analysis'
import { AnalysisUpsertDialog, useDeleteAnalysis } from '@features/analysis'
import { Button, Grid } from '@mui/material'
import { DefaultIcon } from '@shared/ui'
import { useState } from 'react'

export type AnalysisEditableSelectProps = AnalysisSelectProps<false>

export const AnalysisEditableSelect = ({
    value,
    ...rest }: AnalysisEditableSelectProps) => {
    const [open, setOpen] = useState<boolean>(false)

    const { data } = useAnalysis(value?.id)
    const { mutate: del } = useDeleteAnalysis()

    const onAdd = () => {
        setOpen(true)
    }

    const onEdit = () => {
        setOpen(true)
    }

    const onDelete = () => {
        if (data?.id)
            del({
                params: {
                    path: {
                        id: data?.id
                    }
                }
            })
    }

    const onCancel = () => {
        setOpen(false)
    }

    return (<Grid container spacing={0.1}>
        <Grid flex={1}>
            <AnalysisSelect {...rest} />
        </Grid>

        {onAdd && <Button onClick={() => onAdd()} sx={{ ml: 1, minWidth: "40px" }}>
            <DefaultIcon iconStyle='fa-regular' iconName="fa-plus-circle fa-xl" color="blue" />
        </Button>}
        {value?.id && onEdit && <Button onClick={() => onEdit()} sx={{ ml: 1, minWidth: "40px" }}>
            <DefaultIcon iconStyle='fa-regular' iconName="fa-pencil fa-xl" color="blue" />
        </Button>}
        {value?.id && onDelete && <Button onClick={() => onDelete()} sx={{ ml: 1, minWidth: "40px" }}>
            <DefaultIcon iconStyle='fa-regular' iconName="fa-trash fa-xl" color="red" />
        </Button>}

        <AnalysisUpsertDialog
            open={open}
            analysisId={value?.id}
            onCancel={onCancel}
            onDialogSuccess={() => setOpen(false)}
        />
    </Grid>)
}