
import { Grid } from '@mui/material'
import type { TradeGroup } from '@shared/api/schema'
import { FormContainer, TextFieldElement, useForm } from 'react-hook-form-mui'

export type TradeGroupFormProps = {
    tradeGroup: TradeGroup
    formId: string
    onSuccess?: (data: TradeGroup) => void
    onError?: () => void
}

export const TradeGroupForm = ({
    tradeGroup,
    formId,
    onSuccess,
    onError
}: TradeGroupFormProps) => {
    const formContext = useForm({ values: tradeGroup })

    return (
        <FormContainer
            formContext={formContext}
            onSuccess={onSuccess}
            onError={onError}
            FormProps={{ id: formId }}>

            <Grid container spacing={1}>
                <Grid size={12}>
                    <TextFieldElement name='name' label='Наименование' required slotProps={{ input: { autoComplete: "off" } }} />
                </Grid>
                <Grid size={12}>
                    <TextFieldElement name='description' label='Описание' slotProps={{ input: { autoComplete: "off" } }}/>
                </Grid>
            </Grid>
        </FormContainer>
    )
}