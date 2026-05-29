
import { DataGrid, type DataGridProps, type GridActionsCellItemProps, type GridRowId, type GridRowParams } from '@mui/x-data-grid'
import { DefaultGridActionColumn } from './DefaultGridActionColumn'
import { DefaultGridToolbar } from './DefaultGridToolbar'

export interface BaseDataGridProps extends DataGridProps {
    onAdd?: () => void,
    onEdit?: (id: GridRowId) => void,
    onDelete?: (id: GridRowId) => void,
    extraActions?: (params: GridRowParams) => React.ReactElement<GridActionsCellItemProps>[]
}

export const BaseDataGrid = ({ onEdit, onDelete, onAdd, extraActions, columns, ...props }: BaseDataGridProps) => {
    const hasActions = Boolean(onEdit || onDelete || extraActions)
    const defaultActionColumn = hasActions
        ? DefaultGridActionColumn({ onEdit, onDelete, extraActions })
        : null

    const finalColumns = hasActions
        ? [...columns, defaultActionColumn!]
        : columns

    return (
        <DataGrid
            showToolbar
            
            {...props}

            // Слоты
            slots={{
                toolbar: DefaultGridToolbar
            }}
            slotProps={{
                toolbar: {
                    ...props.slotProps?.toolbar,
                    onAdd,
                }
            }}
            columns={finalColumns}
        />
    )
}
