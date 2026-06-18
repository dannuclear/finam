import { StrategyAssetFormDialog, type StrategyAssetFormDialogProps } from "./StrategyAssetFormDialog"
import { StrategyAssetTable, type StrategyAssetTableProps } from "./StrategyAssetTable"

export type StrategyAssetDialogTableProps = {
    tableProps?: Omit<StrategyAssetTableProps, "rows" | "onEdit" | "onDelete">,
    dialogProps?: Omit<StrategyAssetFormDialogProps, "open" | "strategyAsset" | "onSuccess" | "onCancel">,
    open: StrategyAssetFormDialogProps["open"],
    rows: StrategyAssetTableProps["rows"],
    onEdit?: StrategyAssetTableProps["onEdit"],
    onAdd?: StrategyAssetTableProps["onAdd"],
    onDelete?: StrategyAssetTableProps["onDelete"],
    strategyAsset: StrategyAssetFormDialogProps["strategyAsset"],
    onSuccess?: StrategyAssetFormDialogProps["onSuccess"],
    onCancel?: StrategyAssetFormDialogProps["onCancel"]
}

export const StrategyAssetDialogTable = ({
    tableProps,
    dialogProps,
    open,
    rows,
    onEdit,
    onAdd,
    onDelete,
    strategyAsset,
    onSuccess,
    onCancel
}: StrategyAssetDialogTableProps) => {
    return (<>
        <StrategyAssetTable
            rows={rows}
            onAdd={onAdd}
            onEdit={onEdit}
            onDelete={onDelete}
            {...tableProps} />
        <StrategyAssetFormDialog
            open={open}
            strategyAsset={strategyAsset}
            onSuccess={onSuccess}
            onCancel={onCancel}
            {...dialogProps} />
    </>)
}
