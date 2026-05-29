import { Box, Chip, type BoxProps } from '@mui/material'
import { memo, useCallback } from 'react'

export type LegendItem = {
    id: string,
    label: string,
    color: string,
    enabled: boolean
}

export interface LegendProps extends BoxProps {
    options: LegendItem[],
    onOptionClick?: (item: LegendItem) => void
}

const Legend = ({ options, onOptionClick, ...rest }: LegendProps) => {

    const handleClick = useCallback(
        (item: LegendItem) => () => {
            onOptionClick?.(item)
        },
        [onOptionClick]
    )

    return (
        <Box {...rest}>
            {options.map((item) => (
                <Chip
                    key={item.id}
                    label={item.label}
                    size="small"
                    variant={item.enabled ? 'filled' : 'outlined'}
                    onClick={handleClick(item)}
                    sx={{
                        ...(item.enabled && {
                            backgroundColor: item.color
                        }),
                        mr: 1,
                        mt: 1
                    }}
                />
            ))}
        </Box>
    )
}

export default memo(Legend)