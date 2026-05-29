
import { AssetSelectElement } from '@features/asset/ui/asset-select-element'
import { Grid } from '@mui/material'
import type { TradeGroup, TradeGroupTradedAsset } from '@shared/api/schema'
import { ColorPicker } from '@shared/ui'
import { FormContainer, SwitchElement, TextFieldElement, useForm } from 'react-hook-form-mui'

export type TradeGroupTradedAssetFormProps = {
    tradeGroupTradedAsset: TradeGroupTradedAsset
    formId: string
    onSuccess?: (data: TradeGroup) => void
    onError?: () => void
}

export const TradeGroupTradedAssetForm = ({
    tradeGroupTradedAsset,
    formId,
    onSuccess,
    onError
}: TradeGroupTradedAssetFormProps) => {
    const formContext = useForm({ values: tradeGroupTradedAsset })

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
                <Grid size={3}>
                    <TextFieldElement type='number' name='lineWidth' label='Толщина' required />
                </Grid>
                <Grid size={3}>
                    <SwitchElement name='enabled' label="Включен" />
                </Grid>
            </Grid>
        </FormContainer>
    )
}