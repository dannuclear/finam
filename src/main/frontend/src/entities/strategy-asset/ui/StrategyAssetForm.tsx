
import { AssetSelectElement } from '@features/asset/ui/asset-select-element'
import { Grid } from '@mui/material'
import type { StrategyAsset } from '@shared/api/schema'
import { ColorPicker } from '@shared/ui'
import { FormContainer, TextFieldElement, type FormContainerProps } from 'react-hook-form-mui'

export type StrategyAssetFormProps = Omit<FormContainerProps, 'onSuccess'> & {
    formId?: string,
    onSuccess: (data: StrategyAsset) => void
}

export const StrategyAssetForm = ({
    formId,
    onSuccess,
    onError,
    ...props
}: StrategyAssetFormProps) => {
    return (
        <FormContainer
            onSuccess={onSuccess}
            onError={onError}
            FormProps={{ id: formId }}
            {...props}>

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
                {/* <Grid size={2}>
                    <TextFieldElement type='number' name='panelIndex' label="Панель" />
                </Grid>
                <Grid size={3}>
                    <SwitchElement name='enabled' label="Включен" />
                </Grid> */}
            </Grid>
        </FormContainer>
    )
}