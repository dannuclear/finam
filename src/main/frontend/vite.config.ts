import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'
import tsconfigPatchs from 'vite-tsconfig-paths'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tsconfigPatchs()],
  server: {
    host: "0.0.0.0",
    proxy: {
      '^/(auth|api)': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})
