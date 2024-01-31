import { Message } from '@arco-design/web-vue'
import Axios, { AxiosRequestConfig } from 'axios'
import router from '@/router'

export interface KeyValue {
  [key: string]: any
}

export interface resType {
  code: number | string,
  data: any,
  msg: string,
  [key: string]: any
}

const baseURL = `http://localhost:9494/`

const axios = Axios.create({
  baseURL,
  timeout: 10000 // timeout 10s
})

const whiteList = ['user/login']
let timeoutNoNotice = false

axios.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    // if include by white list
    if (config.url && !whiteList.includes(config.url.split(baseURL)[0])) {
      if (!config.data) config.data = {}
    }
    if (config.data && config.data.timeout) config.timeout = config.data.timeout
    if (config.data && config.data.timeoutNoNotice) timeoutNoNotice = true
    return config
  },
  (error) => {
    Promise.reject(error)
  }
)

axios.interceptors.response.use(
  (response: KeyValue) => {
    if (whiteList.includes(response.config.url.split(baseURL)[1])) {
      return response.data
    }
    const res = response.data
    const statusCode = parseInt(response.data.code, 10)
    const requestData = typeof response.config.data === 'object' ? {} : JSON.parse(response.config.data)
    const notNeedMessage = requestData.notNeedMessage ? requestData.notNeedMessage : false
    // if code is not 200
    if (statusCode !== 200) {
      if (response.request.responseURL.includes('excel/importPatent')) {
        return Promise.reject(res)
      } else if (!notNeedMessage) Message.error({ content: res.msg || res.message || 'Error', duration: 5 * 1000 })
      else {
        return Promise.reject(res)
      }
    }
    timeoutNoNotice = false
    return res as resType
  },
  error => {
    if (error === 'no_login') {
      Message.error({ content: 'please login！', duration: 5 * 1000 })
      router.push(`/login`)
    } else if (timeoutNoNotice) {
      timeoutNoNotice = false
      if (error.message.includes('timeout')) {
        return Promise.reject(error.message)
      } else {
        Message.error({ content: error ? (error.message ? error.message : error.msg) : 'request error', duration: 5 * 1000 })
        return Promise.reject(error)
      }
    } else {
      Message.error({ content: error ? (error.message ? error.message : error.msg) : 'request error', duration: 5 * 1000 })
      return Promise.reject(error)
    }
  }
)

export default axios

