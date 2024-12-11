import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
    plugins: [react()],
    base: 'task-ui',
    build: {
        outDir: './dist/static/task-ui',
        emptyOutDir: true
    },
    server: {
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                secure: false,
                ws: true,
            },
            '/spring-tasks-api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                secure: false,
                ws: true,
            }
        }
    }
})
