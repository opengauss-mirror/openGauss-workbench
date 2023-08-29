import axios from 'axios'
import type { AxiosRequestConfig, AxiosResponse } from 'axios'
import { Message, Modal, Notification } from '@arco-design/web-vue'
import { getToken } from '@/utils/auth'
import errorCode from '@/utils/errorCode'

// need to update url path
const whiteNameList = ['host/listAll', 'host/ping', 'hostUser/listAll/', 'hostUser/listAllWithoutRoot/',
  'az/listAll', 'encryption/getKey', 'opsCluster/check']

export interface HttpResponse<T = unknown> {
  status: number;
  msg: string;
  code: number;
  data: T;
}

// axios.defaults.baseURL = 'http://47.95.39.132:9494/'
axios.defaults.baseURL = process.env.VUE_APP_BASE_API || ''
axios.defaults.timeout = 1200000

let isTimeout = false

axios.interceptors.request.use(
  (config: AxiosRequestConfig) => {
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

    // update url for request base project
    whiteNameList.forEach(item => {
      if (config.url?.includes(item)) {
        if (process.env.NODE_ENV === 'development') {
          config.baseURL = '/base_url'
        } else {
          config.baseURL = '/'
        }
      }
    })
    return config
  },
  (error) => {
    // do something
    return Promise.reject(error)
  }
)
// add response interceptors
axios.interceptors.response.use(
  (response: AxiosResponse<HttpResponse>) => {
    const res = response
    // If the status code is not set, the default status is success
    const code = res.data.code || 200
    // get error message
    const msg = (errorCode as any)[code] || res.data.msg || errorCode['default']

    if (code === 401) {
      if (!isTimeout) {
        isTimeout = true
      }
      // @ts-ignore
      window.$wujie?.bus.$emit('opengauss-login-expired')
      return Promise.reject('Invalid session, or session expired, please log in again.')
    } else if (code === 500) {
      isTimeout = true
      Message.error({
        content: msg,
        duration: 5 * 1000
      })
      return Promise.reject(new Error(msg))
    } else if (code !== 200) {
      isTimeout = true
      Notification.error({
        title: 'Error',
        content: msg
      })
      return Promise.reject('error')
    } else {
      isTimeout = true
      return res.data
    }
  },
  (error) => {
    let { message } = error
    if (message == 'Network Error') {
      message = 'Network Error'
    } else if (message.includes('timeout')) {
      message = 'Request timed out'
    } else if (message.includes('Request failed with status code')) {
      message = 'System interface ' + message.substr(message.length - 3) + 'error'
    }
    Message.error({
      content: message || 'Request failed',
      duration: 3 * 1000
    })
    return Promise.reject(error)
  }
)
