
import { type GridColDef, type GridRowId, type GridRowParams } from '@mui/x-data-grid';
import { ServerDataGrid, type ServerDataGridProps } from '@shared/ui';
import SubscribeActionCellItem from './SubscribeActionCellItem';

const columns: GridColDef[] = [
    { field: "id", headerName: "ID", width: 80, headerAlign: "center", align: "right", sortable: false },
    { field: "symbol", headerName: "symbol", width: 200, headerAlign: "center", align: "left", sortable: false },
    { field: "ticker", headerName: "ticker", width: 150, headerAlign: "center", sortable: false },
    { field: "mic", headerName: "mic", width: 130, headerAlign: "center", sortable: false },
    { field: "isin", headerName: "isin", width: 150, headerAlign: "center", align: "left", sortable: false },
    { field: "type", headerName: "type", width: 150, headerAlign: "center", align: "left", sortable: false },
    { field: "name", headerName: "name", flex: 1, headerAlign: "center", sortable: false },
]

export interface PersonTableProps extends Omit<ServerDataGridProps, "path" | "columns"> {
    onSubscribe?: (id: GridRowId) => void
}

export const AssetTable = ({ onSubscribe, ...props }: PersonTableProps) => {

    const extraActions = (params: GridRowParams) => [
        <SubscribeActionCellItem onClick={() => onSubscribe && onSubscribe(params.id)} />,
    ]
    return (
        <ServerDataGrid
            path="/api/v1/finam/assets"
            columns={columns}
            extraActions={extraActions}
            getRowId={row => row.symbol}
            {...props} />
    )
}