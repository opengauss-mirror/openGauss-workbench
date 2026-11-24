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
import { SysSetting } from '@/types/sysSetting'

export function updateSysSetting (params: SysSetting) {
  return axios.put('/system/setting', params)
}

export function listSysSetting () {
  return axios.get('/system/setting')
}

export const checkUploadPath = (path: string) => {
  return axios.get('/system/setting/checkUploadPath', {
    params: {
      path: path
    }
  })
}
