import App from '@app/App'
import { ROUTES } from '@shared/routes'
import { createBrowserRouter, redirect } from 'react-router'
import { authLoader } from './authLoader'

const router = createBrowserRouter([
  // {
  //   path: ROUTES.LOGIN,
  //   lazy: () => import("@pages/login")
  // },
  {
    element: <App />,
    loader: authLoader,
    children: [
      {
        path: ROUTES.ROOT,
        loader: () => redirect(ROUTES.ACCOUNT)
      },
      {
        path: ROUTES.ACCOUNT,
        lazy: () => import("@pages/account")
      },
      {
        path: ROUTES.TOKEN_DETAILS,
        lazy: () => import("@pages/token-details")
      },
      {
        path: ROUTES.ASSETS,
        lazy: () => import("@pages/assets")
      },
      {
        path: ROUTES.TRADE_GROUPS,
        lazy: () => import("@pages/trade-groups")
      },
      {
        path: ROUTES.STRATEGIES,
        lazy: () => import("@pages/strategies")
      },
      {
        path: ROUTES.BACKTEST,
        lazy: () => import("@pages/backtest")
      },
      {
        path: ROUTES.ANALYSIS_RELATIVE_SPREADS,
        lazy: () => import("@pages/analysis-relative-spreads")
      },
      {
        path: ROUTES.ANALYSIS_VOLATILITY,
        lazy: () => import("@pages/analysis-volatility")
      },
      {
        path: ROUTES.TRADING_SPREADS,
        lazy: () => import("@pages/trading-spreads")
      },
    ]
  }
], { basename: import.meta.env.BASE_URL })

export { router }

