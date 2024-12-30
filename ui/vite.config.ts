import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path';

// https://vite.dev/config/
export default defineConfig({
    plugins: [react()],
    base: '/task-ui',
    build: {
        outDir: './dist/static/task-ui',
        emptyOutDir: true
    },
    resolve: {
        alias: {
            '@src': path.resolve(__dirname, './src'),
        }
    },
    test: {
        environment: 'jsdom',
        globals: true,
        setupFiles: './test/setup.ts', // Optional: Set up custom global mocks or utilities
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
