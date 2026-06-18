
import { type GridColDef } from '@mui/x-data-grid';
import { ServerDataGrid, type ServerDataGridProps } from '@shared/ui';

const columns: GridColDef[] = [
    { field: "id", headerName: "ID", width: 80, headerAlign: "center", align: "right", sortable: false },
    { field: "name", headerName: "name", flex: 1, headerAlign: "center", sortable: false }
]

export type PersonTableProps = Omit<ServerDataGridProps, "path" | "columns">

export const StrategyTable = ({ ...props }: PersonTableProps) => {

    return (
        <ServerDataGrid
            path="/api/v1/strategies"
            columns={columns}
            {...props} />
    )
}