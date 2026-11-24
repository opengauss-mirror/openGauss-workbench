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
import { ElMessage, ElMessageBox } from 'element-plus';
import { i18n } from '@/i18n/index';
import { userPersist } from '@/config';
import { sidebarForage } from '@/utils/localforage';
import { interceptHttpDisconnection } from '@/utils/activateDisconnection';

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
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  },
);

service.interceptors.response.use(
  async (response: AxiosResponse) => {
    if (response.data?.code == 401) {
      if (!showExpired) {
        showExpired = true;
        ElMessageBox({
          message: t('common.loginExpired'),
          title: t('common.systemPrompt'),
          type: 'error',
          showClose: false,
          closeOnClickModal: false,
        }).then(() => {
          showExpired = false;
          if (parent !== self) {
            localStorage.removeItem('opengauss-token');
            sessionStorage.clear();
            sidebarForage.clear();
            userPersist.storage.removeItem(userPersist.key);
            parent.location.reload();
          }
        });
      }
      return Promise.reject(response.data.msg);
    } else if (typeof response.data?.code == 'number') {
      // If has code, it has problem in backend.Maybe this code can be snippets
      if (response.data.code == 500 && response.data.msg === 'disconnection') {
        let uuid = '';
        if (response.config.method == 'get') {
          uuid = response.config.params.uuid;
        }
        if (response.config.method == 'post') {
          uuid = JSON.parse(response.config.data).uuid;
        }
        if (response.config.method == 'delete') {
          uuid = response.config.params?.uuid || JSON.parse(response.config.data).uuid;
        }
        await interceptHttpDisconnection(uuid);
        return Promise.reject();
      } else {
        showErrMessage(response.data.msg);
        return Promise.reject(response.data.msg);
      }
    } else {
      return response.data;
    }
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
    message,
    type,
    duration,
  });
}

export default service;
