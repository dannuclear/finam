import { Box } from "@mui/material"
// import { ROUTES } from "@shared/routes"
import { Header } from "@widgets/layout"
import { useEffect } from "react"
import { Outlet, useNavigate } from "react-router"

function App() {
  const navigate = useNavigate()

  useEffect(() => {
    // const handler = () => navigate(ROUTES.LOGIN)
    // window.addEventListener("unauthorized", handler)
    // return () => window.removeEventListener("unauthorized", handler)
  }, [navigate])

  return (
    <>
      <Header />
      <Box sx={{ padding: 1 }}>
        <Outlet />
      </Box>
    </>
  )
}

export default App
