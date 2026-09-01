import { Grid } from '@mui/material'
import type { StrategyParameterWithValue } from '@shared/api/schema'
import { FormDialog, type FormDialogProps } from '@shared/ui/dialog/FormDialog'
import { FormContainer, TextFieldElement, type FieldValues } from 'react-hook-form-mui'

export type StrategyParametersDialogProps = Omit<FormDialogProps, "formId"> & {
    parameters?: StrategyParameterWithValue[],
    onSuccess?: (data: FieldValues) => void
}

export const StrategyParametersDialog = ({
    onSuccess,
    parameters,
    ...rest
}: StrategyParametersDialogProps) => {
    const values = Object.fromEntries(parameters?.map(({ id, value }) => [id, value]) ?? [])
    return (
        <FormDialog
            title="Параметры стратегии"
            formId="parameters-form"
            {...rest}
            dialogContent={
                parameters &&
                <FormContainer
                    values={values}
                    FormProps={{ id: "parameters-form" }}
                    onSuccess={onSuccess}>
                    <Grid container spacing={1}>
                        {parameters?.map(param =>
                            <Grid key={param.id} size={12}>
                                <TextFieldElement name={param.id!} label={param.name} required />
                            </Grid>
                        )}
                    </Grid>
                </FormContainer>
            }>
        </FormDialog>
    )
}
