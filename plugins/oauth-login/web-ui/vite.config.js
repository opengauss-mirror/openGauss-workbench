import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  runtimeCompiler: true,
  transpileDependencies: false,
  lintOnSave: false,
  base: "/static-plugin/oauth-login/",
  productionSourceMap: false,
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    host: '0.0.0.0',
    port: 8081,
    cors: true,
    proxy: {
      [process.env.VUE_APP_BASE_API]: {
        target: process.env.API_BASE_URL,
        changeOrigin: true,
        ws: true
      },
      '/api':{
        target:'http://localhost:9494',
        changeOrigin: true,
        rewrite:(path) => path.replace(/^\/api/,'')
      }

    },
    historyApiFallback: {
      rewrites: [
        { from: /\/plugins\/oauth-login/, to: '/plugins/oauth-login/index.html' }
      ]
    },
  },
  devServer:{

  }

})


