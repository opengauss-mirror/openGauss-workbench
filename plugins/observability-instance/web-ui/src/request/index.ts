import axios, { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios'

const observabilityBaseURL = process.env.mode === 'production' ? '/plugins/observability-instance' : ''

export interface ApiResponse<T = any> {
    code: string | number
    data: T
    error: any
    msg: string
}

const handleData = <T>(res: AxiosResponse<ApiResponse<T>, any>) => {
    if (res.data && (res.data.code === '200' || res.data.code === 200)) {
        return Promise.resolve(res.data.data)
    } else {
        return Promise.reject(res?.data.msg || 'Request Error').catch((err) => {
            console.error(err)
        })
    }
}
const handleDataToHandleError = <T>(res: AxiosResponse<ApiResponse<T>, any>) => {
    if (res.data) {
        return Promise.resolve(res.data)
    } else {
        return Promise.reject(res?.data || 'Request Error').catch((err) => {
            console.error(err)
        })
    }
}

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

    //
    async getAndHandleError<T = any>(url: string, params?: any, config?: AxiosRequestConfig) {
        const token = localStorage.getItem('opengauss-token')
        if (token) {
            if (!config) {
                config = {}
            }
            if (!config.headers) {
                config.headers = {}
            }
            config.headers.Authorization = `Bearer ${token}`
        }
        try {
            return handleDataToHandleError<T>(
                await axios.get<ApiResponse<T>>(`${observabilityBaseURL}${url}`, { params, ...config })
            )
        } catch (error) {
            const err = error as AxiosError<any>
            if (err.response && err.response.data && err.response.data.msg) {
                return Promise.reject(err.response.data.msg)
            }
            return Promise.reject(error)
        }
    }

    async getNative<T = any>(url: string, params?: any, config?: AxiosRequestConfig) {
        const token = localStorage.getItem('opengauss-token')
        if (token) {
            if (!config) {
                config = {}
            }
            if (!config.headers) {
                config.headers = {}
            }
            config.headers.Authorization = `Bearer ${token}`
        }
        try {
            return await axios.get<ApiResponse<T>>(`${observabilityBaseURL}${url}`, { params, ...config })
        } catch (error) {
            const err = error as AxiosError<any>
            if (err.response && err.response.data && err.response.data.msg) {
                return Promise.reject(err.response.data.msg)
            }
            return Promise.reject(error)
        }
    }

    async get<T = any>(url: string, params?: any, config?: AxiosRequestConfig) {
        const token = localStorage.getItem('opengauss-token')
        if (token) {
            if (!config) {
                config = {}
            }
            if (!config.headers) {
                config.headers = {}
            }
            config.headers.Authorization = `Bearer ${token}`
        }
        try {
            return handleData<T>(
                await axios.get<ApiResponse<T>>(`${observabilityBaseURL}${url}`, { params, ...config })
            )
        } catch (error) {
            const err = error as AxiosError<any>
            if (err.response && err.response.data && err.response.data.msg) {
                return Promise.reject(err.response.data.msg)
            }
            return Promise.reject(error)
        }
    }

    async delete<T = any>(url: string, params?: any, config?: AxiosRequestConfig) {
        const token = localStorage.getItem('opengauss-token')
        if (token) {
            if (!config) {
                config = {}
            }
            if (!config.headers) {
                config.headers = {}
            }
            config.headers.Authorization = `Bearer ${token}`
        }
        try {
            return handleData<T>(
                await axios.delete<ApiResponse<T>>(`${observabilityBaseURL}${url}`, { params, ...config })
            )
        } catch (error) {
            const err = error as AxiosError<any>
            if (err.response && err.response.data && err.response.data.msg) {
                return Promise.reject(err.response.data.msg)
            }
            return Promise.reject(error)
        }
    }

    async post<T = any, D = any>(url: string, data?: D, config?: AxiosRequestConfig<D>) {
        const token = localStorage.getItem('opengauss-token')
        if (token) {
            if (!config) {
                config = {}
            }
            if (!config.headers) {
                config.headers = {}
            }
            config.headers.Authorization = `Bearer ${token}`
        }
        try {
            return handleData<T>(await axios.post<ApiResponse<T>>(`${observabilityBaseURL}${url}`, data, config))
        } catch (error) {
            const err = error as AxiosError<any>
            if (err.response && err.response.data && err.response.data.msg) {
                return Promise.reject(err.response.data.msg)
            }
            return Promise.reject(error)
        }
    }

    async put<T = any, D = any>(url: string, data?: D, config?: AxiosRequestConfig<D>) {
        const token = localStorage.getItem('opengauss-token')
        if (token) {
            if (!config) {
                config = {}
            }
            if (!config.headers) {
                config.headers = {}
            }
            config.headers.Authorization = `Bearer ${token}`
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

    async patch<T = any, D = any>(url: string, data?: D, config?: AxiosRequestConfig<D>) {
        const token = localStorage.getItem('opengauss-token')
        if (token) {
            if (!config) {
                config = {}
            }
            if (!config.headers) {
                config.headers = {}
            }
            config.headers.Authorization = `Bearer ${token}`
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
