import axios from 'axios'
import type { AxiosRequestConfig, AxiosResponse } from 'axios'
import { Message, Modal } from '@arco-design/web-vue'
import { useUserStore } from '@/store'
import { getToken } from '@/utils/auth'
import errorCode from '@/utils/errorCode'
import i18n from '@/locale/index'

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
    if (token && config.url !== '/login') {
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
    const msg = ((errorCode as any)[code] && i18n.global.t((errorCode as any)[code])) || res.data.msg || i18n.global.t(errorCode['default'])

    if (code === 401) {
      if (!isTimeout) {
        isTimeout = true
        Modal.error({
          title: i18n.global.t('components.login-form.5o61dvpc2o00'),
          content: i18n.global.t('components.login-form.5o61dvpc5cs0'),
          okText: i18n.global.t('components.login-form.5o61dvpc5o80'),
          width: 'auto',
          maskClosable: false,
          async onOk () {
            isTimeout = false
            const userStore = useUserStore()

            await userStore.logout()
            window.location.reload()
          }
        })
      }
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
    const { request, message, response } = error
    if (request.responseURL.includes('plugins/base-ops')) {
      if (response.status !== 404) {
        Message.error({
          content: message || 'Error',
          duration: 3 * 1000
        })
      }
    } else {
      Message.error({
        content: message || 'Error',
        duration: 3 * 1000
      })
    }
    return Promise.reject(error)
  }
)
