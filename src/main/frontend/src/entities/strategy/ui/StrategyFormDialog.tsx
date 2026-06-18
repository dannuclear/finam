
import { StrategyAssetDialogTable, type StrategyAssetDialogTableProps } from '@entities/strategy-asset'
import Tab from '@mui/material/Tab'
import Tabs from '@mui/material/Tabs'
import type { Strategy, StrategyAsset } from '@shared/api/schema'
import { TabPanel } from '@shared/ui'
import { FormDialog, type FormDialogProps } from '@shared/ui/dialog/FormDialog'
import { useState } from 'react'
import { StrategyForm, type StrategyFormProps } from './StrategyForm'

export interface StrategyFormDialogProps extends Omit<FormDialogProps, 'dialogContent' | 'formId' | "onSuccess"> {
    strategy: Strategy,
    onSuccess: (data: Strategy, strategyAssets?: StrategyAsset[]) => void,
    formProps?: Omit<StrategyFormProps, 'strategy' | 'formId' | 'onSuccess'>,
    strategyAssetProps: StrategyAssetDialogTableProps
}

export const StrategyFormDialog = ({
    strategy,
    title = "Стратегия",
    formProps,
    onSuccess,
    strategyAssetProps,
    ...props }: StrategyFormDialogProps) => {

    const [tab, setTab] = useState<number>(0)

    const onSuccessInternal = (data: Strategy) => {
        if (onSuccess)
            onSuccess(data, strategyAssetProps.rows)
    }

    return (
        <FormDialog
            maxWidth="lg"
            fullWidth
            title={title}
            minHeight={500}
            formId='strategy-form'
            dialogContent={<>
                <Tabs value={tab} onChange={(_, value) => setTab(value)}>
                    <Tab label="Основные" />
                    <Tab label="Инструменты" />
                </Tabs>
                <TabPanel value={tab} index={0} prefix="strategy" persist>
                    <StrategyForm
                        formId='strategy-form'
                        values={strategy ?? {}}
                        onSuccess={onSuccessInternal}
                        {...formProps} />
                </TabPanel>
                <TabPanel value={tab} index={1} prefix="strategy" persist>
                    <StrategyAssetDialogTable {...strategyAssetProps} />
                </TabPanel>
            </>}
            {...props}
        />
    )
}