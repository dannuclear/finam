
import { QuoteChartActionCellItem } from '@features/trade-group-chart';
import { Button } from '@mui/material';
import Switch from '@mui/material/Switch';
import { type GridColDef, type GridRowId, type GridRowParams } from '@mui/x-data-grid';
import { rqClient } from '@shared/api/instance';
import { queryClient } from '@shared/api/query-client';
import { ServerDataGrid, type ServerDataGridProps } from '@shared/ui';

const columns: GridColDef[] = [
    { field: "id", headerName: "ID", width: 80, headerAlign: "center", align: "right", sortable: false },
    { field: "name", headerName: "Наименование", width: 200, headerAlign: "center", align: "left", sortable: false },
    { field: "description", headerName: "Описание", flex: 1, headerAlign: "center", sortable: false },
]

export interface TradeGroupTableProps extends Omit<ServerDataGridProps, "path" | "columns"> {
    onShowQuotes?: (id: GridRowId) => void,
    onCalcOffsets?: (id: GridRowId) => void
}

export const TradeGroupTable = ({ onShowQuotes, onCalcOffsets, ...props }: TradeGroupTableProps) => {
    const { mutate: toggle, isPending } = rqClient.useMutation("post", "/api/v1/trade-groups/{id}/toggle-active");

    const onSettled = () => {
        queryClient.invalidateQueries(rqClient.queryOptions("get", "/api/v1/trade-groups"))
    }

    const extraActions = (params: GridRowParams) => [
        <QuoteChartActionCellItem onClick={() => onShowQuotes && onShowQuotes(params.id)} />
    ]

    const dynamicColumns: GridColDef[] = [
        ...columns,
        {
            field: " ", headerName: "OFS", width: 100, headerAlign: "center", align: "center", sortable: false, renderCell: (params) => {
                return <Button onClick={() => onCalcOffsets && onCalcOffsets(params.id)}>CALC</Button>
            }
        },
        {
            field: "active", headerName: "Включен", width: 100, headerAlign: "center", align: "center", sortable: false, renderCell(params) {
                return <Switch
                    disabled={isPending}
                    checked={params.value}
                    onChange={() => toggle({
                        params: {
                            path: {
                                id: params.id as number
                            }
                        }
                    }, { onSettled })}
                />
            },
        },
    ]

    return (
        <ServerDataGrid
            path="/api/v1/trade-groups"
            columns={dynamicColumns}
            extraActions={extraActions}
            {...props} />
    )
}