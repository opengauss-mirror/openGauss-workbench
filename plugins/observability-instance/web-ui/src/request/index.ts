import axios, { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

const observabilityBaseURL = process.env.mode === 'production' ? '/plugins/observability-instance' : ''

export interface ApiResponse<T = any> {
  code: string | number
  data: T
  error: any
  msg: string
}

const handleData = <T>(res: AxiosResponse<ApiResponse<T>, any>) => {
  if (res.data.code === '200' || res.data.code === 200) {
    if (res.data.data) return Promise.resolve(res.data.data)
    else return Promise.resolve()
  } else {
    if (res.data.code === '500' || res.data.code === 500) {
      ElMessage.error(res?.data.msg || 'Request Error')
      throw new Error(res?.data.msg || 'Request Error');
    } else {
      return Promise.reject(res)
    }
  }
}

axios.defaults.timeout = 120000;

export class Request {
  constructor(config?: AxiosRequestConfig) {
    if (config) {
      for (const key in config) {
        if (key in axios.defaults) {
          // @ts-ignore
          axios.defaults[key] = config[key]
        }
      }
    }
  }

  async request<T = any>(
    method: 'get' | 'delete' | 'post' | 'patch' | 'put',
    url: string,
    params?: any,
    data?: any,
    config?: AxiosRequestConfig
  ) {
    const token = localStorage.getItem('opengauss-token')
    if (token) {
      if (!config) {
        config = {}
      }
      if (!config.headers) {
        config.headers = {}
      }
      config.headers.Authorization = `Bearer ${token}`
      config.headers['Content-Language'] = localStorage.getItem('locale') === 'en-US' ? 'en_US' : 'zh_CN'
    }
    try {
      let response
      if (method === 'get') {
        response = await axios.get<ApiResponse<T>>(`${observabilityBaseURL}${url}`, { params, ...config })
      } else if (method === 'post') {
        response = await axios.post<ApiResponse<T>>(`${observabilityBaseURL}${url}`, data, config)
      } else if (method === 'delete') {
        response = await axios.delete<ApiResponse<T>>(`${observabilityBaseURL}${url}`, { params, ...config })
      } else if (method === 'patch') {
        response = await axios.patch<ApiResponse<T>>(`${observabilityBaseURL}${url}`, data, config)
      } else if (method === 'put') {
        response = await axios.put<ApiResponse<T>>(`${observabilityBaseURL}${url}`, data, config)
      }
      if (response) return handleData<T>(response)
      else {
        const reason = 'not support method'
        return Promise.reject(reason)
      }
    } catch (error) {
      const err = error as AxiosError<any>
      if (err.response && err.response.data && err.response.data.msg) {
        return Promise.reject(err.response.data.msg)
      }
      return Promise.reject(error)
    }
  }

  async get<T = any>(url: string, params?: any, config?: AxiosRequestConfig) {
    return this.request<T>('get', url, params, null, config)
  }

  async delete<T = any>(url: string, params?: any, config?: AxiosRequestConfig) {
    return this.request<T>('delete', url, params, null, config)
  }

  async post<T = any, D = any>(url: string, data?: D, config?: AxiosRequestConfig<D>) {
    return this.request<T>('post', url, null, data, config)
  }

  async put<T = any, D = any>(url: string, data?: D, config?: AxiosRequestConfig<D>) {
    return this.request<T>('put', url, null, data, config)
  }

  async patch<T = any, D = any>(url: string, data?: D, config?: AxiosRequestConfig<D>) {
    return this.request<T>('patch', url, null, data, config)
  }
}

const ogRequest = new Request({
  baseURL: import.meta.env.VITE_BASE_URL,
})

export default ogRequest
