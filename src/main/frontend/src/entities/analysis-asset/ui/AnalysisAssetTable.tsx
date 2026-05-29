import { Button, IconButton, Toolbar } from "@mui/material"
import Table from "@mui/material/Table"
import TableBody from "@mui/material/TableBody"
import TableCell from "@mui/material/TableCell"
import TableContainer from "@mui/material/TableContainer"
import TableHead from "@mui/material/TableHead"
import TableRow from "@mui/material/TableRow"
import type { AnalysisAsset } from "@shared/api/schema"
import { DefaultIcon } from "@shared/ui"

export type AnalysisAssetTableProps = {
    rows: AnalysisAsset[],
    onAdd?: () => void,
    onEdit?: (id: string) => void,
    onDelete?: (id: string) => void
}

export const AnalysisAssetTable = ({ rows, onAdd, onEdit, onDelete }: AnalysisAssetTableProps) => {
    return (<>
        <Toolbar variant="dense" sx={{ p: 1, minHeight: 10 }} disableGutters >
            <Button onClick={onAdd}>Добавить</Button>
        </Toolbar>
        <TableContainer>
            <Table size="small" aria-label="a dense table">
                <TableHead>
                    <TableRow>
                        <TableCell>Инструмент</TableCell>
                        <TableCell width={1}>Включен</TableCell>
                        <TableCell width={1}>Цвет</TableCell>
                        <TableCell width={1}>Толщина</TableCell>
                        <TableCell width={1}>Панель</TableCell>
                        <TableCell width={1}></TableCell>
                        <TableCell width={1}></TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {rows.filter(row => row.changeStatus !== "DELETED").map((row) => (
                        <TableRow key={row._tempId}>
                            <TableCell component="th" scope="row">{row.asset?.symbol}</TableCell>
                            <TableCell component="th" scope="row">{row.enabled ? "Вкл" : "Откл"}</TableCell>
                            <TableCell component="th" scope="row">
                                <div style={{ backgroundColor: row.lineColor, width: 40, height: 20, borderRadius: 4 }}> </div>
                            </TableCell>

                            <TableCell component="th" scope="row">{row.lineWidth}</TableCell>
                            <TableCell component="th" scope="row">{row.panelIndex}</TableCell>
                            <TableCell component="th" scope="row">
                                <IconButton size="small" onClick={() => onEdit && row._tempId && onEdit(row._tempId)}>
                                    <DefaultIcon iconName='fa-pencil' />
                                </IconButton>
                            </TableCell>
                            <TableCell component="th" scope="row">
                                <IconButton size="small" onClick={() => onDelete && row._tempId && onDelete(row._tempId)}>
                                    <DefaultIcon color="#ff0000" iconName='fa-trash' />
                                </IconButton>
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
    </>
    )
}
