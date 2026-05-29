import { styled } from "@mui/material/styles";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell, { tableCellClasses } from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import type { TradeHistory } from "@shared/api/schema";
import dayjs from "dayjs";

/* ---------- STYLES ---------- */

const StyledContainer = styled(TableContainer)(({ theme }) => ({
    border: `1px solid ${theme.palette.divider}`, // рамка вокруг таблицы
}));

const StyledTable = styled(Table)(({ theme }) => ({
    borderCollapse: "collapse",
    minWidth: 650,

    [`& .${tableCellClasses.root}`]: {
        padding: theme.spacing(0.5, 1),
        fontSize: 12,
        borderRight: `1px solid ${theme.palette.divider}`, // вертикальные линии
        borderBottom: `1px solid ${theme.palette.divider}`, // горизонтальные линии
    },

    [`& .${tableCellClasses.head}`]: {
        fontWeight: 600,
        backgroundColor: theme.palette.action.hover,
    },

    // убрать правую границу у последней колонки
    [`& .${tableCellClasses.root}:last-of-type`]: {
        borderRight: "none",
    },

    // убрать нижнюю границу у последней строки
    "& tbody tr:last-of-type td": {
        borderBottom: "none",
    },
}));

/* ---------- COMPONENT ---------- */

const TradesTable = ({
    trades,
}: {
    trades: TradeHistory["trades"];
}) => {
    return (
        <StyledContainer >
            <StyledTable size="small">
                <TableHead>
                    <TableRow>
                        <TableCell>Инструмент</TableCell>
                        <TableCell>Операция</TableCell>
                        <TableCell align="center">Дата</TableCell>
                        <TableCell align="center">Количество</TableCell>
                        <TableCell align="center">Цена</TableCell>
                        <TableCell align="center">Сумма</TableCell>
                        <TableCell align="center">Комментарий</TableCell>
                    </TableRow>
                </TableHead>

                <TableBody>
                    {trades?.map((row) => (
                        <TableRow key={row.tradeId}>
                            <TableCell>{row.symbol}</TableCell>
                            <TableCell>{row.side}</TableCell>
                            <TableCell align="right">{dayjs(row.timestamp).format('DD.MM.YYYY HH.mm.ss')}</TableCell>
                            <TableCell align="right">{row.size}</TableCell>
                            <TableCell align="right">{row.price}</TableCell>
                            <TableCell align="right">{row.sum}</TableCell>
                            <TableCell align="right">{row.comment}</TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </StyledTable>
        </StyledContainer>
    );
};

export default TradesTable;
