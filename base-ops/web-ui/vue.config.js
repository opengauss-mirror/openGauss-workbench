const { defineConfig } = require('@vue/cli-service')
const path = require('path')

const resolve = dir => path.join(__dirname, './', dir)
const port = process.env.port || process.env.npm_config_port || 80

module.exports = defineConfig({
  runtimeCompiler: true,
  transpileDependencies: false,
  lintOnSave: false,
  publicPath: "/static-plugin/base-ops/",
  productionSourceMap: false,
  css: {
    loaderOptions: {
      less: {
        lessOptions: {
          modifyVars: {
            'primary-6': '#E41D1D',
            'primary-5': '#E94842',
            'primary-7': '#BE1118',
            'primary-4': '#FAC4BC',
            'primary-3': '#F49C92',
            'primary-2': '#F49C92',
            'primary-1': '#FFECE8',
            'warning-6': '#FF7D01',
            'success-6': '#00B429',
            'link-6': '#E41D1D',
            'color-text-1': '#1D212A',
            'color-text-2': '#4E5A6A',
            'color-text-3': '#868F9C',
            'color-text-4': '#f2f3f5',
            'dark-color-text-4': '#4E5A6A',
            'dark-primary-6': '#FCEF92',
            'dark-primary-5': '#F8EFAE',
            'dark-link-6': '#FCEF92',
            'cpu-bg': '#F5F2FB',
            'dark-cpu-bg': '#868F9C',
            'lock-bg': '#E9F0FF',
            'dark-lock-bg': '#868F9C',
            'memory-bg': '#ECF9FF',
            'dark-memory-bg': '#868F9C',
            'connect-bg': '#EEFFEC',
            'dark-connect-bg': '#868F9C',
            'net-bg': '#FFF5E5',
            'dark-net-bg': '#868F9C',
            'session-bg': '#FFECEC',
            'dark-session-bg': '#868F9C',
            'session-top-bg': '#F2F8FF',
            'dark-session-top-bg': '#868F9C',
          },
          javascriptEnabled: true
        }
      }
    }
  },
  chainWebpack(config) {
    config.module.rule('svg').exclude.add(resolve('src/assets/icons')).end()
    config.module
      .rule('icons')
      .test(/\.svg$/)
      .include.add(resolve('src/assets/icons'))
      .end()
      .use('svg-sprite-loader')
      .loader('svg-sprite-loader')
      .options({
        symbolId: 'icon-[name]'
      })
      .end()
  },
  devServer: {
    headers: {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Headers": "*",
      "Access-Control-Allow-Methods": "*",
    },
    port,
    proxy: {
      // detail: https://cli.vuejs.org/config/#devserver-proxy
      [process.env.VUE_APP_BASE_API]: {
        target: process.env.API_BASE_URL,
        changeOrigin: true,
        ws: true
      },
      '/base_url': {
        target: process.env.API_BASE_URL,
        changeOrigin: true,
        pathRewrite: {
          '^/base_url': ''
        },
        ws: true
      }
    },
    historyApiFallback: {
      rewrites: [
        { from: /\/static-plugin\/base-ops/, to: '/static-plugin/base-ops/index.html' }
      ]
    },
    allowedHosts: 'all'
  }
})
