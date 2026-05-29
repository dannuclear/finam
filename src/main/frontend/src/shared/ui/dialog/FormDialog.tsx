import { Button } from "@mui/material";
import { PendingDialog, type PendingDialogProps } from "./PendingDialog";

export interface FormDialogProps extends PendingDialogProps {
    formId: string;
    onCancel?: () => void;
    showSave?: boolean;
}

export const FormDialog = ({
    formId,
    onCancel,
    showSave = true,
    ...rest }: FormDialogProps) => {
    return (
        <PendingDialog
            dialogActions={
                <>
                    {showSave && formId && <Button form={formId} type="submit" color="error">Сохранить</Button>}
                    {onCancel && <Button color="success" onClick={onCancel}>Отмена</Button>}
                </>
            }
            {...rest}
        />
    )
}
