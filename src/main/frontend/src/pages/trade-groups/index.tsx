import Grid from "@mui/material/Grid"
import { fetchClient, rqClient } from "@shared/api/instance"
import { queryClient } from "@shared/api/query-client"
import type { TradeGroup, TradeGroupReferenceAsset, TradeGroupTradedAsset } from "@shared/api/schema"
import { useDialogControl } from "@shared/model/use-dialog-control"
import { PeriodDialog, type PeriodDialogProps } from "@shared/ui"
import { TradeGroupDialog, TradeGroupQuotesDialog, TradeGroupTable } from "@widgets/trade-group"
import { useCreateTradeGroup, useTradeGroup } from "@entities/trade-group"
import { useUpdateTradeGroup } from "@entities/trade-group/api/use-update-trade-group"
import { useState } from "react"

const TradeGroupListPage = () => {
    const dialog = useDialogControl()
    const { data } = useTradeGroup(dialog.id)
    const { mutate: create, isPending: isCreatePending } = useCreateTradeGroup()
    const { mutate: update, isPending: isUpdatePending } = useUpdateTradeGroup()
    const periodDialog = useDialogControl()

    const [tradeGroupId, setTradedGroupId] = useState<number | null>(null)

    const onSuccess = () => {
        dialog.close()
        queryClient.invalidateQueries(rqClient.queryOptions("get", "/api/v1/trade-groups"))
    }

    const onPeriodOk: PeriodDialogProps["onOk"] = (startTime, endTime) => {
        if (startTime == null || endTime == null || periodDialog.id == null)
            return;

        fetchClient.POST("/api/v1/trade-groups/{id}/calculate-reference-offsets",
            {
                params: {
                    path: {
                        id: periodDialog.id
                    },
                    query: {
                        timeFrame: 'TIME_FRAME_D',
                        startTime: startTime.format(),
                        endTime: endTime.format(),
                    }
                }
            }).then(() => periodDialog.close())
    }

    const onDialogSuccess = (data: TradeGroup, tradedAssets?: TradeGroupTradedAsset[], referenceAssets?: TradeGroupReferenceAsset[]) => {
        if (typeof dialog.id === 'number') {
            update({
                params: {
                    path: {
                        id: dialog.id
                    }
                }, body: { ...data, tradedAssets: tradedAssets, referenceAssets: referenceAssets }
            },
                {
                    onSuccess: onSuccess
                }
            )
        } else {
            create({
                body: { ...data, tradedAssets: tradedAssets, referenceAssets: referenceAssets }
            }, {
                onSuccess: onSuccess
            })
        }
    }

    return (<>
        <Grid container spacing={1} sx={{ pt: 1 }}>
            <Grid size={12}>
                <TradeGroupTable
                    onAdd={dialog.prepare}
                    onEdit={dialog.edit}
                    onShowQuotes={(id) => setTradedGroupId(id as number)}
                    onCalcOffsets={periodDialog.edit}
                />
            </Grid>
        </Grid>

        <TradeGroupDialog
            key={dialog.id}
            tradeGroup={data ?? {}}
            open={dialog.open}
            onSave={() => { }}
            onCancel={dialog.close}
            isPending={isCreatePending || isUpdatePending}
            onDialogSuccess={onDialogSuccess}
        />

        <TradeGroupQuotesDialog
            tradedGroupId={tradeGroupId}
            open={Boolean(tradeGroupId)}
            onOk={() => setTradedGroupId(null)}
        />

        <PeriodDialog
            open={periodDialog.open}
            onOk={onPeriodOk}
        />
    </>)
}

export { TradeGroupListPage as Component }

