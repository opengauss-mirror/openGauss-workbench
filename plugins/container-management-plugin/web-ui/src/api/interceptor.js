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
import { Message } from '@arco-design/web-vue'
import { getToken } from '@/utils/auth'
import errorCode from '@/utils/errorCode'

axios.defaults.baseURL = process.env.VUE_APP_BASE_API || ''
axios.defaults.timeout = 1200000

axios.interceptors.request.use(
  (config) => {
    // let each request carry token
    // this example using the JWT token
    // Authorization is a custom headers key
    // please modify it according to the actual situation
    const token = getToken()
    if (token) {
      if (!config.headers) {
        config.headers = {}
      }
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    // do something
    return Promise.reject(error)
  }
)
// add response interceptors
axios.interceptors.response.use(
  (response) => {
    const res = response
    // If the status code is not set, the default status is success
    const code = res.data.code || 200
    // get error message
    const msg = errorCode[code] || res.data.msg || errorCode.default

    if (code === 401) {
      window.$wujie?.bus.$emit('opengauss-login-expired')
      return Promise.reject('Please Log in')
    } else if (code === 500) {
      Message.error({
        content: msg,
        duration: 5 * 1000
      })
      return Promise.reject(new Error(msg))
    } else if (code !== 200) {
      Message.error({
        content: msg,
        duration: 5 * 1000
      })
      return Promise.reject('error')
    } else {
      return res.data
    }
  },
  (error) => {
    const { message } = error
    Message.error({
      content: message || 'Error',
      duration: 3 * 1000
    })
    return Promise.reject(error)
  }
)

export default axios
