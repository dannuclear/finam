import { DataGrid, type GridColDef } from "@mui/x-data-grid"
import { rqClient } from "@shared/api/instance"

const columns: GridColDef[] = [
    { field: "id", headerName: "Счета", flex: 1, headerAlign: "center", sortable: false },
]

const AccountsPage = () => {
    const { data } = rqClient.useQuery("get", "/api/v1/finam/token-details")

    return (<>
        <DataGrid
            columns={columns}
            rows={data?.accountIds?.map(el => ({ id: el }))}
            getRowId={row => row}
        />
    </>)
}

export { AccountsPage as Component }

