import { Box } from '@mui/material';
import React from 'react';

export interface TabPanelProps {
    children?: React.ReactNode;
    prefix: string
    index: number;
    value: number;
    persist?: boolean
}

export const TabPanel = ({ index, value, prefix, children, persist = false, ...props }: TabPanelProps) => {
    return (
        <div
            role='tabpanel'
            hidden={value != index}
            id={`${prefix}-tabpanel-${index}`}
            aria-labelledby={`${prefix}-tab-${index}`}
            {...props}
        >{(persist || value === index) && <Box sx={{ pt: 2 }}>{children}</Box>}</div>
    )
}
