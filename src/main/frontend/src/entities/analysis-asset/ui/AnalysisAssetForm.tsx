
import { AssetSelectElement } from '@features/asset/ui/asset-select-element'
import { Grid } from '@mui/material'
import type { AnalysisAsset } from '@shared/api/schema'
import { ColorPicker } from '@shared/ui'
import { FormContainer, SwitchElement, TextFieldElement, useForm, type FormContainerProps } from 'react-hook-form-mui'

export type AnalysisAssetFormProps = Omit<FormContainerProps, 'onSuccess'> & {
    formId?: string,
    onSuccess: (data: AnalysisAsset) => void
}

export const AnalysisAssetForm = ({
    formId,
    onSuccess,
    onError,
    ...props
}: AnalysisAssetFormProps) => {
    const formContext = useForm(props)

    return (
        <FormContainer
            formContext={formContext}
            onSuccess={onSuccess}
            onError={onError}
            FormProps={{ id: formId }}>

            <Grid container spacing={1}>
                <Grid size={12}>
                    <AssetSelectElement name='asset' />
                </Grid>
                <Grid size={2}>
                    <ColorPicker name='lineColor' />
                </Grid>
                <Grid size={2}>
                    <TextFieldElement type='number' name='lineWidth' label='Толщина' required />
                </Grid>
                <Grid size={2}>
                    <TextFieldElement type='number' name='panelIndex' label="Панель" />
                </Grid>
                <Grid size={3}>
                    <SwitchElement name='enabled' label="Включен" />
                </Grid>
            </Grid>
        </FormContainer>
    )
}