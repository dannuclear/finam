import { theme } from '@app/styles';
import { CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, type DialogProps } from '@mui/material';
import type { ReactNode } from 'react';

export interface PendingDialogProps extends DialogProps {
    title?: string
    dialogContent?: ReactNode;
    dialogActions?: ReactNode;
    pending?: boolean;
    minHeight?: number;
    background?: string
}

export const PendingDialog = ({
    title,
    dialogContent,
    dialogActions,
    pending,
    minHeight,
    background = theme.palette.primary.main,
    ...props
}: PendingDialogProps) => {
    return (
        <Dialog {...props}>
            <DialogTitle sx={{ background }}>
                {title}
            </DialogTitle>

            <DialogContent dividers sx={{ minHeight }}>
                {dialogContent}
            </DialogContent>

            <DialogActions sx={{ background }}>
                {pending
                    ? <CircularProgress color="inherit" size={30} />
                    : dialogActions
                }
            </DialogActions>
        </Dialog>
    )
}
