import { Controller } from "react-hook-form";
import { AssetSelect, type AssetSelectProps } from "./asset-select";

export type AssetSelectElementProps<Multiple extends boolean | undefined> = AssetSelectProps<Multiple> & { name: string }

export const AssetSelectElement = <Multiple extends boolean | undefined>({ name, ...props }: AssetSelectElementProps<Multiple>) => {
    return (
        <Controller
            name={name}
            render={({ field: { onChange, value } }) =>
                <AssetSelect
                    {...props}
                    onChange={(_, val) => onChange(val)}
                    value={value ?? {}}
                />}
        />
    )
}