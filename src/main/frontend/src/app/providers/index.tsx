import { type Theme, ThemeProvider } from '@mui/material/styles'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'
import 'dayjs/locale/ru'
import { RouterProvider, type RouterProviderProps } from 'react-router'
import { Toasts } from '../message'

type Props = {
    router: RouterProviderProps['router'],
    theme: Theme,
    queryClient: QueryClient
}

export function Providers({ router, theme, queryClient }: Readonly<Props>) {
    return (
        <ThemeProvider theme={theme}>
            <QueryClientProvider client={queryClient}>
                <LocalizationProvider dateAdapter={AdapterDayjs} adapterLocale='ru'>
                    <RouterProvider router={router} />
                    <Toasts />
                    <ReactQueryDevtools />
                </LocalizationProvider>
            </QueryClientProvider>
        </ThemeProvider >
    );
}
