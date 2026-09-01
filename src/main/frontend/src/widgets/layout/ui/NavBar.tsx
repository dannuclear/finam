import logo from "@assets/logo.svg"
import KeyboardArrowDownIcon from "@mui/icons-material/KeyboardArrowDown"
import { AppBar, Box, Button, IconButton, Toolbar } from "@mui/material"
import { ROUTES } from "@shared/routes"
import { DefaultIcon } from "@shared/ui"
import { TokenInfoWidget } from "@widgets/token"
import React, { useState } from 'react'
import { useNavigate } from "react-router"
import MainMenu, { type MainMenuItem } from "./MainMenu"

const items: MainMenuItem[] = [
    {
        label: 'Счета',
        icon: 'fa-rectangle-history-circle-user',
        navigate: ROUTES.ACCOUNT,
        roles: [],
    },
    {
        label: 'Токен',
        icon: 'fa-receipt',
        navigate: ROUTES.TOKEN_DETAILS,
        roles: [],
    },
    {
        label: 'Инструменты',
        icon: 'fa-chart-mixed-up-circle-dollar',
        navigate: ROUTES.ASSETS,
        roles: [],
    },
    {
        label: 'Торговые наборы',
        icon: 'fa-toolbox',
        navigate: ROUTES.TRADE_GROUPS,
        roles: [],
    },
    {
        label: 'Стратегии',
        icon: 'fa-chess',
        redirect: "#",
        roles: [],
        items: [
            {
                label: 'Список',
                icon: 'fa-list-radio',
                navigate: ROUTES.STRATEGIES,
                roles: [],
            },
            {
                label: 'Тестирование',
                icon: 'fa-flask-gear',
                navigate: ROUTES.BACKTEST,
                roles: [],
            },
        ]
    },
    {
        label: 'Аналитика',
        icon: 'fa-calculator',
        redirect: "#",
        roles: [],
        items: [
            {
                label: 'Относительные спреды',
                icon: 'fa-chart-line-up-down',
                navigate: ROUTES.ANALYSIS_RELATIVE_SPREADS,
                roles: [],
            },
            {
                label: 'Волатильность',
                icon: 'fa-chart-line-up-down',
                navigate: ROUTES.ANALYSIS_VOLATILITY,
                roles: [],
            },
        ]
    },
    {
        label: 'Торговля',
        icon: 'fa-display-chart-up-circle-dollar',
        redirect: "#",
        roles: [],
        items: [
            {
                label: 'Спреды',
                icon: 'fa-arrows-from-dotted-line',
                navigate: ROUTES.TRADING_SPREADS,
                roles: [],
            },
        ]
    },
]

type NavButtonProps = {
    label: string,
    icon: string,
    onClick: React.MouseEventHandler<HTMLButtonElement>,
    arrowDownIcon: boolean
}

const NavButton = ({
    label,
    icon,
    onClick,
    arrowDownIcon }: NavButtonProps) => {
    return (
        <Button
            startIcon={<DefaultIcon iconName={icon} />}
            onClick={onClick}
            sx={{ color: 'inherit' }}
            endIcon={arrowDownIcon && <KeyboardArrowDownIcon />}
        >{label}</Button>
    )
}

export const NavBar = () => {
    const navigate = useNavigate()
    const [anchorEl, setAnchorEl] = useState<HTMLElement>()
    const [menuItems, setMenuItems] = useState<MainMenuItem[]>()

    const onNavClick = (item: MainMenuItem, event: React.MouseEvent<HTMLElement>) => {
        if (item.navigate) {
            navigate(item.navigate);
            setAnchorEl(undefined);
        }
        else {
            setAnchorEl(event.currentTarget);
            setMenuItems(item.items);
        }
    }

    return (
        <>
            <AppBar position='static'>
                <Toolbar variant='dense'>
                    <IconButton
                        size='large'
                        edge='start'
                        color='inherit' sx={{ mr: 2 }} onClick={() => navigate(ROUTES.ROOT)}>
                        <img src={logo} width={25} ></img>
                    </IconButton>

                    <Box sx={{ flexGrow: 1 }}>
                        {
                            items.map((item, idx) => <NavButton
                                key={idx}
                                onClick={(e) => onNavClick(item, e)}
                                arrowDownIcon={Boolean(item.items?.length)}
                                label={item.label}
                                icon={item.icon} />)
                        }
                    </Box>

                    <Box sx={{ textAlign: "end" }}>
                        <React.Suspense fallback="Loading">
                            <TokenInfoWidget />
                        </React.Suspense>
                    </Box>
                </Toolbar>
            </AppBar>
            <MainMenu
                anchorEl={anchorEl}
                items={menuItems}
                onClose={() => setAnchorEl(undefined)} />
        </>
    )
}
