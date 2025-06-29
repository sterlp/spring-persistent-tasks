// https://vite.dev/config/
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-swc";
import { resolve } from "node:path";
import dts from "vite-plugin-dts";
import path from "path";

export default defineConfig({
    plugins: [
        react(),
        dts({
            tsconfigPath: "./tsconfig.app.json",
            exclude: "src",
            insertTypesEntry: true,
        }),
    ],
    resolve: {
        alias: {
            "@src": path.resolve(__dirname, "./src"),
            "@lib": path.resolve(__dirname, "./lib"),
        },
    },
    build: {
        copyPublicDir: false,
        lib: {
            entry: resolve(__dirname, "lib/main.ts"),
            name: "spring-persistent-tasks-ui",
            fileName: "main",
            //formats: ["es"], // "cjs"
        },
        rollupOptions: {
            external: ["react", "react-dom", "react/jsx-runtime"],
            output: {
                globals: {
                    react: "React",
                    "react-dom": "ReactDOM",
                    "react/jsx-runtime": "jsxRuntime",
                },
            },
        },
    },
});
