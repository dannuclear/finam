import { StrategyUpsertDialog } from "@features/strategy"
import Grid from "@mui/material/Grid"
import type { GridRowId } from "@mui/x-data-grid"
import { StrategyTable } from "@widgets/strategy"
import { useState } from "react"

const StrategiesPage = () => {
    const [strategyId, setStrategyId] = useState<GridRowId | 'NEW' | null>(null)

    return (<>
        <Grid container spacing={1} sx={{ pt: 1 }}>
            <Grid size={12}>
                <StrategyTable
                    onAdd={() => setStrategyId("NEW")}
                    onEdit={(id) => setStrategyId(id)} />
            </Grid>
        </Grid>

        <StrategyUpsertDialog
            strategyId={Number(strategyId)}
            open={!!strategyId}
            onCancel={() => setStrategyId(null)}
            onDialogSuccess={() => setStrategyId(null)}
        />
    </>)
}

export { StrategiesPage as Component }

