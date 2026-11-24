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
import {encrypt, encryptWithPublicKey} from '@/utils/jsencrypt'

export interface LoginData {
  username: string;
  password: string;
}

export interface LoginRes {
  token: string;
}

export async function login(data: LoginData) {
  const formatData = {...data}
  formatData.password = await encrypt(formatData.password)
  return axios.post<LoginRes>('/login', formatData)
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
  const formData = { ...payload }
  formData.password = encryptWithPublicKey(payload.password)
  return axios.post('/system/user', formData)
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
  const formData = { ...payload }
  formData.password = encryptWithPublicKey(payload.password)
  return axios.put('/system/user/resetPwd', formData)
}

export function updateCode (data: any) {
  const formatData = { ...data }
  formatData.newPassword = encryptWithPublicKey(formatData.newPassword)
  formatData.oldPassword = encryptWithPublicKey(formatData.oldPassword)
  return axios.put('/system/user/profile/updatePwd', formatData)
}

export function userRoleList (userId?: string) {
  return axios.get(`/system/user/${userId}`)
}
