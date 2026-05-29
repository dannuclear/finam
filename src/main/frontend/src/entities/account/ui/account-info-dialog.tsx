import { Grid, Tab, Tabs } from '@mui/material'
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import type { Account } from '@shared/api/schema'
import { DefaultDialog, TabPanel, type DefaultDialogProps } from '@shared/ui'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import { useState } from 'react'
import useAccountTrades from '../api/use-account-trades'
import useAccountTransactions from '../api/use-account-transactions'
import AccountInfo from './account-info'
import TradesTable from './trades-table'
import TransactionsTable from './transactions-table'

interface AccountInfoDialogProps extends Omit<DefaultDialogProps, 'dialogContent'> {
    account?: Account
}

const AccountInfoDialog = ({
    account,
    title = "Информация о счете",
    ...props }: AccountInfoDialogProps) => {

    const [tab, setTab] = useState<number>(0)
    const [startTime, setStartTime] = useState<Dayjs | null>(dayjs().subtract(1, 'year'))
    const [endTime, setEndTime] = useState<Dayjs | null>(dayjs())
    const { tradesData } = useAccountTrades({ accountId: account?.accountId, startTime, endTime })
    const { transactionsData } = useAccountTransactions({ accountId: account?.accountId, startTime, endTime })

    return (
        <DefaultDialog
            maxWidth="lg"
            fullWidth
            title={title}
            minHeight={700}
            dialogContent={
                <>
                    <Tabs value={tab} onChange={(_, val) => setTab(val)}>
                        <Tab label="Основные данные" />
                        <Tab label="Сделки" />
                        <Tab label="Транзакции" />
                    </Tabs>
                    <TabPanel value={tab} index={0} prefix="account" persist>
                        <AccountInfo account={account ?? {}} />
                    </TabPanel>
                    <TabPanel value={tab} index={1} prefix="account">
                        <Grid container spacing={1}>
                            <Grid size={3}>
                                <DateTimePicker label="Начало" value={startTime} onChange={(value) => setStartTime(value)} />
                            </Grid>
                            <Grid size={3}>
                                <DateTimePicker label="Конец" value={endTime} onChange={(value) => setEndTime(value)} />
                            </Grid>
                            <Grid size={12}>
                                <TradesTable trades={tradesData?.trades ?? []} />
                            </Grid>
                        </Grid>
                    </TabPanel>
                    <TabPanel value={tab} index={2} prefix="account">
                        <Grid container spacing={1}>
                            <Grid size={3}>
                                <DateTimePicker label="Начало" value={startTime} onChange={(value) => setStartTime(value)} />
                            </Grid>
                            <Grid size={3}>
                                <DateTimePicker label="Конец" value={endTime} onChange={(value) => setEndTime(value)} />
                            </Grid>
                            <Grid size={12}>
                                <TransactionsTable transactions={transactionsData ?? []} />
                            </Grid>
                        </Grid>
                    </TabPanel>
                </>
            }
            {...props}
        />
    )
}

export default AccountInfoDialog