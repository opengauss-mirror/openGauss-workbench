import axios, { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios';
import { ElMessage } from 'element-plus';

declare module 'axios' {
  interface AxiosResponse {
    message: string;
    code: number;
  }
}

const service = axios.create({
  baseURL: import.meta.env.MODE === 'production' ? '/plugins/webds-plugin' : '/',
  timeout: 3000000,
  withCredentials: true,
});

service.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    const token: string = localStorage.getItem('opengauss-token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
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
    return response.data;
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
