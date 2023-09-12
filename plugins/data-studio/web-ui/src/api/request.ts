import axios, { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { i18n } from '@/i18n/index';
import { userPersist } from '@/config';
import { sidebarForage } from '@/utils/localforage';

declare module 'axios' {
  interface AxiosResponse {
    message: string;
    code: number;
    [key: string]: any;
  }
}

const t = i18n.global.t;
let showExpired = false;
const service = axios.create({
  baseURL:
    import.meta.env.MODE === 'production' ? `/plugins/${import.meta.env.VITE_PLUGIN_NAME}` : '/',
  timeout: 3000000,
  withCredentials: true,
});

service.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    const token: string = localStorage.getItem('opengauss-token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
      config.headers['Accept-Language'] = i18n.global.locale.value;
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  },
);

service.interceptors.response.use(
  (response: AxiosResponse) => {
    if (response.data?.code == 401) {
      !showExpired &&
        ElMessageBox({
          message: t('common.loginExpired'),
          title: t('common.systemPrompt'),
          type: 'error',
          showClose: false,
          closeOnClickModal: false,
        }).then(() => {
          if (parent !== self) {
            localStorage.removeItem('opengauss-token');
            sessionStorage.clear();
            sidebarForage.clear();
            userPersist.storage.removeItem(userPersist.key);
            parent.location.reload();
          }
          return Promise.reject(response.data.msg);
        });
      showExpired = true;
    }
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
