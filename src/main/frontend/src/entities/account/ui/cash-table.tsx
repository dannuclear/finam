import { styled } from "@mui/material/styles";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell, { tableCellClasses } from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import type { Account } from "@shared/api/schema";

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

const CashTable = ({
    cash,
}: {
    cash: Account["cash"];
}) => {
    return (
        <StyledContainer >
            <StyledTable size="small">
                <TableHead>
                    <TableRow>
                        <TableCell>currencyCode</TableCell>
                        <TableCell align="center">nanos</TableCell>
                        <TableCell align="center">units</TableCell>
                    </TableRow>
                </TableHead>

                <TableBody>
                    {cash?.map((row) => (
                        <TableRow key={row.currencyCode}>
                            <TableCell>{row.currencyCode}</TableCell>
                            <TableCell align="right">{row.nanos}</TableCell>
                            <TableCell align="right">{row.units}</TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </StyledTable>
        </StyledContainer>
    );
};

export default CashTable;
