import { TradeGroupReferenceAssetForm, type TradeGroupReferenceAssetFormProps } from '@features/trade-group-edit'
import type { TradeGroupReferenceAsset } from '@shared/api/schema'
import { DefaultDialog, type DefaultDialogProps } from '@shared/ui'

interface TradeGroupReferenceAssetDialogProps extends Omit<DefaultDialogProps, 'dialogContent'> {
    tradeGroupReferenceAsset: TradeGroupReferenceAsset,
    formProps: Omit<TradeGroupReferenceAssetFormProps, 'tradeGroupReferenceAsset' | 'formId'>
}

export const TradeGroupReferenceAssetDialog = ({
    tradeGroupReferenceAsset,
    title = "Торговый инструмент",
    formProps,
    ...props }: TradeGroupReferenceAssetDialogProps) => {

    return (
        <DefaultDialog
            maxWidth="md"
            fullWidth
            title={title}
            formId='trade-group-reference-asset-form'
            onSave={() => { }}
            dialogContent={
                <TradeGroupReferenceAssetForm
                    formId='trade-group-reference-asset-form'
                    tradeGroupReferenceAsset={tradeGroupReferenceAsset ?? {}}
                    {...formProps} />
            }
            {...props}
        />
    )
}