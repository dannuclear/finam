import { TradeGroupTradedAssetForm, type TradeGroupTradedAssetFormProps } from '@features/trade-group-edit'
import type { TradeGroupTradedAsset } from '@shared/api/schema'
import { DefaultDialog, type DefaultDialogProps } from '@shared/ui'

interface TradeGroupTradedAssetDialogProps extends Omit<DefaultDialogProps, 'dialogContent'> {
    tradeGroupTradedAsset: TradeGroupTradedAsset,
    formProps: Omit<TradeGroupTradedAssetFormProps, 'tradeGroupTradedAsset' | 'formId'>
}

export const TradeGroupTradedAssetDialog = ({
    tradeGroupTradedAsset,
    title = "Торговый инструмент",
    formProps,
    ...props }: TradeGroupTradedAssetDialogProps) => {

    return (
        <DefaultDialog
            maxWidth="sm"
            fullWidth
            title={title}
            formId='trade-group-traded-asset-form'
            onSave={() => { }}
            dialogContent={
                <TradeGroupTradedAssetForm
                    formId='trade-group-traded-asset-form'
                    tradeGroupTradedAsset={tradeGroupTradedAsset ?? {}}
                    {...formProps} />
            }
            {...props}
        />
    )
}