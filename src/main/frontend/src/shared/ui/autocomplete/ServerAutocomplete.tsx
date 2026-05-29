import { Autocomplete, TextField, type AutocompleteProps, type AutocompleteRenderInputParams } from '@mui/material';

export type IdWithName = {
    id?: number | undefined,
    name?: string | undefined
}

export interface ServerAutocompleteProps<Value, Multiple extends boolean | undefined, FreeSolo extends boolean | undefined, ChipComponent extends React.ElementType = "div"> extends Omit<AutocompleteProps<Value, Multiple, FreeSolo, false, ChipComponent>, "renderInput"> {
    label?: string,
    placeholder?: string,
    renderInput?: (params: AutocompleteRenderInputParams) => React.ReactNode;
    getOptionLabel?: (option: Value) => string,
    isOptionEqualToValue?: (option: Value, value: Value) => boolean
}

export default function ServerAutocomplete<Value extends object, Multiple extends boolean | undefined = false, FreeSolo extends boolean | undefined = false, ChipComponent extends React.ElementType = "div">({
    label,
    placeholder,
    getOptionLabel,
    getOptionKey,
    isOptionEqualToValue,
    ...props
}: ServerAutocompleteProps<Value, Multiple, FreeSolo, ChipComponent>) {

    const defaultGetOptionLabel = (option: Value) => {
        if ("name" in option && typeof option.name === "string") {
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
            {...props}
        />
    )
}
