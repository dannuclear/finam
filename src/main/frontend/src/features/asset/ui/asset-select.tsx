import { debounce } from "@mui/material";
import type { Asset } from "@shared/api/schema";
import { ServerAutocomplete, type ServerAutocompleteProps } from "@shared/ui";
import { useAssetPage } from "@entities/asset";
import { useMemo, useState } from "react";

export type AssetSelectProps<Multiple extends boolean | undefined> = Omit<ServerAutocompleteProps<Asset, Multiple, false>, "options">

export const AssetSelect = <Multiple extends boolean | undefined>({
    label = "Инструмент",
    placeholder = "Инструмент",
    ...props
}: AssetSelectProps<Multiple>) => {
    const [q, setQ] = useState<string>()
    const { data, isPending } = useAssetPage({ page: 0, size: 10, q })

    const debouncedOnChange = useMemo(() =>
        debounce(async (newValue) => {
            if (newValue && newValue.length > 2) {
                setQ(newValue)
            }
        }, 1000), [])

    return (
        <ServerAutocomplete
            {...props}
            label={label}
            placeholder={placeholder}
            onInputChange={(_, value, reason) => reason === "input" && debouncedOnChange(value)}
            loading={isPending}
            options={data?.content ?? []}
            getOptionLabel={option => option.symbol ?? ""}
        />
    )
}