import type { Strategy } from "@shared/api/schema";
import { DebouncedAutocomplete, type DebouncedAutocompleteProps } from "@shared/ui";
import { useState } from "react";
import { useStrategies } from "../model/use-strategies";

export type StrategySelectProps<Multiple extends boolean | undefined> = Omit<DebouncedAutocompleteProps<Strategy, Multiple, false>, "options">

export const StrategySelect = <Multiple extends boolean | undefined = false>({
    label = "Стратегия",
    ...rest
}: StrategySelectProps<Multiple>) => {
    const [q, setQ] = useState<string>()
    const { data, isPending } = useStrategies({ page: 0, size: 10, q })

    return (<DebouncedAutocomplete
        label={label}
        loading={isPending}
        options={data?.content ?? []}
        onDebounced={value => setQ(value)}

        {...rest}
    />)
}