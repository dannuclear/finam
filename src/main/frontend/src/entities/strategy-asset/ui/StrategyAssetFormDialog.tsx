import type { StrategyAsset } from '@shared/api/schema'
import { FormDialog, type FormDialogProps } from '@shared/ui/dialog/FormDialog'
import type { FieldValues } from 'react-hook-form'
import { StrategyAssetForm, type StrategyAssetFormProps } from './StrategyAssetForm'

export interface StrategyAssetFormDialogProps extends Omit<FormDialogProps, 'dialogContent' | 'formId'> {
    strategyAsset: StrategyAsset,
    formProps?: Omit<StrategyAssetFormProps, 'formId' | 'values'>,
    onSuccess?: (strategyAsset: StrategyAsset) => void
}

export const StrategyAssetFormDialog = ({
    strategyAsset,
    title = "Инструмент стратегии",
    onSuccess,
    formProps,
    ...props }: StrategyAssetFormDialogProps) => {

    const onSuccessInternal = (values: FieldValues) => {
        if (onSuccess)
            onSuccess(values)
    }

    return (
        <FormDialog
            maxWidth="md"
            fullWidth
            title={title}
            formId='strategy-asset-form'
            dialogContent={
                <StrategyAssetForm
                    formId='strategy-asset-form'
                    onSuccess={onSuccessInternal}
                    values={strategyAsset ?? {}}
                    {...formProps} />
            }
            {...props}
        />
    )
}