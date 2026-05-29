import CancelIcon from '@mui/icons-material/Cancel';
import SearchIcon from '@mui/icons-material/Search';
import { Box, Button, InputAdornment, TextField } from '@mui/material';
import { type GridSlotsComponentsProps, QuickFilter, QuickFilterClear, QuickFilterControl, Toolbar } from '@mui/x-data-grid';
import type { MouseEventHandler } from 'react';

declare module '@mui/x-data-grid' {
    interface ToolbarPropsOverrides {
        onAdd?: MouseEventHandler,
        onExcel?: MouseEventHandler,
        labelAdd: string
    }
}

export const DefaultGridToolbar = ({ onAdd, labelAdd = 'Создать' }: NonNullable<GridSlotsComponentsProps['toolbar']>) => {
    return (
        <Toolbar>
            {onAdd && <Button size='medium' onClick={onAdd}>{labelAdd}</Button>}
            <Box sx={{ flex: 1 }} />
            {/* <Button size='medium' onClick={onExcel}>Excel</Button> */}
            <QuickFilter expanded>
                <QuickFilterControl
                    render={({ ref, ...other }) => (
                        <TextField
                            {...other}
                            sx={{ width: 360 }}
                            inputRef={ref}
                            aria-label="Search"
                            placeholder="Поиск..."
                            size="small"
                            slotProps={{
                                input: {
                                    startAdornment: (
                                        <InputAdornment position="start">
                                            <SearchIcon fontSize="small" />
                                        </InputAdornment>
                                    ),
                                    endAdornment: other.value ? (
                                        <InputAdornment position="end">
                                            <QuickFilterClear
                                                edge="end"
                                                size="small"
                                                aria-label="Clear search"
                                                material={{ sx: { marginRight: -0.75 } }}
                                            >
                                                <CancelIcon fontSize="small" />
                                            </QuickFilterClear>
                                        </InputAdornment>
                                    ) : null,
                                    ...other.slotProps?.input,
                                },
                                ...other.slotProps,
                            }}
                        />
                    )}
                />
            </QuickFilter>
        </Toolbar >
    )
}
