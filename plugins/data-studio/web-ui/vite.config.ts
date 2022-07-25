/**
 Copyright(c) vue-admin-perfect(zouzhibin).
 */

import { defineConfig, ConfigEnv, UserConfig, loadEnv } from 'vite';
import path from 'path';

import vue from '@vitejs/plugin-vue';

import { createSvgIconsPlugin } from 'vite-plugin-svg-icons';

import vueSetupExtend from 'vite-plugin-vue-setup-extend';

function resolve(dir) {
  return path.join(__dirname, '.', dir);
}

// https://vitejs.dev/config/
export default defineConfig(({ mode }: ConfigEnv): UserConfig => {
  const env = loadEnv(mode, process.cwd(), '');
  return {
    base: mode === 'production' ? '/static-plugin/webds-plugin/' : '/',
    plugins: [
      vue(),
      vueSetupExtend(),
      createSvgIconsPlugin({
        iconDirs: [path.resolve(process.cwd(), 'src/icons/svg')],
        symbolId: 'icon-[dir]-[name]',
      }),
    ],
    css: {
      preprocessorOptions: {
        scss: {
          additionalData: `@use "./src/styles/variables.scss" as *;`,
        },
      },
    },
    resolve: {
      alias: {
        '@': resolve('src'),
        static: resolve('public/static'),
      },
      extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue'],
    },
    server: {
      host: '0.0.0.0',
      port: 8100,
      open: true,
      https: false,
      cors: true,
      proxy: {
        '/dataStudio/web': {
          target: `http://${env.VITE_API_HOST}`,
          changeOrigin: true,
        },
      },
    },
    build: {
      sourcemap: false,
    },
  };
});
