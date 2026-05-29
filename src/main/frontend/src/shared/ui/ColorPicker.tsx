import { Controller, type ControllerProps } from 'react-hook-form';

const ColorPicker = ({ ...props }: Omit<ControllerProps, "render">) => {
    return (
        <Controller
            defaultValue="#000000"
            render={({ field: { onChange, value } }) =>
                <input
                    type="color"
                    value={value ?? ""}
                    onChange={(event) => {
                        onChange(event.target.value)
                        console.log(event.target.value);

                    }
                    }
                    style={{
                        height: "100%",
                        width: "100%"
                    }}
                />
            }
            {...props}
        />
    )
}

export default ColorPicker