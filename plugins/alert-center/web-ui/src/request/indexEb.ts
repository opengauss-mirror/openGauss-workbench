
import axios, { AxiosError, AxiosRequestConfig, AxiosResponse } from "axios";
import { ElMessage } from "element-plus";

const observabilityBaseURL = process.env.mode === 'production' ? '/plugins/alert-center' : '';
interface ApiResponse<T = any> {
    code: string | number;
    data: T;
    error: any;
    msg: string;
    config:{
        url:string
    };
}

const handleData = <T>(res: AxiosResponse<ApiResponse<T>, any>) => {
    if (res.status === 200 && res.config.url === '/getOffCpuSvg') {
        return Promise.resolve(res)
    } else {
        if (res.data && (res.data.code === "200" || res.data.code === 200)) {
            return Promise.resolve(res.data.data)
        } else {
            ElMessage.error(res?.data.msg || "请求错误")
            return Promise.reject(res?.data.msg || "请求错误")
        }
    }
}

export class Request {
    constructor(config?: AxiosRequestConfig) {
        if (config) {
            axios.defaults.baseURL = 'http://10.10.9.221:5000'
            for (const key in config) {
                if (key in axios.defaults) {
                    // @ts-ignore
                    axios.defaults[key] = config[key]
                }
            }
        }
    }

    async get<T = any>(url: string, params?: any, config?: AxiosRequestConfig) {
        axios.defaults.baseURL = 'http://10.10.9.221:5000'
        try {
            return handleData<T>(await axios.get<ApiResponse<T>>(`${observabilityBaseURL}${url}`, { params, ...config }))
        } catch (error) {
            const err = error as AxiosError<any>
            if (err.response && err.response.status === 500) {
                ElMessage.error("服务器错误，请稍后重试")
            }
            if (err.response && err.response.data && err.response.data.msg) {
                return Promise.reject(err.response.data.msg) 
            }
            return Promise.reject(error)
        }
    }

    async delete<T = any>(url: string, params?: any, config?: AxiosRequestConfig) {
        axios.defaults.baseURL = 'http://10.10.9.221:5000'
        try {
            return handleData<T>(await axios.delete<ApiResponse<T>>(`${observabilityBaseURL}${url}`, { params, ...config }))
        } catch (error) {
            const err = error as AxiosError<any>
            if (err.response && err.response.data && err.response.data.msg) {
                return Promise.reject(err.response.data.msg) 
            }
            return Promise.reject(error)
        }
    }

    async post<T = any, D = any>(url: string, data?: D, config?: AxiosRequestConfig<D>) {
        axios.defaults.baseURL = 'http://10.10.9.221:5000'
        try {
            return handleData<T>(await axios.post<ApiResponse<T>>(`${observabilityBaseURL}${url}`, data, config))
        } catch (error) {
            const err = error as AxiosError<any>
            if (err.response && err.response.status === 500) {
                ElMessage.error("服务器错误，请稍后重试")
            }
            if (err.response && err.response.data && err.response.data.msg) {
                return Promise.reject(err.response.data.msg) 
            }
            return Promise.reject(error)
        }
    }

    async put<T = any, D = any>(url: string, data?: D, config?: AxiosRequestConfig<D>) {
        axios.defaults.baseURL = 'http://10.10.9.221:5000'
        try {
            return handleData<T>(await axios.put<ApiResponse<T>>(`${observabilityBaseURL}${url}`, data, config))
        } catch (error) {
            const err = error as AxiosError<any>
            if (err.response && err.response.status === 500) {
                ElMessage.error("服务器错误，请稍后重试")
            }
            if (err.response && err.response.data && err.response.data.msg) {
                return Promise.reject(err.response.data.msg) 
            }
            return Promise.reject(error)
        }
    }

    async patch<T = any, D = any>(url: string, data?: D, config?: AxiosRequestConfig<D>) {
        axios.defaults.baseURL = 'http://10.10.9.221:5000'
        try {
            return handleData<T>(await axios.patch<ApiResponse<T>>(`${observabilityBaseURL}${url}`, data, config))
        } catch (error) {
            const err = error as AxiosError<any>
            if (err.response && err.response.status === 500) {
                ElMessage.error("服务器错误，请稍后重试")
            }
            if (err.response && err.response.data && err.response.data.msg) {
                return Promise.reject(err.response.data.msg) 
            }
            return Promise.reject(error)
        }
    }
}

const ogRequest = new Request({
    baseURL: import.meta.env.VITE_BASE_URL
})

export default ogRequest
