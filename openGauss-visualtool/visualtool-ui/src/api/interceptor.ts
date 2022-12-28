import axios from 'axios'
import type { AxiosRequestConfig, AxiosResponse } from 'axios'
import { Message, Modal } from '@arco-design/web-vue'
import { useUserStore } from '@/store'
import { getToken } from '@/utils/auth'
import errorCode from '@/utils/errorCode'

export interface HttpResponse<T = unknown> {
  status: number;
  msg: string;
  code: number;
  data: T;
}

axios.defaults.baseURL = process.env.VUE_APP_BASE_API || ''
axios.defaults.timeout = 600000

let isTimeout = false

axios.interceptors.request.use(
  (config: AxiosRequestConfig) => {
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
    return Promise.reject(error)
  }
)
// add response interceptors
axios.interceptors.response.use(
  (response: AxiosResponse<HttpResponse>) => {
    const res = response
    const code = res.data.code || 200
    const msg = (errorCode as any)[code] || res.data.msg || errorCode['default']

    if (code === 401) {
      if (!isTimeout) {
        isTimeout = true
        const locale = localStorage.getItem('locale')
        Modal.error({
          title: locale === 'en-US' ? 'System Warning' : '系统提示',
          content: locale === 'en-US' ? 'The login status has expired, you can stay on this page, or log in again' : '登录状态已过期，您可以继续留在该页面，或者重新登录',
          okText: locale === 'en-US' ? 'Re-Login' : '重新登录',
          width: 'auto',
          async onOk () {
            const userStore = useUserStore()

            await userStore.logout()
            window.location.reload()
          }
        })
      }
      return Promise.reject('Please Log in')
    } else if (code === 500) {
      isTimeout = true
      Message.error({
        content: msg,
        duration: 5 * 1000
      })
      return Promise.reject(new Error(msg))
    } else if (code !== 200) {
      isTimeout = true
      Message.error({
        content: msg,
        duration: 5 * 1000
      })
      return Promise.reject('error')
    } else {
      isTimeout = true
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
