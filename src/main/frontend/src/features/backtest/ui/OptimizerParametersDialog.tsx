import { Grid } from '@mui/material'
import type { OptimizerStrategyParameter } from '@shared/api/schema'
import { FormDialog, type FormDialogProps } from '@shared/ui/dialog/FormDialog'
import { FormContainer, TextFieldElement, type FieldValues } from 'react-hook-form-mui'

export type OptimizerParametersDialogProps = Omit<FormDialogProps, "formId"> & {
    parameters?: OptimizerStrategyParameter[],
    onSuccess?: (data: FieldValues) => void
}

export const OptimizerParametersDialog = ({
    onSuccess,
    parameters,
    ...rest
}: OptimizerParametersDialogProps) => {
    const values = Object.fromEntries(
        parameters?.map(({ id, ...value }) => [id, value]) ?? []
    )
    return (
        <FormDialog
            title="Параметры оптимизатора"
            formId="optimizer-parameters-form"
            maxWidth="md"
            {...rest}
            dialogContent={
                <FormContainer
                    values={values}
                    FormProps={{ id: "optimizer-parameters-form" }}
                    onSuccess={onSuccess}
                >
                    <Grid container spacing={1}>
                        {parameters?.map((param) => (
                            <Grid key={param.id} size={12}>
                                <Grid container spacing={1}>
                                    <Grid size={6} alignContent="center">
                                        {/* <Typography>{param.name}</Typography> */}
                                    </Grid>

                                    <Grid size={2}>
                                        <TextFieldElement
                                            name={`${param.id}.from`}
                                            label="От"
                                            required
                                        />
                                    </Grid>

                                    <Grid size={2}>
                                        <TextFieldElement
                                            name={`${param.id}.to`}
                                            label="До"
                                            required
                                        />
                                    </Grid>

                                    <Grid size={2}>
                                        <TextFieldElement
                                            name={`${param.id}.step`}
                                            label="Шаг"
                                            required
                                        />
                                    </Grid>
                                </Grid>
                            </Grid>
                        ))}
                    </Grid>
                </FormContainer>
            }
        />
    )
}
