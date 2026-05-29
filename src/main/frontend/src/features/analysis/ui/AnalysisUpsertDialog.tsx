import { AnalysisFormDialog, useAnalysis, type AnalysisFormDialogProps } from "@entities/analysis"
import { useAnalysisAssets } from "@entities/analysis-asset"
import type { Analysis, AnalysisAsset } from "@shared/api/schema"
import { useCreateAnalysis } from "../model/use-create-analysis"
import { useUpdateAnalysis } from "../model/use-update-analysis"

export type AnalysisUpsertDialogProps = Omit<AnalysisFormDialogProps, 'analysis' | 'onSuccess' | 'analysisAssetProps'> & {
    analysisId?: number,
    onDialogSuccess: () => void
}

export const AnalysisUpsertDialog = ({
    analysisId,
    onDialogSuccess,
    ...rest }: AnalysisUpsertDialogProps) => {

    const { data } = useAnalysis(analysisId)
    const { mutate: create, isPending: isCreatePending } = useCreateAnalysis()
    const { mutate: update, isPending: isUpdatePending } = useUpdateAnalysis()

    const {
        data: referenceAssets,
        addItemToCache: addReferenceAsset,
        updateItemInCache: updateReferenceAsset,
        deleteItemInCache: deleteReferenceAsset,
        prepare,
        analysisAsset,
        edit,
        cancel,
        resetCache
    } = useAnalysisAssets(analysisId)

    const onSuccessInternal = () => {
        resetCache()
        if (onDialogSuccess)
            onDialogSuccess()
    }

    const onDialogSuccessInternal = (data: Analysis, analysisAssets?: AnalysisAsset[]) => {
        if (data?.id) {
            update({
                params: {
                    path: {
                        id: data?.id
                    }
                }, body: { ...data, assets: analysisAssets }
            }, {
                onSuccess: onSuccessInternal
            })
        } else {
            create({
                body: { ...data, assets: analysisAssets }
            }, {
                onSuccess: onSuccessInternal
            })
        }
    }

    const onSuccessAnalysisAsset = (data: AnalysisAsset) => {
        if (data?._tempId)
            updateReferenceAsset(data)
        else
            addReferenceAsset(data)
    }

    return (
        <AnalysisFormDialog
            analysis={data ?? {}}
            pending={isCreatePending || isUpdatePending}
            onSuccess={onDialogSuccessInternal}

            analysisAssetProps={{
                rows: referenceAssets ?? [],
                onAdd: () => prepare({ lineColor: "#000000", lineWidth: 1, enabled: true, panelIndex: 1 }),
                onEdit: edit,
                onDelete: deleteReferenceAsset,
                analysisAsset: analysisAsset ?? {},
                open: analysisAsset != null,
                onSuccess: onSuccessAnalysisAsset,
                onCancel: cancel
            }}

            {...rest} />
    )
}
