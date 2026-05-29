import { debounce } from '@mui/material';
import React, { useEffect, useMemo } from 'react';
import { IdWithNameAutocomplete, type IdWithNameAutocompleteProps } from './IdWithNameAutocomplete';

export type DebouncedAutocompleteProps<
    Value,
    Multiple extends boolean | undefined,
    FreeSolo extends boolean | undefined,
    ChipComponent extends React.ElementType = "div"> = IdWithNameAutocompleteProps<Value, Multiple, FreeSolo, ChipComponent> & {
        delay?: number,
        onDebounced?: (value: string) => void
    }

export const DebouncedAutocomplete = <
    Value extends object,
    Multiple extends boolean | undefined = false,
    FreeSolo extends boolean | undefined = false,
    ChipComponent extends React.ElementType = "div">({
        onDebounced,
        delay = 1000,
        ...rest
    }: DebouncedAutocompleteProps<Value, Multiple, FreeSolo, ChipComponent>) => {

    const debouncedOnChange = useMemo(() =>
        debounce(async (newValue) => {
            if (newValue && newValue.length > 2 && onDebounced) {
                onDebounced(newValue)
            }
        }, delay), [delay, onDebounced])

    useEffect(() => {
        return () => {
            debouncedOnChange.clear();
        };
    }, [debouncedOnChange]);

    return (
        <IdWithNameAutocomplete
            onInputChange={(_, value, reason) => reason === "input" && debouncedOnChange(value)}
            {...rest}
        />
    )
}
