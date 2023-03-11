const { defineConfig } = require('@vue/cli-service')
const path = require('path')

const resolve = dir => path.join(__dirname, './', dir)
const port = process.env.port || process.env.npm_config_port || 80

module.exports = defineConfig({
  productionSourceMap: false,
  runtimeCompiler: true,
  transpileDependencies: false,
  lintOnSave: false,
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
            'dark-primary-7': '#FCEF92',
            'dark-primary-6': '#FCEF92',
            'dark-primary-5': '#F8EFAE',
            'dark-warning-6': '#FF7D01',
            'dark-success-6': '#00B429',
            'dark-link-6': '#FCEF92'
          },
          javascriptEnabled: true
        }
      }
    }
  },
  chainWebpack (config) {
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
  // webpack-dev-server config
  devServer: {
    headers: {
      'Access-Control-Allow-Origin': '*'
    },
    port,
    proxy: {
      // detail: https://cli.vuejs.org/config/#devserver-proxy
      [process.env.VUE_APP_BASE_API]: {
        target: process.env.API_BASE_URL,
        changeOrigin: true,
        pathRewrite: {
          ['^' + process.env.VUE_APP_BASE_API]: ''
        }
      }
    },
    allowedHosts: 'all'
  }
})
