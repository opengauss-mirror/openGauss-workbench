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

import axios from 'axios'

export interface LoginData {
  username: string;
  password: string;
}

export interface LoginRes {
  token: string;
}

export function logout () {
  return axios.post<LoginRes>('/logout')
}

export function getUserInfo () {
  return axios.get('/system/user/profile')
}

export function updateUserInfo (payload: any) {
  return axios.put('/system/user/profile', payload)
}

export function getMenuList () {
  const locale = localStorage.getItem('locale') || 'zh-CN'
  return axios.get('/getRouters', {
    headers: {
      language: locale
    }
  })
}

export function getUserList (query: any) {
  return axios.get('/system/user/list', {
    params: query
  })
}

export function createUser (payload: any) {
  return axios.post('/system/user', payload)
}

export function userDelete (userId: string) {
  return axios.delete(`/system/user/${userId}`)
}

export function changeStatus (payload: any) {
  return axios.put('/system/user/changeStatus', payload)
}

export function updateUser (payload: any) {
  return axios.put('/system/user', payload)
}

export function resetCode (payload: any) {
  return axios.put('/system/user/resetPwd', payload)
}

export function userRoleList (userId?: string) {
  return axios.get(`/system/user/${userId}`)
}
