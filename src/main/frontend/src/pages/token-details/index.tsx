import { TextField } from "@mui/material"
import Grid from "@mui/material/Grid"
import { DataGrid, type GridColDef, type GridPaginationModel, type GridRowId } from "@mui/x-data-grid"
import { DateTimePicker } from "@mui/x-date-pickers/DateTimePicker"
import dayjs from "dayjs"
import { AccountInfoDialog, AccountTable, useAccount } from "@entities/account"
import { useTokenDetails } from "@entities/finam-token"
import React, { useState } from "react"

const permissionsColumns: GridColDef[] = [
    { field: "quoteLevel", headerName: "Уровень котировок", flex: 1, headerAlign: "center", sortable: false },
    { field: "delayMinutes", headerName: "Задержка в минутах", width: 200, headerAlign: "center", sortable: false },
    { field: "mic", headerName: "Идентификатор биржи mic", width: 200, headerAlign: "center", sortable: false },
    { field: "country", headerName: "Страна", width: 200, headerAlign: "center", sortable: false },
    { field: "continent", headerName: "Континент", width: 200, headerAlign: "center", sortable: false },
    { field: "worldwide", headerName: "Весь мир", width: 200, headerAlign: "center", sortable: false },
]

const TokenDetailsPage = () => {
    const [accountId, setAccountId] = useState<GridRowId | null>(null)

    const [permsPaginationModel, setPermsPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 5 })
    const { data } = useTokenDetails()
    const { account } = useAccount({ accountId: accountId as string })

    return (
        <React.Suspense fallback={<div>Loading...</div>}>
            <Grid container spacing={1} sx={{ pt: 1 }}>
                <Grid size={3}>
                    <DateTimePicker value={dayjs(data?.createdAt)} label="Создан" disabled />
                </Grid>
                <Grid size={3}>
                    <DateTimePicker value={dayjs(data?.expiresAt)} label="Действует до" disabled />
                </Grid>
                <Grid size={2}>
                    <TextField value={data?.readonly == false ? "Нет" : "Да"} label="Только для чтения" disabled />
                </Grid>
                <Grid size={12}>
                    <AccountTable
                        rows={data.accountIds}
                        onDetails={setAccountId} />
                </Grid>
                <Grid size={12}>
                    <DataGrid
                        columns={permissionsColumns}
                        rows={data?.mdPermissions}
                        getRowId={row => row.mic}
                        paginationModel={permsPaginationModel}
                        onPaginationModelChange={setPermsPaginationModel}
                        pageSizeOptions={[5, 10, 20, 40, 100]}
                    />
                </Grid>
            </Grid>

            <AccountInfoDialog
                open={Boolean(accountId)}
                onCancel={() => setAccountId(null)}
                account={account} />
        </React.Suspense>
    )
}

export { TokenDetailsPage as Component }

