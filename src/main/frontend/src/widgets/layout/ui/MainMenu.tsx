import ListItemIcon from "@mui/material/ListItemIcon"
import ListItemText from "@mui/material/ListItemText"
import type { MenuProps } from "@mui/material/Menu"
import Menu from "@mui/material/Menu"
import MenuItem from "@mui/material/MenuItem"
import type { ROUTES } from "@shared/routes"
import { DefaultIcon } from "@shared/ui/DefaultIcon"
import { type MouseEvent } from 'react'
import { Link, useNavigate } from "react-router"

export type MainMenuItem =
    | {
        label: string
        icon: string
        navigate: (typeof ROUTES)[keyof typeof ROUTES]
        redirect?: never,
        items?: MainMenuItem[],
        roles?: string[]
    }
    | {
        label: string
        icon: string
        redirect: string
        navigate?: (typeof ROUTES)[keyof typeof ROUTES],
        items?: MainMenuItem[],
        roles?: string[]
    }

export interface MainMenuProps extends MenuProps {
    items?: MainMenuItem[]
    onNavigate?: (path: string, event: MouseEvent<HTMLElement>) => void
}

const MainMenu = ({ items, anchorEl, ...props }: Omit<MainMenuProps, "open">) => {
    const navigate = useNavigate()
    return (
        <Menu
            {...props}
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}>

            {items?.map((item, idx) => {
                const content = (
                    <>
                        <ListItemIcon>
                            <DefaultIcon iconStyle="fa-light fa-lg" iconName={item.icon} />
                        </ListItemIcon>
                        <ListItemText primary={item.label} />
                    </>
                )

                if ('navigate' in item) {
                    return (
                        <MenuItem
                            key={idx}
                            onClick={(e) => (navigate(item.navigate!), props.onClose?.(e, "backdropClick"))}
                        >
                            {content}
                        </MenuItem>
                    )
                }

                return (
                    <MenuItem
                        key={idx}
                        component={Link}
                        to={item.redirect}
                    >
                        {content}
                    </MenuItem>
                )
            })}
        </Menu>
    )
}

export default MainMenu