import type { Property } from "csstype"

export const DefaultIcon = ({ iconName, iconStyle = 'fa-duotone ', color }: { iconName: string, iconStyle?: string, color?: Property.Color }) => {
    return (
        <i className={`${iconStyle} ${iconName}`} style={{ color }}></i>
    )
}
