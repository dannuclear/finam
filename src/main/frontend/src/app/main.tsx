import { queryClient } from '@shared/api/query-client'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { Providers } from './providers'
import { router } from './router'
import { theme } from './styles'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Providers theme={theme} queryClient={queryClient} router={router}/>
  </StrictMode>,
)
