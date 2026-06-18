import { Grid } from '@mui/material'
import type { Strategy } from '@shared/api/schema'
import { FormContainer, TextFieldElement, type FormContainerProps } from 'react-hook-form-mui'

export type StrategyFormProps = Omit<FormContainerProps, 'onSuccess'> & {
    formId?: string,
    onSuccess: (data: Strategy) => void
}

export const StrategyForm = ({
    formId,
    ...props
}: StrategyFormProps) => {
    return (
        <FormContainer
            FormProps={{ id: formId }}
            {...props}>

            <Grid container spacing={1}>
                <Grid size={12}>
                    <TextFieldElement name='name' label='Наименование' required />
                </Grid>
            </Grid>
        </FormContainer>
    )
}