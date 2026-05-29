import { useAnalysisPage } from "@entities/analysis";
import type { Analysis } from "@shared/api/schema";
import { DebouncedAutocomplete, type DebouncedAutocompleteProps } from "@shared/ui";
import { useState } from "react";

export type AnalysisSelectProps<Multiple extends boolean | undefined> = Omit<DebouncedAutocompleteProps<Analysis, Multiple, false>, "options">

export const AnalysisSelect = <Multiple extends boolean | undefined = false>({
    label = "Анализ",
    ...rest
}: AnalysisSelectProps<Multiple>) => {
    const [q, setQ] = useState<string>()
    const { data, isPending } = useAnalysisPage({ page: 0, size: 10, q })

    return (<DebouncedAutocomplete
        label={label}
        loading={isPending}
        options={data?.content ?? []}
        onDebounced={value => setQ(value)}

        {...rest}
    />)
}