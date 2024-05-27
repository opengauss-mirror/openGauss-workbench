import axios, { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios';
import { ElMessage } from 'element-plus';
import { i18n } from '@/i18n/index';

declare module 'axios' {
  interface AxiosResponse {
    name: string;
  }
}

const t = i18n.global.t;
const service = axios.create({
  baseURL:
    import.meta.env.PROD && import.meta.env.VITE_PLUGIN_NAME
      ? `/plugins/${import.meta.env.VITE_PLUGIN_NAME}`
      : '/',
  timeout: 3000000,
  withCredentials: true,
});

service.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    const token: string = localStorage.getItem('opengauss-token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    config.headers['Accept-Language'] = i18n.global.locale.value;
    config.headers['Responsetype'] = 'blob';
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  },
);

service.interceptors.response.use(
  (response: AxiosResponse) => {
    if (response.data?.code == 500) {
      ElMessage({
        message: response.data.msg,
        type: 'error',
        duration: 5000,
      });
      return Promise.reject(response.data.msg);
    }
    return {
      name: decodeURIComponent(response.headers['content-disposition']),
      data: response.data,
    };
  },
  (error: AxiosError) => {
    showErrMessage(error.message);
    return Promise.reject(error);
  },
);

/**
 * @description showErrMessage
 * @param message error message
 * @param type message type
 * @param duration Message duration
 */
function showErrMessage(message: string, type: any = 'error', duration = 5000) {
  ElMessage({
    message: message,
    type: type,
    duration: duration,
  });
}

export default service;
