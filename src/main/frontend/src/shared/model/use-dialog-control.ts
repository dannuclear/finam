import type { GridRowId } from "@mui/x-data-grid/models"
import type { NumberOrNew } from "@shared/api/schema"
import { useState } from "react"

export const useDialogControl = () => {
    const [idOrNew, setIdOrNew] = useState<NumberOrNew>()

    const edit = (id: number | GridRowId) => {
        if (typeof id === "number")
            setIdOrNew(id)
    }

    const prepare = () => setIdOrNew("new")

    const close = () => setIdOrNew(undefined)

    return {
        open: Boolean(idOrNew),
        edit,
        prepare,
        id: typeof idOrNew === "number" ? idOrNew : undefined,
        close
    }
}