import { ToastContainer } from "react-toastify"


const Toasts = () => {
    return (
        <ToastContainer
            position="top-right"
            autoClose={3000}
            hideProgressBar={false}
            newestOnTop={false}
            closeOnClick={false}
            rtl={false}
            theme="dark" />
    )
}

export { Toasts }
