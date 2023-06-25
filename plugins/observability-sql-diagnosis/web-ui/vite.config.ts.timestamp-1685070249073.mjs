// vite.config.ts
import { defineConfig, loadEnv } from "file:///D:/projects/openGauss-workbench-devcloud/openGauss-workbench-yangzihao-history/plugins/observability-sql-diagnosis/web-ui/node_modules/vite/dist/node/index.js";
import vue from "file:///D:/projects/openGauss-workbench-devcloud/openGauss-workbench-yangzihao-history/plugins/observability-sql-diagnosis/web-ui/node_modules/@vitejs/plugin-vue/dist/index.mjs";
import AutoImport from "file:///D:/projects/openGauss-workbench-devcloud/openGauss-workbench-yangzihao-history/plugins/observability-sql-diagnosis/web-ui/node_modules/unplugin-auto-import/dist/vite.mjs";
import Components from "file:///D:/projects/openGauss-workbench-devcloud/openGauss-workbench-yangzihao-history/plugins/observability-sql-diagnosis/web-ui/node_modules/unplugin-vue-components/dist/vite.mjs";
import { ElementPlusResolver } from "file:///D:/projects/openGauss-workbench-devcloud/openGauss-workbench-yangzihao-history/plugins/observability-sql-diagnosis/web-ui/node_modules/unplugin-vue-components/dist/resolvers.mjs";
import { createSvgIconsPlugin } from "file:///D:/projects/openGauss-workbench-devcloud/openGauss-workbench-yangzihao-history/plugins/observability-sql-diagnosis/web-ui/node_modules/vite-plugin-svg-icons/dist/index.mjs";
import { resolve } from "path";
var __vite_injected_original_dirname = "D:\\projects\\openGauss-workbench-devcloud\\openGauss-workbench-yangzihao-history\\plugins\\observability-sql-diagnosis\\web-ui";
var vite_config_default = defineConfig(({ command, mode }) => {
  loadEnv(mode, process.cwd());
  return {
    base: mode === "production" ? "/static-plugin/observability-sql-diagnosis/" : "/",
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
        resolvers: [ElementPlusResolver({
          importStyle: "sass"
        })],
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
        "^/historyDiagnosis": "http://192.168.110.31:9494/plugins/observability-sql-diagnosis",
        "^/sqlDiagnosis": "http://192.168.110.31:9494/plugins/observability-sql-diagnosis",
        "^/observability": "http://192.168.110.31:9494/plugins/observability-sql-diagnosis",
        "^/encryption": "http://192.168.110.31:9494"
      }
    }
  };
});
export {
  vite_config_default as default
};
//# sourceMappingURL=data:application/json;base64,ewogICJ2ZXJzaW9uIjogMywKICAic291cmNlcyI6IFsidml0ZS5jb25maWcudHMiXSwKICAic291cmNlc0NvbnRlbnQiOiBbImNvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9kaXJuYW1lID0gXCJEOlxcXFxwcm9qZWN0c1xcXFxvcGVuR2F1c3Mtd29ya2JlbmNoLWRldmNsb3VkXFxcXG9wZW5HYXVzcy13b3JrYmVuY2gteWFuZ3ppaGFvLWhpc3RvcnlcXFxccGx1Z2luc1xcXFxvYnNlcnZhYmlsaXR5LXNxbC1kaWFnbm9zaXNcXFxcd2ViLXVpXCI7Y29uc3QgX192aXRlX2luamVjdGVkX29yaWdpbmFsX2ZpbGVuYW1lID0gXCJEOlxcXFxwcm9qZWN0c1xcXFxvcGVuR2F1c3Mtd29ya2JlbmNoLWRldmNsb3VkXFxcXG9wZW5HYXVzcy13b3JrYmVuY2gteWFuZ3ppaGFvLWhpc3RvcnlcXFxccGx1Z2luc1xcXFxvYnNlcnZhYmlsaXR5LXNxbC1kaWFnbm9zaXNcXFxcd2ViLXVpXFxcXHZpdGUuY29uZmlnLnRzXCI7Y29uc3QgX192aXRlX2luamVjdGVkX29yaWdpbmFsX2ltcG9ydF9tZXRhX3VybCA9IFwiZmlsZTovLy9EOi9wcm9qZWN0cy9vcGVuR2F1c3Mtd29ya2JlbmNoLWRldmNsb3VkL29wZW5HYXVzcy13b3JrYmVuY2gteWFuZ3ppaGFvLWhpc3RvcnkvcGx1Z2lucy9vYnNlcnZhYmlsaXR5LXNxbC1kaWFnbm9zaXMvd2ViLXVpL3ZpdGUuY29uZmlnLnRzXCI7aW1wb3J0IHsgZGVmaW5lQ29uZmlnLCBsb2FkRW52IH0gZnJvbSAndml0ZSdcclxuaW1wb3J0IHZ1ZSBmcm9tICdAdml0ZWpzL3BsdWdpbi12dWUnXHJcbmltcG9ydCBBdXRvSW1wb3J0IGZyb20gJ3VucGx1Z2luLWF1dG8taW1wb3J0L3ZpdGUnXHJcbmltcG9ydCBDb21wb25lbnRzIGZyb20gJ3VucGx1Z2luLXZ1ZS1jb21wb25lbnRzL3ZpdGUnXHJcbmltcG9ydCB7IEVsZW1lbnRQbHVzUmVzb2x2ZXIgfSBmcm9tICd1bnBsdWdpbi12dWUtY29tcG9uZW50cy9yZXNvbHZlcnMnXHJcbmltcG9ydCB7IGNyZWF0ZVN2Z0ljb25zUGx1Z2luIH0gZnJvbSAndml0ZS1wbHVnaW4tc3ZnLWljb25zJ1xyXG5pbXBvcnQgeyByZXNvbHZlIH0gZnJvbSAncGF0aCc7XHJcblxyXG4vLyBodHRwczovL3ZpdGVqcy5kZXYvY29uZmlnL1xyXG5leHBvcnQgZGVmYXVsdCBkZWZpbmVDb25maWcoKHsgY29tbWFuZCwgbW9kZSB9KSA9PiB7XHJcbiAgICAvLyBDaGVjayB0aGUgZW52aXJvbm1lbnQgZmlsZXMuIGVudi5kZXZlbG9wbWVudCBhbmQuIGVudi5wcm9kdWN0aW9uIHVuZGVyIHRoZSBwYXRoIG9mIHByb2Nlc3MuY3dkKClcclxuICAgIGxvYWRFbnYobW9kZSwgcHJvY2Vzcy5jd2QoKSk7IFxyXG4gICAgcmV0dXJuIHtcclxuICAgICAgICBiYXNlOiBtb2RlID09PSAncHJvZHVjdGlvbicgPyBcIi9zdGF0aWMtcGx1Z2luL29ic2VydmFiaWxpdHktc3FsLWRpYWdub3Npcy9cIiA6ICcvJyxcclxuICAgICAgICBwbHVnaW5zOiBbXHJcbiAgICAgICAgICAgIHZ1ZSgpLFxyXG4gICAgICAgICAgICBBdXRvSW1wb3J0KHtcclxuICAgICAgICAgICAgICAgIHJlc29sdmVyczogW0VsZW1lbnRQbHVzUmVzb2x2ZXIoKV0sXHJcbiAgICAgICAgICAgICAgICBpbXBvcnRzOiBbJ3Z1ZScsICd2dWUtcm91dGVyJ10sXHJcbiAgICAgICAgICAgICAgICBkdHM6ICdzcmMvYXV0by1pbXBvcnRzLmQudHMnLFxyXG4gICAgICAgICAgICAgICAgZXNsaW50cmM6IHtcclxuICAgICAgICAgICAgICAgICAgICBlbmFibGVkOiBmYWxzZSxcclxuICAgICAgICAgICAgICAgICAgICBmaWxlcGF0aDogJy4vLmVzbGludHJjLWF1dG8taW1wb3J0Lmpzb24nLFxyXG4gICAgICAgICAgICAgICAgICAgIGdsb2JhbHNQcm9wVmFsdWU6IHRydWVcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSksXHJcbiAgICAgICAgICAgIENvbXBvbmVudHMoe1xyXG4gICAgICAgICAgICAgICAgcmVzb2x2ZXJzOiBbRWxlbWVudFBsdXNSZXNvbHZlcih7XHJcbiAgICAgICAgICAgICAgICAgICAgaW1wb3J0U3R5bGU6ICdzYXNzJ1xyXG4gICAgICAgICAgICAgICAgfSldLFxyXG4gICAgICAgICAgICAgICAgZHRzOiAnc3JjL2NvbXBvbmVudHMuZC50cycsXHJcbiAgICAgICAgICAgICAgICBkaXJzOiBbJ3NyYy9jb21wb25lbnRzJywgJ3NyYy9sYXlvdXQnXVxyXG4gICAgICAgICAgICB9KSxcclxuICAgICAgICAgICAgY3JlYXRlU3ZnSWNvbnNQbHVnaW4oe1xyXG4gICAgICAgICAgICAgICAgaWNvbkRpcnM6IFtyZXNvbHZlKHByb2Nlc3MuY3dkKCksICdzcmMvYXNzZXRzL3N2ZycpXSxcclxuICAgICAgICAgICAgICAgIHN5bWJvbElkOiAnaWNvbi1bZGlyXS1bbmFtZV0nLFxyXG4gICAgICAgICAgICAgICAgaW5qZWN0OiAnYm9keS1maXJzdCdcclxuICAgICAgICAgICAgfSlcclxuICAgICAgICBdLFxyXG4gICAgICAgIGRlZmluZToge1xyXG4gICAgICAgICAgICAncHJvY2Vzcy5lbnYnOiB7XHJcbiAgICAgICAgICAgICAgICBtb2RlXHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9LFxyXG4gICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgYWxpYXM6IHtcclxuICAgICAgICAgICAgICAgICdAJzogcmVzb2x2ZShfX2Rpcm5hbWUsICcuL3NyYy8nKSxcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICB9LFxyXG4gICAgICAgIGNzczoge1xyXG4gICAgICAgICAgICBwcmVwcm9jZXNzb3JPcHRpb25zOiB7XHJcbiAgICAgICAgICAgICAgICBzY3NzOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgYWRkaXRpb25hbERhdGE6IGBAdXNlIFwiQC9hc3NldHMvc3R5bGUvdGhlbWUuc2Nzc1wiIGFzICo7QHVzZSBcIkAvYXNzZXRzL3N0eWxlL2NvbG9yLnNjc3NcIiBhcyAqO2BcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0sXHJcbiAgICAgICAgc2VydmVyOiB7XHJcbiAgICAgICAgICAgIHByb3h5OiB7XHJcbiAgICAgICAgICAgICAgICAnXi9oaXN0b3J5RGlhZ25vc2lzJzogJ2h0dHA6Ly8xOTIuMTY4LjExMC4zMTo5NDk0L3BsdWdpbnMvb2JzZXJ2YWJpbGl0eS1zcWwtZGlhZ25vc2lzJyxcclxuICAgICAgICAgICAgICAgICdeL3NxbERpYWdub3Npcyc6ICdodHRwOi8vMTkyLjE2OC4xMTAuMzE6OTQ5NC9wbHVnaW5zL29ic2VydmFiaWxpdHktc3FsLWRpYWdub3NpcycsXHJcbiAgICAgICAgICAgICAgICAnXi9vYnNlcnZhYmlsaXR5JzogJ2h0dHA6Ly8xOTIuMTY4LjExMC4zMTo5NDk0L3BsdWdpbnMvb2JzZXJ2YWJpbGl0eS1zcWwtZGlhZ25vc2lzJyxcclxuICAgICAgICAgICAgICAgIFwiXi9lbmNyeXB0aW9uXCI6IFwiaHR0cDovLzE5Mi4xNjguMTEwLjMxOjk0OTRcIixcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH1cclxuICAgIH1cclxufSlcclxuIl0sCiAgIm1hcHBpbmdzIjogIjtBQUEyaUIsU0FBUyxjQUFjLGVBQWU7QUFDamxCLE9BQU8sU0FBUztBQUNoQixPQUFPLGdCQUFnQjtBQUN2QixPQUFPLGdCQUFnQjtBQUN2QixTQUFTLDJCQUEyQjtBQUNwQyxTQUFTLDRCQUE0QjtBQUNyQyxTQUFTLGVBQWU7QUFOeEIsSUFBTSxtQ0FBbUM7QUFTekMsSUFBTyxzQkFBUSxhQUFhLENBQUMsRUFBRSxTQUFTLEtBQUssTUFBTTtBQUUvQyxVQUFRLE1BQU0sUUFBUSxJQUFJLENBQUM7QUFDM0IsU0FBTztBQUFBLElBQ0gsTUFBTSxTQUFTLGVBQWUsZ0RBQWdEO0FBQUEsSUFDOUUsU0FBUztBQUFBLE1BQ0wsSUFBSTtBQUFBLE1BQ0osV0FBVztBQUFBLFFBQ1AsV0FBVyxDQUFDLG9CQUFvQixDQUFDO0FBQUEsUUFDakMsU0FBUyxDQUFDLE9BQU8sWUFBWTtBQUFBLFFBQzdCLEtBQUs7QUFBQSxRQUNMLFVBQVU7QUFBQSxVQUNOLFNBQVM7QUFBQSxVQUNULFVBQVU7QUFBQSxVQUNWLGtCQUFrQjtBQUFBLFFBQ3RCO0FBQUEsTUFDSixDQUFDO0FBQUEsTUFDRCxXQUFXO0FBQUEsUUFDUCxXQUFXLENBQUMsb0JBQW9CO0FBQUEsVUFDNUIsYUFBYTtBQUFBLFFBQ2pCLENBQUMsQ0FBQztBQUFBLFFBQ0YsS0FBSztBQUFBLFFBQ0wsTUFBTSxDQUFDLGtCQUFrQixZQUFZO0FBQUEsTUFDekMsQ0FBQztBQUFBLE1BQ0QscUJBQXFCO0FBQUEsUUFDakIsVUFBVSxDQUFDLFFBQVEsUUFBUSxJQUFJLEdBQUcsZ0JBQWdCLENBQUM7QUFBQSxRQUNuRCxVQUFVO0FBQUEsUUFDVixRQUFRO0FBQUEsTUFDWixDQUFDO0FBQUEsSUFDTDtBQUFBLElBQ0EsUUFBUTtBQUFBLE1BQ0osZUFBZTtBQUFBLFFBQ1g7QUFBQSxNQUNKO0FBQUEsSUFDSjtBQUFBLElBQ0EsU0FBUztBQUFBLE1BQ0wsT0FBTztBQUFBLFFBQ0gsS0FBSyxRQUFRLGtDQUFXLFFBQVE7QUFBQSxNQUNwQztBQUFBLElBQ0o7QUFBQSxJQUNBLEtBQUs7QUFBQSxNQUNELHFCQUFxQjtBQUFBLFFBQ2pCLE1BQU07QUFBQSxVQUNGLGdCQUFnQjtBQUFBLFFBQ3BCO0FBQUEsTUFDSjtBQUFBLElBQ0o7QUFBQSxJQUNBLFFBQVE7QUFBQSxNQUNKLE9BQU87QUFBQSxRQUNILHNCQUFzQjtBQUFBLFFBQ3RCLGtCQUFrQjtBQUFBLFFBQ2xCLG1CQUFtQjtBQUFBLFFBQ25CLGdCQUFnQjtBQUFBLE1BQ3BCO0FBQUEsSUFDSjtBQUFBLEVBQ0o7QUFDSixDQUFDOyIsCiAgIm5hbWVzIjogW10KfQo=
