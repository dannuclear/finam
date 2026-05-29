import { theme } from "@app/styles";
import { Button, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, type DialogProps } from "@mui/material";
import { styled } from "@mui/material/styles";
import type { ReactNode } from "react";

const CustomizedDialog = styled(Dialog)(({ theme }) => ({
    '& .MuiDialogTitle-root': {
        padding: theme.spacing(1),
        // background: theme.palette.primary.main,
        color: 'white'
    },
    '& .MuiDialogContent-root': {
        padding: `${theme.spacing(1)}!important`,
    },
    '& .MuiDialogActions-root': {
        padding: theme.spacing(1),
        // background: theme.palette.primary.main,
        minHeight: 33,
        '& .MuiButtonBase-root': {
            color: 'white',
            border: '2px solid rgba(255, 255, 255, 0.5)',
        }
    },
}));

export interface DefaultDialogProps extends Omit<DialogProps, 'onError'> {
    formId?: string;
    dialogContent: ReactNode;
    onCancel?: () => void;
    onOk?: () => void;
    onSave?: () => void;
    isPending?: boolean;
    minHeight?: number;
    background?: string
}

const DefaultDialog = ({
    formId,
    title,
    dialogContent,
    onCancel,
    onOk,
    onSave,
    isPending,
    minHeight,
    background = theme.palette.primary.main,
    ...props }: DefaultDialogProps) => {
    return (
        <CustomizedDialog {...props}>
            <DialogTitle sx={{ background: background }}>{title}</DialogTitle>
            <DialogContent dividers sx={{ minHeight: minHeight }}>
                {dialogContent}
            </DialogContent>
            <DialogActions sx={{ background: background }}>
                {isPending ? (
                    <CircularProgress color="inherit" size={30} />
                ) : (
                    <>
                        {onOk && <Button color="error" onClick={onOk}>Ok</Button>}
                        {onSave && <Button form={formId} type={formId ? "submit" : "button"} color="error" onClick={onSave}>Сохранить</Button>}
                        {onCancel && <Button color="success" onClick={onCancel}>Отмена</Button>}
                    </>
                )}
            </DialogActions>
        </CustomizedDialog>
    );
};

export default DefaultDialog;
