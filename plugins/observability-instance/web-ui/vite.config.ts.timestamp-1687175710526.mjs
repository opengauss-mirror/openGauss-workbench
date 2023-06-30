// vite.config.ts
import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";
import AutoImport from "unplugin-auto-import/vite";
import Components from "unplugin-vue-components/vite";
import { ElementPlusResolver } from "unplugin-vue-components/resolvers";
import { createSvgIconsPlugin } from "vite-plugin-svg-icons";
import { resolve } from "path";
var __vite_injected_original_dirname = "E:\\work\\project\\opengauss-workbench\\code\\openGauss-workbench\\plugins\\observability-instance\\web-ui";
var vite_config_default = defineConfig(({ command, mode }) => {
  loadEnv(mode, process.cwd());
  return {
    base: mode === "production" ? "/static-plugin/observability-instance/" : "/",
    plugins: [
      vue(),
      AutoImport({
        resolvers: [ElementPlusResolver()],
        imports: ["vue", "vue-router"],
        dts: "src/auto-imports.d.ts",
        eslintrc: {
          enabled: false,
          filepath: "./.eslintrc-auto-import.json",
          globalsPropValue: true
        }
      }),
      Components({
        resolvers: [
          ElementPlusResolver({
            importStyle: "sass"
          })
        ],
        dts: "src/components.d.ts",
        dirs: ["src/components", "src/layout"]
      }),
      createSvgIconsPlugin({
        iconDirs: [resolve(process.cwd(), "src/assets/svg")],
        symbolId: "icon-[dir]-[name]",
        inject: "body-first"
      })
    ],
    define: {
      "process.env": {
        mode
      }
    },
    resolve: {
      alias: {
        "@": resolve(__vite_injected_original_dirname, "./src/")
      }
    },
    css: {
      preprocessorOptions: {
        scss: {
          additionalData: `@use "@/assets/style/theme.scss" as *;@use "@/assets/style/color.scss" as *;`
        }
      }
    },
    server: {
      proxy: {
        "^/observability": "http://192.168.110.31:9494/plugins/observability-instance",
        "^/sqlDiagnosis": "http://192.168.110.31:9494/plugins/observability-instance",
        "^/wdr": "http://192.168.110.31:9494/plugins/observability-instance",
        "^/encryption": "http://192.168.110.31:9494/plugins/observability-instance",
        "^/host": "http://192.168.110.31:9494/",
        "^/hostUser": "http://192.168.110.31:9494/"
      }
    }
  };
});
export {
  vite_config_default as default
};
//# sourceMappingURL=data:application/json;base64,ewogICJ2ZXJzaW9uIjogMywKICAic291cmNlcyI6IFsidml0ZS5jb25maWcudHMiXSwKICAic291cmNlc0NvbnRlbnQiOiBbImNvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9kaXJuYW1lID0gXCJFOlxcXFx3b3JrXFxcXHByb2plY3RcXFxcb3BlbmdhdXNzLXdvcmtiZW5jaFxcXFxjb2RlXFxcXG9wZW5HYXVzcy13b3JrYmVuY2hcXFxccGx1Z2luc1xcXFxvYnNlcnZhYmlsaXR5LWluc3RhbmNlXFxcXHdlYi11aVwiO2NvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9maWxlbmFtZSA9IFwiRTpcXFxcd29ya1xcXFxwcm9qZWN0XFxcXG9wZW5nYXVzcy13b3JrYmVuY2hcXFxcY29kZVxcXFxvcGVuR2F1c3Mtd29ya2JlbmNoXFxcXHBsdWdpbnNcXFxcb2JzZXJ2YWJpbGl0eS1pbnN0YW5jZVxcXFx3ZWItdWlcXFxcdml0ZS5jb25maWcudHNcIjtjb25zdCBfX3ZpdGVfaW5qZWN0ZWRfb3JpZ2luYWxfaW1wb3J0X21ldGFfdXJsID0gXCJmaWxlOi8vL0U6L3dvcmsvcHJvamVjdC9vcGVuZ2F1c3Mtd29ya2JlbmNoL2NvZGUvb3BlbkdhdXNzLXdvcmtiZW5jaC9wbHVnaW5zL29ic2VydmFiaWxpdHktaW5zdGFuY2Uvd2ViLXVpL3ZpdGUuY29uZmlnLnRzXCI7Ly8vXHJcbi8vLyBDb3B5cmlnaHQgKGMpIDIwMjMgSHVhd2VpIFRlY2hub2xvZ2llcyBDby4sTHRkLlxyXG4vLy9cclxuXHJcbmltcG9ydCB7IGRlZmluZUNvbmZpZywgbG9hZEVudiB9IGZyb20gXCJ2aXRlXCI7XHJcbmltcG9ydCB2dWUgZnJvbSBcIkB2aXRlanMvcGx1Z2luLXZ1ZVwiO1xyXG5pbXBvcnQgQXV0b0ltcG9ydCBmcm9tIFwidW5wbHVnaW4tYXV0by1pbXBvcnQvdml0ZVwiO1xyXG5pbXBvcnQgQ29tcG9uZW50cyBmcm9tIFwidW5wbHVnaW4tdnVlLWNvbXBvbmVudHMvdml0ZVwiO1xyXG5pbXBvcnQgeyBFbGVtZW50UGx1c1Jlc29sdmVyIH0gZnJvbSBcInVucGx1Z2luLXZ1ZS1jb21wb25lbnRzL3Jlc29sdmVyc1wiO1xyXG5pbXBvcnQgeyBjcmVhdGVTdmdJY29uc1BsdWdpbiB9IGZyb20gXCJ2aXRlLXBsdWdpbi1zdmctaWNvbnNcIjtcclxuaW1wb3J0IHsgcmVzb2x2ZSB9IGZyb20gXCJwYXRoXCI7XHJcblxyXG4vLyBodHRwczovL3ZpdGVqcy5kZXYvY29uZmlnL1xyXG5leHBvcnQgZGVmYXVsdCBkZWZpbmVDb25maWcoKHsgY29tbWFuZCwgbW9kZSB9KSA9PiB7XHJcbiAgICBsb2FkRW52KG1vZGUsIHByb2Nlc3MuY3dkKCkpO1xyXG4gICAgcmV0dXJuIHtcclxuICAgICAgICBiYXNlOiBtb2RlID09PSBcInByb2R1Y3Rpb25cIiA/IFwiL3N0YXRpYy1wbHVnaW4vb2JzZXJ2YWJpbGl0eS1pbnN0YW5jZS9cIiA6IFwiL1wiLFxyXG4gICAgICAgIHBsdWdpbnM6IFtcclxuICAgICAgICAgICAgdnVlKCksXHJcbiAgICAgICAgICAgIEF1dG9JbXBvcnQoe1xyXG4gICAgICAgICAgICAgICAgcmVzb2x2ZXJzOiBbRWxlbWVudFBsdXNSZXNvbHZlcigpXSxcclxuICAgICAgICAgICAgICAgIGltcG9ydHM6IFtcInZ1ZVwiLCBcInZ1ZS1yb3V0ZXJcIl0sXHJcbiAgICAgICAgICAgICAgICBkdHM6IFwic3JjL2F1dG8taW1wb3J0cy5kLnRzXCIsXHJcbiAgICAgICAgICAgICAgICBlc2xpbnRyYzoge1xyXG4gICAgICAgICAgICAgICAgICAgIGVuYWJsZWQ6IGZhbHNlLFxyXG4gICAgICAgICAgICAgICAgICAgIGZpbGVwYXRoOiBcIi4vLmVzbGludHJjLWF1dG8taW1wb3J0Lmpzb25cIixcclxuICAgICAgICAgICAgICAgICAgICBnbG9iYWxzUHJvcFZhbHVlOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgfSksXHJcbiAgICAgICAgICAgIENvbXBvbmVudHMoe1xyXG4gICAgICAgICAgICAgICAgcmVzb2x2ZXJzOiBbXHJcbiAgICAgICAgICAgICAgICAgICAgRWxlbWVudFBsdXNSZXNvbHZlcih7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIGltcG9ydFN0eWxlOiBcInNhc3NcIixcclxuICAgICAgICAgICAgICAgICAgICB9KSxcclxuICAgICAgICAgICAgICAgIF0sXHJcbiAgICAgICAgICAgICAgICBkdHM6IFwic3JjL2NvbXBvbmVudHMuZC50c1wiLFxyXG4gICAgICAgICAgICAgICAgZGlyczogW1wic3JjL2NvbXBvbmVudHNcIiwgXCJzcmMvbGF5b3V0XCJdLFxyXG4gICAgICAgICAgICB9KSxcclxuICAgICAgICAgICAgY3JlYXRlU3ZnSWNvbnNQbHVnaW4oe1xyXG4gICAgICAgICAgICAgICAgaWNvbkRpcnM6IFtyZXNvbHZlKHByb2Nlc3MuY3dkKCksIFwic3JjL2Fzc2V0cy9zdmdcIildLFxyXG4gICAgICAgICAgICAgICAgc3ltYm9sSWQ6IFwiaWNvbi1bZGlyXS1bbmFtZV1cIixcclxuICAgICAgICAgICAgICAgIGluamVjdDogXCJib2R5LWZpcnN0XCIsXHJcbiAgICAgICAgICAgIH0pLFxyXG4gICAgICAgIF0sXHJcbiAgICAgICAgZGVmaW5lOiB7XHJcbiAgICAgICAgICAgIFwicHJvY2Vzcy5lbnZcIjoge1xyXG4gICAgICAgICAgICAgICAgbW9kZSxcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICB9LFxyXG4gICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgYWxpYXM6IHtcclxuICAgICAgICAgICAgICAgIFwiQFwiOiByZXNvbHZlKF9fZGlybmFtZSwgXCIuL3NyYy9cIiksXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgfSxcclxuICAgICAgICBjc3M6IHtcclxuICAgICAgICAgICAgcHJlcHJvY2Vzc29yT3B0aW9uczoge1xyXG4gICAgICAgICAgICAgICAgc2Nzczoge1xyXG4gICAgICAgICAgICAgICAgICAgIGFkZGl0aW9uYWxEYXRhOiBgQHVzZSBcIkAvYXNzZXRzL3N0eWxlL3RoZW1lLnNjc3NcIiBhcyAqO0B1c2UgXCJAL2Fzc2V0cy9zdHlsZS9jb2xvci5zY3NzXCIgYXMgKjtgLFxyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICB9LFxyXG4gICAgICAgIHNlcnZlcjoge1xyXG4gICAgICAgICAgICBwcm94eToge1xyXG4gICAgICAgICAgICAgICAgJ14vb2JzZXJ2YWJpbGl0eSc6ICdodHRwOi8vMTkyLjE2OC4xMTAuMzE6OTQ5NC9wbHVnaW5zL29ic2VydmFiaWxpdHktaW5zdGFuY2UnLFxyXG4gICAgICAgICAgICAgICAgJ14vc3FsRGlhZ25vc2lzJzogJ2h0dHA6Ly8xOTIuMTY4LjExMC4zMTo5NDk0L3BsdWdpbnMvb2JzZXJ2YWJpbGl0eS1pbnN0YW5jZScsXHJcbiAgICAgICAgICAgICAgICAnXi93ZHInOiAnaHR0cDovLzE5Mi4xNjguMTEwLjMxOjk0OTQvcGx1Z2lucy9vYnNlcnZhYmlsaXR5LWluc3RhbmNlJyxcclxuICAgICAgICAgICAgICAgICdeL2VuY3J5cHRpb24nOiAnaHR0cDovLzE5Mi4xNjguMTEwLjMxOjk0OTQvcGx1Z2lucy9vYnNlcnZhYmlsaXR5LWluc3RhbmNlJyxcclxuICAgICAgICAgICAgICAgICdeL2hvc3QnOiAnaHR0cDovLzE5Mi4xNjguMTEwLjMxOjk0OTQvJyxcclxuICAgICAgICAgICAgICAgICdeL2hvc3RVc2VyJzogJ2h0dHA6Ly8xOTIuMTY4LjExMC4zMTo5NDk0LydcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICB9LFxyXG4gICAgfTtcclxufSk7XHJcbiJdLAogICJtYXBwaW5ncyI6ICI7QUFJQSxTQUFTLGNBQWMsZUFBZTtBQUN0QyxPQUFPLFNBQVM7QUFDaEIsT0FBTyxnQkFBZ0I7QUFDdkIsT0FBTyxnQkFBZ0I7QUFDdkIsU0FBUywyQkFBMkI7QUFDcEMsU0FBUyw0QkFBNEI7QUFDckMsU0FBUyxlQUFlO0FBVnhCLElBQU0sbUNBQW1DO0FBYXpDLElBQU8sc0JBQVEsYUFBYSxDQUFDLEVBQUUsU0FBUyxLQUFLLE1BQU07QUFDL0MsVUFBUSxNQUFNLFFBQVEsSUFBSSxDQUFDO0FBQzNCLFNBQU87QUFBQSxJQUNILE1BQU0sU0FBUyxlQUFlLDJDQUEyQztBQUFBLElBQ3pFLFNBQVM7QUFBQSxNQUNMLElBQUk7QUFBQSxNQUNKLFdBQVc7QUFBQSxRQUNQLFdBQVcsQ0FBQyxvQkFBb0IsQ0FBQztBQUFBLFFBQ2pDLFNBQVMsQ0FBQyxPQUFPLFlBQVk7QUFBQSxRQUM3QixLQUFLO0FBQUEsUUFDTCxVQUFVO0FBQUEsVUFDTixTQUFTO0FBQUEsVUFDVCxVQUFVO0FBQUEsVUFDVixrQkFBa0I7QUFBQSxRQUN0QjtBQUFBLE1BQ0osQ0FBQztBQUFBLE1BQ0QsV0FBVztBQUFBLFFBQ1AsV0FBVztBQUFBLFVBQ1Asb0JBQW9CO0FBQUEsWUFDaEIsYUFBYTtBQUFBLFVBQ2pCLENBQUM7QUFBQSxRQUNMO0FBQUEsUUFDQSxLQUFLO0FBQUEsUUFDTCxNQUFNLENBQUMsa0JBQWtCLFlBQVk7QUFBQSxNQUN6QyxDQUFDO0FBQUEsTUFDRCxxQkFBcUI7QUFBQSxRQUNqQixVQUFVLENBQUMsUUFBUSxRQUFRLElBQUksR0FBRyxnQkFBZ0IsQ0FBQztBQUFBLFFBQ25ELFVBQVU7QUFBQSxRQUNWLFFBQVE7QUFBQSxNQUNaLENBQUM7QUFBQSxJQUNMO0FBQUEsSUFDQSxRQUFRO0FBQUEsTUFDSixlQUFlO0FBQUEsUUFDWDtBQUFBLE1BQ0o7QUFBQSxJQUNKO0FBQUEsSUFDQSxTQUFTO0FBQUEsTUFDTCxPQUFPO0FBQUEsUUFDSCxLQUFLLFFBQVEsa0NBQVcsUUFBUTtBQUFBLE1BQ3BDO0FBQUEsSUFDSjtBQUFBLElBQ0EsS0FBSztBQUFBLE1BQ0QscUJBQXFCO0FBQUEsUUFDakIsTUFBTTtBQUFBLFVBQ0YsZ0JBQWdCO0FBQUEsUUFDcEI7QUFBQSxNQUNKO0FBQUEsSUFDSjtBQUFBLElBQ0EsUUFBUTtBQUFBLE1BQ0osT0FBTztBQUFBLFFBQ0gsbUJBQW1CO0FBQUEsUUFDbkIsa0JBQWtCO0FBQUEsUUFDbEIsU0FBUztBQUFBLFFBQ1QsZ0JBQWdCO0FBQUEsUUFDaEIsVUFBVTtBQUFBLFFBQ1YsY0FBYztBQUFBLE1BQ2xCO0FBQUEsSUFDSjtBQUFBLEVBQ0o7QUFDSixDQUFDOyIsCiAgIm5hbWVzIjogW10KfQo=
