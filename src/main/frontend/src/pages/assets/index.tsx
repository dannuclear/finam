import Grid from "@mui/material/Grid"
import type { GridRowId } from "@mui/x-data-grid"
import { AssetTable } from "@widgets/asset"
import { QuoteChartDialog } from "@widgets/quote"
import { useState } from "react"

const AssetListPage = () => {
    const [symbol, setSymbol] = useState<GridRowId | null>(null)

    return (<>
        <Grid container spacing={1} sx={{ pt: 1 }}>
            <Grid size={12}>
                <AssetTable onSubscribe={id => setSymbol(id)} />
            </Grid>
        </Grid>

        <QuoteChartDialog
            symbol={symbol as string}
            open={!!symbol}
            onOk={() => setSymbol(null)}
        />
    </>)
}

export { AssetListPage as Component }

