const { defineConfig } = require('@vue/cli-service')
const path = require('path')

const resolve = dir => path.join(__dirname, './', dir)
const port = process.env.port || process.env.npm_config_port || 80

module.exports = defineConfig({
  runtimeCompiler: true,
  transpileDependencies: false,
  lintOnSave: false,
  publicPath: "/static-plugin/base-ops/",
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
            'dark-primary-6': '#E41D1D',
            'dark-primary-5': '#E94842',
            'dark-link-6': '#E41D1D'
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
        target: 'http://39.108.219.254:9494',
        changeOrigin: true
      },
      '/base_url': {
        target: 'http://39.108.219.254:9494',
        changeOrigin: true,
        pathRewrite: {
          '^/base_url': ''
        }
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
