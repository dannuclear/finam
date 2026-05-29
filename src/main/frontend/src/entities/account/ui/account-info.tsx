import { TextField } from "@mui/material";
import Grid from "@mui/material/Grid";
import type { Account } from "@shared/api/schema";
import React from "react";
import CashTable from "./cash-table";
import PositionTable from "./position-table";

interface AccountInfoProps {
    account: Account;
}

const AccountInfo = React.memo(({ account }: AccountInfoProps) => {
    return (
        <Grid container spacing={1}>
            <Grid size={2}>
                <TextField
                    label="Стоимость"
                    value={account.equity ?? ""}
                    disabled />
            </Grid>
            <Grid size={2}>
                <TextField
                    label="Нереализованная прибыль"
                    value={account.unrealizedProfit ?? ""}
                    disabled
                />
            </Grid>
            <Grid size={12}>
                <PositionTable positions={account?.positions} />
            </Grid>
            <Grid size={12}>
                <CashTable cash={account?.cash} />
            </Grid>
        </Grid>
    )
})

export default AccountInfo