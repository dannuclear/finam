import { TradeGroupForm, type TradeGroupFormProps } from '@features/trade-group-edit'
import Tab from '@mui/material/Tab'
import Tabs from '@mui/material/Tabs'
import type { TradeGroup, TradeGroupReferenceAsset, TradeGroupTradedAsset } from '@shared/api/schema'
import { DefaultDialog, TabPanel, type DefaultDialogProps } from '@shared/ui'
import { TradeGroupReferenceAssetTable, TradeGroupTradedAssetTable, useTradeGroupReferenceAssets, useTradeGroupTradedAssets } from '@entities/trade-group'
import { useState } from 'react'
import { TradeGroupReferenceAssetDialog } from './trade-group-reference-assets-dialog'
import { TradeGroupTradedAssetDialog } from './trade-group-traded-assets-dialog'

interface TradeGroupDialogProps extends Omit<DefaultDialogProps, 'dialogContent'> {
    tradeGroup: TradeGroup,
    onDialogSuccess: (data: TradeGroup, tradedAssets?: TradeGroupTradedAsset[], referenceAssets?: TradeGroupReferenceAsset[]) => void,
    formProps?: Omit<TradeGroupFormProps, 'tradeGroup' | 'formId' | 'onSuccess'>
}

export const TradeGroupDialog = ({
    tradeGroup,
    title = "Торговый набор",
    formProps,
    onDialogSuccess,
    ...props }: TradeGroupDialogProps) => {

    const [tab, setTab] = useState<number>(0)
    const [tradedAsset, setTradedAsset] = useState<TradeGroupTradedAsset | null>(null)
    const [referenceAsset, setReferenceAsset] = useState<TradeGroupReferenceAsset | null>(null)

    const {
        data: tradedAssets,
        addItemToCache: addTradedAsset,
        updateItemInCache: updateTradedAsset,
        deleteItemInCache: deleteTradedAsset,
        getItemFromCache: getTradedAsset,
    } = useTradeGroupTradedAssets(tradeGroup.id)

    const {
        data: referenceAssets,
        addItemToCache: addReferenceAsset,
        updateItemInCache: updateReferenceAsset,
        deleteItemInCache: deleteReferenceAsset,
        getItemFromCache: getReferenceAsset,
    } = useTradeGroupReferenceAssets(tradeGroup.id)

    const onSuccessTradedAsset = (data: TradeGroupTradedAsset) => {
        if (data?._tempId)
            updateTradedAsset(data)
        else
            addTradedAsset(data)
        setTradedAsset(null)
    }

    const onSuccessReferenceAsset = (data: TradeGroupReferenceAsset) => {
        if (data?._tempId)
            updateReferenceAsset(data)
        else
            addReferenceAsset(data)
        setReferenceAsset(null)
    }

    const onSuccessInternal = (data: TradeGroup) => {
        if (onDialogSuccess)
            onDialogSuccess(data, tradedAssets, referenceAssets)
    }

    return (
        <DefaultDialog
            maxWidth="lg"
            fullWidth
            title={title}
            minHeight={500}
            formId='trade-group-form'
            dialogContent={<>
                <Tabs value={tab} onChange={(_, value) => setTab(value)}>
                    <Tab label="Основные" />
                    <Tab label="Торговые инструменты" />
                    <Tab label="Опорные инструменты" />
                </Tabs>
                <TabPanel value={tab} index={0} prefix="trade-group" persist>
                    <TradeGroupForm
                        formId='trade-group-form'
                        tradeGroup={tradeGroup ?? {}}
                        onSuccess={onSuccessInternal}
                        {...formProps} />
                </TabPanel>
                <TabPanel value={tab} index={1} prefix="trade-group" persist>
                    <TradeGroupTradedAssetTable
                        rows={tradedAssets ?? []}
                        onAdd={() => setTradedAsset({})}
                        onDelete={(id) => deleteTradedAsset(id)}
                        onEdit={(id) => setTradedAsset(getTradedAsset(id)!)}
                    />
                    <TradeGroupTradedAssetDialog
                        open={Boolean(tradedAsset)}
                        tradeGroupTradedAsset={tradedAsset ?? {}}
                        onCancel={() => setTradedAsset(null)}
                        formProps={{
                            onSuccess: onSuccessTradedAsset
                        }}
                    />
                </TabPanel>
                <TabPanel value={tab} index={2} prefix="trade-group" persist>
                    <TradeGroupReferenceAssetTable
                        rows={referenceAssets ?? []}
                        onAdd={() => setReferenceAsset({ priceOffset: 0, lineColor: "#000000", lineWidth: 1, enabled: true, panelNum: 1 })}
                        onDelete={(id) => deleteReferenceAsset(id)}
                        onEdit={(id) => setReferenceAsset(getReferenceAsset(id)!)}
                    />
                    <TradeGroupReferenceAssetDialog
                        open={Boolean(referenceAsset)}
                        tradeGroupReferenceAsset={referenceAsset ?? {}}
                        onCancel={() => setReferenceAsset(null)}
                        formProps={{
                            onSuccess: onSuccessReferenceAsset
                        }}
                    />
                </TabPanel>
            </>}
            {...props}
        />
    )
}