import { StrategyFormDialog, useStrategy, type StrategyFormDialogProps } from "@entities/strategy"
import { useStrategyAssets } from "@entities/strategy-asset"
import type { Strategy, StrategyAsset } from "@shared/api/schema"
import { useCreateStrategy } from "../model/use-create-strategy"
import { useUpdateStrategy } from "../model/use-update-strategy"

export type StrategyUpsertDialogProps = Omit<StrategyFormDialogProps, 'strategy' | 'onSuccess' | 'strategyAssetProps'> & {
    strategyId?: number,
    onDialogSuccess: () => void
}

export const StrategyUpsertDialog = ({
    strategyId,
    onDialogSuccess,
    ...rest }: StrategyUpsertDialogProps) => {

    const { data } = useStrategy(strategyId)
    const { mutate: create, isPending: isCreatePending } = useCreateStrategy()
    const { mutate: update, isPending: isUpdatePending } = useUpdateStrategy()

    const {
        data: referenceAssets,
        addItemToCache: addReferenceAsset,
        updateItemInCache: updateReferenceAsset,
        deleteItemInCache: deleteReferenceAsset,
        prepare,
        strategyAsset,
        edit,
        cancel,
        resetCache
    } = useStrategyAssets(strategyId)

    const onSuccessInternal = () => {
        resetCache()
        if (onDialogSuccess)
            onDialogSuccess()
    }

    const onDialogSuccessInternal = (data: Strategy, strategyAssets?: StrategyAsset[]) => {
        if (data?.id) {
            update({
                params: {
                    path: {
                        id: data?.id
                    }
                }, body: { ...data, assets: strategyAssets }
            }, {
                onSuccess: onSuccessInternal
            })
        } else {
            create({
                body: { ...data, assets: strategyAssets }
            }, {
                onSuccess: onSuccessInternal
            })
        }
    }

    const onSuccessStrategyAsset = (data: StrategyAsset) => {
        if (data?._tempId)
            updateReferenceAsset(data)
        else
            addReferenceAsset(data)
    }

    return (
        <StrategyFormDialog
            strategy={data ?? {}}
            pending={isCreatePending || isUpdatePending}
            onSuccess={onDialogSuccessInternal}

            strategyAssetProps={{
                rows: referenceAssets ?? [],
                onAdd: () => prepare({ lineColor: "#000000", lineWidth: 1 }),
                onEdit: edit,
                onDelete: deleteReferenceAsset,
                strategyAsset: strategyAsset ?? {},
                open: strategyAsset != null,
                onSuccess: onSuccessStrategyAsset,
                onCancel: cancel
            }}

            {...rest} />
    )
}
