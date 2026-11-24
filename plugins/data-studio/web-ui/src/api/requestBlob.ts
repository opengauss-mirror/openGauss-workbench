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
      showErrMessage(response.data.msg);
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
