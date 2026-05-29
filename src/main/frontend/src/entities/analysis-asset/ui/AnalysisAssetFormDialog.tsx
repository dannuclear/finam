import type { AnalysisAsset } from '@shared/api/schema'
import { FormDialog, type FormDialogProps } from '@shared/ui/dialog/FormDialog'
import type { FieldValues } from 'react-hook-form'
import { AnalysisAssetForm, type AnalysisAssetFormProps } from './AnalysisAssetForm'

export interface AnalysisAssetFormDialogProps extends Omit<FormDialogProps, 'dialogContent' | 'formId'> {
    analysisAsset: AnalysisAsset,
    formProps?: Omit<AnalysisAssetFormProps, 'formId' | 'values'>,
    onSuccess?: (analysisAsset: AnalysisAsset) => void
}

export const AnalysisAssetFormDialog = ({
    analysisAsset,
    title = "Серия",
    onSuccess,
    formProps,
    ...props }: AnalysisAssetFormDialogProps) => {

    const onSuccessInternal = (values: FieldValues) => {
        if (onSuccess)
            onSuccess(values)
    }

    return (
        <FormDialog
            maxWidth="md"
            fullWidth
            title={title}
            formId='analysis-asset-form'
            dialogContent={
                <AnalysisAssetForm
                    formId='analysis-asset-form'
                    onSuccess={onSuccessInternal}
                    values={analysisAsset ?? {}}
                    {...formProps} />
            }
            {...props}
        />
    )
}