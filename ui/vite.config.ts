/// <reference types="vitest" />
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-swc";
import path from "path";

// https://vite.dev/config/
export default defineConfig({
    plugins: [react()],
    base: "/task-ui",
    build: {
        outDir: "./dist/static/task-ui",
        emptyOutDir: true,
    },
    resolve: {
        alias: {
            "@src": path.resolve(__dirname, "./src"),
        },
    },
    test: {
        environment: "jsdom",
        globals: true,
        setupFiles: "./test/setup.ts",
        css: true,
    },
    server: {
        proxy: {
            "/api": {
                target: "http://localhost:8080",
                changeOrigin: true,
                secure: false,
                ws: true,
            },
            "/spring-tasks-api": {
                target: "http://localhost:8080",
                changeOrigin: true,
                secure: false,
                ws: true,
            },
        },
    },
});
