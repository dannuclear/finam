
import { AnalysisAssetDialogTable, type AnalysisAssetDialogTableProps } from '@entities/analysis-asset'
import Tab from '@mui/material/Tab'
import Tabs from '@mui/material/Tabs'
import type { Analysis, AnalysisAsset } from '@shared/api/schema'
import { TabPanel } from '@shared/ui'
import { FormDialog, type FormDialogProps } from '@shared/ui/dialog/FormDialog'
import { useState } from 'react'
import { AnalysisForm, type AnalysisFormProps } from './AnalysisForm'

export interface AnalysisFormDialogProps extends Omit<FormDialogProps, 'dialogContent' | 'formId' | "onSuccess"> {
    analysis: Analysis,
    onSuccess: (data: Analysis, analysisAssets?: AnalysisAsset[]) => void,
    formProps?: Omit<AnalysisFormProps, 'analysis' | 'formId' | 'onSuccess'>,
    analysisAssetProps: AnalysisAssetDialogTableProps
}

export const AnalysisFormDialog = ({
    analysis,
    title = "Анализ",
    formProps,
    onSuccess,
    analysisAssetProps,
    ...props }: AnalysisFormDialogProps) => {

    const [tab, setTab] = useState<number>(0)

    const onSuccessInternal = (data: Analysis) => {
        if (onSuccess)
            onSuccess(data, analysisAssetProps.rows)
    }

    return (
        <FormDialog
            maxWidth="lg"
            fullWidth
            title={title}
            minHeight={500}
            formId='analysis-form'
            dialogContent={<>
                <Tabs value={tab} onChange={(_, value) => setTab(value)}>
                    <Tab label="Основные" />
                    <Tab label="Инструменты" />
                </Tabs>
                <TabPanel value={tab} index={0} prefix="analysis" persist>
                    <AnalysisForm
                        formId='analysis-form'
                        values={analysis ?? {}}
                        onSuccess={onSuccessInternal}
                        {...formProps} />
                </TabPanel>
                <TabPanel value={tab} index={1} prefix="analysis" persist>
                    <AnalysisAssetDialogTable {...analysisAssetProps} />
                </TabPanel>
            </>}
            {...props}
        />
    )
}