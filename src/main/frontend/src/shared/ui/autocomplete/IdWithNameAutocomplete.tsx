import { Autocomplete, TextField, type AutocompleteFreeSoloValueMapping, type AutocompleteProps } from '@mui/material';
import React from 'react';

export type IdWithNameAutocompleteProps<
    Value,
    Multiple extends boolean | undefined,
    FreeSolo extends boolean | undefined,
    ChipComponent extends React.ElementType = "div"> = Omit<AutocompleteProps<Value, Multiple, false, FreeSolo, ChipComponent>, "renderInput"> & {
        label?: string;
        placeholder?: string
    }

export const IdWithNameAutocomplete = <
    Value extends object,
    Multiple extends boolean | undefined = false,
    FreeSolo extends boolean | undefined = false,
    ChipComponent extends React.ElementType = "div">({
        label,
        placeholder = label,
        getOptionLabel,
        getOptionKey,
        isOptionEqualToValue,
        ...rest
    }: IdWithNameAutocompleteProps<Value, Multiple, FreeSolo, ChipComponent>) => {

    const defaultGetOptionLabel = (option: Value | AutocompleteFreeSoloValueMapping<FreeSolo>) => {
        if (typeof option === "string")
            return option
        else if ("name" in option && typeof option.name === "string") {
            return option.name;
        }
        return "";
    };

    const defaultGetOptionKey = (option: Value): React.Key => {
        if ("id" in option && (typeof option.id === "string" || typeof option.id === "number")) {
            return option.id;
        }
        if ("name" in option && typeof option.name === "string") {
            return option.name;
        }
        return JSON.stringify(option);
    };


    const defaultIsEqual = (option: Value, value: Value) => {
        if (
            "id" in option &&
            "id" in value &&
            (typeof option.id === "string" || typeof option.id === "number") &&
            (typeof value.id === "string" || typeof value.id === "number")
        ) {
            return option.id === value.id;
        }
        return option === value;
    };

    return (
        <Autocomplete
            renderInput={(params) =>
                <TextField
                    {...params}
                    label={label}
                    placeholder={placeholder}
                />}

            renderOption={(props, option) => (
                <li {...props} key={(getOptionKey ?? defaultGetOptionKey)(option)}>
                    {(getOptionLabel ?? defaultGetOptionLabel)(option)}
                </li>
            )}

            getOptionLabel={(getOptionLabel ?? defaultGetOptionLabel)}
            isOptionEqualToValue={
                isOptionEqualToValue ?? defaultIsEqual
            }

            // Отключаем локальный фильтр
            filterOptions={(x) => x}

            {...rest}
        />
    )
}
