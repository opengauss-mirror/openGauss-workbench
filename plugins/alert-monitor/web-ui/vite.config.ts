import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";
import AutoImport from "unplugin-auto-import/vite";
import Components from "unplugin-vue-components/vite";
import { ElementPlusResolver } from "unplugin-vue-components/resolvers";
import { createSvgIconsPlugin } from "vite-plugin-svg-icons";
import { resolve } from "path";

// https://vitejs.dev/config/
export default defineConfig(({ command, mode }) => {
    loadEnv(mode, process.cwd());
    return {
        base: mode === "production" ? "/static-plugin/alert-monitor/" : "/",
        plugins: [
            vue(),
            AutoImport({
                resolvers: [ElementPlusResolver()],
                imports: ["vue", "vue-router"],
                dts: "src/auto-imports.d.ts",
                eslintrc: {
                    enabled: false,
                    filepath: "./.eslintrc-auto-import.json",
                    globalsPropValue: true,
                },
            }),
            Components({
                resolvers: [
                    ElementPlusResolver({
                        importStyle: "sass",
                    }),
                ],
                dts: "src/components.d.ts",
                dirs: ["src/components", "src/layout"],
            }),
            createSvgIconsPlugin({
                iconDirs: [resolve(process.cwd(), "src/assets/svg")],
                symbolId: "icon-[dir]-[name]",
                inject: "body-first",
            }),
        ],
        define: {
            "process.env": {
                mode,
            },
        },
        resolve: {
            alias: {
                "@": resolve(__dirname, "./src/"),
            },
        },
        css: {
            preprocessorOptions: {
                scss: {
                    additionalData: `@use "@/assets/style/theme.scss" as *;@use "@/assets/style/color.scss" as *;`,
                },
            },
        },
        server: {
            proxy: {
                '^/api': 'http://127.0.0.1:8080'
            },
        },
    };
});
