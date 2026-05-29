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

const PositionTable = ({
    positions,
}: {
    positions: Account["positions"];
}) => {
    return (
        <StyledContainer >
            <StyledTable size="small">
                <TableHead>
                    <TableRow>
                        <TableCell>Symbol</TableCell>
                        <TableCell align="center">Quantity</TableCell>
                        <TableCell align="center">Avg Price</TableCell>
                        <TableCell align="center">Current Price</TableCell>
                        <TableCell align="center">Maint. Margin</TableCell>
                        <TableCell align="center">Daily PnL</TableCell>
                        <TableCell align="center">Unrealized PnL</TableCell>
                    </TableRow>
                </TableHead>

                <TableBody>
                    {positions?.map((row) => (
                        <TableRow key={row.symbol}>
                            <TableCell>{row.symbol}</TableCell>
                            <TableCell align="right">{row.quantity}</TableCell>
                            <TableCell align="right">{row.averagePrice}</TableCell>
                            <TableCell align="right">{row.currentPrice}</TableCell>
                            <TableCell align="right">{row.maintenanceMargin}</TableCell>
                            <TableCell align="right">{row.dailyPnl}</TableCell>
                            <TableCell align="right">{row.unrealizedPnl}</TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </StyledTable>
        </StyledContainer>
    );
};

export default PositionTable;
