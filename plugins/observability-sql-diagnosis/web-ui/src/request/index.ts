import axios, { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

const observabilityBaseURL = process.env.mode === 'production' ? '/plugins/observability-sql-diagnosis' : ''

interface ApiResponse<T = any> {
    code: string | number
    data: T
    error: any
    msg: string
}

const handleData = <T>(res: AxiosResponse<ApiResponse<T>, any>) => {
    if (res.data && (res.data.code === '200' || res.data.code === 200)) {
        return Promise.resolve(res.data.data)
    } else {
        return Promise.reject(res?.data.msg || 'Request Error')
    }
}

const handleRESTfulData = (res: any) => {
    if (isSuccessResponse(res)) {
        return Promise.resolve(res.data)
    } else {
        if (!extraConfig.notTip) {
            ElMessage.error(res?.data.msg || 'Request Error')
        }
        return Promise.reject(res?.data?.msg || 'Request Error')
    }
}

const isSuccessResponse = (res: any) => {
    if (res.status === 200) {
        if (res.data === undefined) {
            return true
        } else if (Object.keys(res.data).length === 2 && typeof res.data.code !== 'undefined' && typeof res.data.msg !== 'undefined' && res.data.code !== 200) {
            return false
        } else return true
    } else return false
}

let extraConfig = {};

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

    async get<T = any>(url: string, params?: any, config?: AxiosRequestConfig, extra = {} as any) {
        extraConfig = extra
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
            return handleRESTfulData<T>(await axios.get<T>(`${observabilityBaseURL}${url}`, { params, ...config }))
        } catch (error) {
            const err = error as AxiosError<any>
            if (err.response && err.response.data && err.response.data.msg) {
                return Promise.reject(err.response.data.msg)
            }
            return Promise.reject(error)
        }
    }

    async delete<T = any>(url: string, params?: any, config?: AxiosRequestConfig, extra = {} as any) {
        extraConfig = extra
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
            return handleRESTfulData<T>(await axios.delete<T>(`${observabilityBaseURL}${url}`, { params, ...config }))
        } catch (error) {
            const err = error as AxiosError<any>
            if (err.response && err.response.data && err.response.data.msg) {
                return Promise.reject(err.response.data.msg)
            }
            return Promise.reject(error)
        }
    }

    async post<T = any, D = any>(url: string, data?: D, config?: AxiosRequestConfig<D>, extra = {} as any) {
        extraConfig = extra
        const token = localStorage.getItem('opengauss-token')
        if (token) {
            if (!config) {
                config = {}
            }
            if (config.notTip) {
                notTip = config.notTip
                config.notTip = undefined
            }
            if (!config.headers) {
                config.headers = {}
            }
            config.headers.Authorization = `Bearer ${token}`
            config.headers['Content-Language'] = localStorage.getItem('locale') === 'en-US' ? 'en_US' : 'zh_CN'
        }
        try {
            return handleRESTfulData<T>(await axios.post<T>(`${observabilityBaseURL}${url}`, data, config))
        } catch (error) {
            const err = error as AxiosError<any>
            if (err.response && err.response.data && err.response.data.msg) {
                return Promise.reject(err.response.data.msg)
            }
            return Promise.reject(error)
        }
    }

    async put<T = any, D = any>(url: string, data?: D, config?: AxiosRequestConfig<D>, extra = {} as any) {
        extraConfig = extra
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
            return handleData<T>(await axios.put<ApiResponse<T>>(`${observabilityBaseURL}${url}`, data, config))
        } catch (error) {
            const err = error as AxiosError<any>
            if (err.response && err.response.data && err.response.data.msg) {
                return Promise.reject(err.response.data.msg)
            }
            return Promise.reject(error)
        }
    }

    async patch<T = any, D = any>(url: string, data?: D, config?: AxiosRequestConfig<D>, extra = {} as any) {
        extraConfig = extra
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
            return handleData<T>(await axios.patch<ApiResponse<T>>(`${observabilityBaseURL}${url}`, data, config))
        } catch (error) {
            const err = error as AxiosError<any>
            if (err.response && err.response.data && err.response.data.msg) {
                return Promise.reject(err.response.data.msg)
            }
            return Promise.reject(error)
        }
    }
}

const ogRequest = new Request({
    baseURL: import.meta.env.VITE_BASE_URL,
})

export default ogRequest
