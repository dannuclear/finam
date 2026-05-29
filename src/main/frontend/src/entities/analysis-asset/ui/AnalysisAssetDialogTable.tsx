import { AnalysisAssetFormDialog, type AnalysisAssetFormDialogProps } from "./AnalysisAssetFormDialog"
import { AnalysisAssetTable, type AnalysisAssetTableProps } from "./AnalysisAssetTable"

export type AnalysisAssetDialogTableProps = {
    tableProps?: Omit<AnalysisAssetTableProps, "rows" | "onEdit" | "onDelete">,
    dialogProps?: Omit<AnalysisAssetFormDialogProps, "open" | "analysisAsset" | "onSuccess" | "onCancel">,
    open: AnalysisAssetFormDialogProps["open"],
    rows: AnalysisAssetTableProps["rows"],
    onEdit?: AnalysisAssetTableProps["onEdit"],
    onAdd?: AnalysisAssetTableProps["onAdd"],
    onDelete?: AnalysisAssetTableProps["onDelete"],
    analysisAsset: AnalysisAssetFormDialogProps["analysisAsset"],
    onSuccess?: AnalysisAssetFormDialogProps["onSuccess"],
    onCancel?: AnalysisAssetFormDialogProps["onCancel"]
}

export const AnalysisAssetDialogTable = ({
    tableProps,
    dialogProps,
    open,
    rows,
    onEdit,
    onAdd,
    onDelete,
    analysisAsset,
    onSuccess,
    onCancel
}: AnalysisAssetDialogTableProps) => {
    return (<>
        <AnalysisAssetTable
            rows={rows}
            onAdd={onAdd}
            onEdit={onEdit}
            onDelete={onDelete}
            {...tableProps} />
        <AnalysisAssetFormDialog
            open={open}
            analysisAsset={analysisAsset}
            onSuccess={onSuccess}
            onCancel={onCancel}
            {...dialogProps} />
    </>)
}
