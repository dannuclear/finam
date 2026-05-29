import { Grid } from '@mui/material'
import type { Analysis } from '@shared/api/schema'
import { FormContainer, TextFieldElement, type FormContainerProps } from 'react-hook-form-mui'

export type AnalysisFormProps = Omit<FormContainerProps, 'onSuccess'> & {
    formId?: string,
    onSuccess: (data: Analysis) => void
}

export const AnalysisForm = ({
    formId,
    ...props
}: AnalysisFormProps) => {
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