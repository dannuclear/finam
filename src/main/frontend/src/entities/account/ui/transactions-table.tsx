import { styled } from "@mui/material/styles";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell, { tableCellClasses } from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import type { Transaction } from "@shared/api/schema";
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

const TransactionsTable = ({
    transactions,
}: {
    transactions: Transaction[];
}) => {
    return (
        <StyledContainer >
            <StyledTable size="small">
                <TableHead>
                    <TableRow>
                        <TableCell>Инструмент</TableCell>
                        <TableCell>Тип</TableCell>
                        <TableCell align="center">Дата</TableCell>
                        <TableCell align="center">Количество</TableCell>
                        <TableCell align="center">Цена</TableCell>
                        <TableCell align="center">Изменение количества</TableCell>
                        <TableCell align="center">Наименование</TableCell>
                    </TableRow>
                </TableHead>

                <TableBody>
                    {transactions?.map((row) => (
                        <TableRow key={row.id}>
                            <TableCell>{row.symbol}</TableCell>
                            <TableCell>{row.transactionCategory}</TableCell>
                            <TableCell align="right">{dayjs(row.timestamp).format('DD.MM.YYYY HH.mm.ss')}</TableCell>
                            <TableCell align="right">{row.trade?.size}</TableCell>
                            <TableCell align="right">{row.trade?.price}</TableCell>
                            <TableCell align="right">{row.changeQty}</TableCell>
                            <TableCell align="right">{row.transactionName}</TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </StyledTable>
        </StyledContainer>
    );
};

export default TransactionsTable;
