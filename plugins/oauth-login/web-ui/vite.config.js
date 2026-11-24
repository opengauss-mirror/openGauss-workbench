/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

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


