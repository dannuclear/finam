import { GridActionsCellItem, type DataGridProps, type GridColDef, type GridRowId } from "@mui/x-data-grid";
import { DataGrid } from "@mui/x-data-grid/DataGrid";
import { DefaultIcon } from "@shared/ui";
import { useMemo } from "react";

interface AccountTableProps extends Omit<DataGridProps, "columns"> {
    onDetails?: (id: GridRowId) => void,
}

const AccountTable = ({ onDetails, rows, ...props }: AccountTableProps) => {
    const data = rows?.map((id) => ({ id }));

    const columns = useMemo<GridColDef[]>(() => [
        {
            field: "id",
            headerName: "Счета",
            flex: 1,
            headerAlign: "center",
            sortable: false,
        },
        {
            field: "actions",
            type: "actions",
            width: 100,
            getActions: (params) => [
                <GridActionsCellItem
                    key="details"
                    icon={<DefaultIcon iconName="fa-magnifying-glass-dollar" />}
                    color="primary"
                    label=""
                    onClick={() => onDetails?.(params.id)}
                />
            ],
        },
    ], [onDetails]);

    return (
        <DataGrid
            columns={columns}
            rows={data}
            hideFooter
            {...props}
        />
    )
}

export default AccountTable