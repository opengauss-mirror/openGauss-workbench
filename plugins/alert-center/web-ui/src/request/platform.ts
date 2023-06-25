import axios, { AxiosError, AxiosRequestConfig, AxiosResponse } from "axios";

const platformBaseURL = "";

interface ApiResponse<T = any> {
    code: string | number;
    data: T;
    error: any;
    msg: string;
}

const handleData = <T>(res: AxiosResponse<ApiResponse<T>, any>) => {
    if (res.data && (res.data.code === "200" || res.data.code === 200)) {
        return Promise.resolve(res.data.data);
    } else {
        return Promise.reject(res?.data.msg || "Request Error").catch((err) => {
            console.log(err);
        });
    }
};

const handleRESTfulData = <T>(res: AxiosResponse<T, any>) => {
    return Promise.resolve(res.data);
};

export class Request {
    constructor(config?: AxiosRequestConfig) {
        if (config) {
            for (const key in config) {
                if (key in axios.defaults) {
                    // @ts-ignore
                    axios.defaults[key] = config[key];
                }
            }
        }
    }

    async getNative<T = any>(url: string, params?: any, config?: AxiosRequestConfig) {
        const token = localStorage.getItem("opengauss-token");
        if (token) {
            if (!config) {
                config = {};
            }
            if (!config.headers) {
                config.headers = {};
            }
            config.headers.Authorization = `Bearer ${token}`;
        }
        try {
            return await axios.get<ApiResponse<T>>(`${platformBaseURL}${url}`, { params, ...config });
        } catch (error) {
            const err = error as AxiosError<any>;
            if (err.response && err.response.data && err.response.data.msg) {
                return Promise.reject(err.response.data.msg);
            }
            return Promise.reject(error);
        }
    }

    async get<T = any>(url: string, params?: any, config?: AxiosRequestConfig) {
        const token = localStorage.getItem("opengauss-token");
        if (token) {
            if (!config) {
                config = {};
            }
            if (!config.headers) {
                config.headers = {};
            }
            config.headers.Authorization = `Bearer ${token}`;
        }
        try {
            return handleRESTfulData<T>(await axios.get<T>(`${platformBaseURL}${url}`, { params, ...config }));
        } catch (error) {
            const err = error as AxiosError<any>;
            if (err.response && err.response.data && err.response.data.msg) {
                return Promise.reject(err.response.data.msg);
            }
            return Promise.reject(error);
        }
    }

    async delete<T = any>(url: string, params?: any, config?: AxiosRequestConfig) {
        const token = localStorage.getItem("opengauss-token");
        if (token) {
            if (!config) {
                config = {};
            }
            if (!config.headers) {
                config.headers = {};
            }
            config.headers.Authorization = `Bearer ${token}`;
        }
        try {
            return handleRESTfulData<T>(await axios.delete<T>(`${platformBaseURL}${url}`, { params, ...config }));
        } catch (error) {
            const err = error as AxiosError<any>;
            if (err.response && err.response.data && err.response.data.msg) {
                return Promise.reject(err.response.data.msg);
            }
            return Promise.reject(error);
        }
    }

    async post<T = any, D = any>(url: string, data?: D, config?: AxiosRequestConfig<D>) {
        const token = localStorage.getItem("opengauss-token");
        if (token) {
            if (!config) {
                config = {};
            }
            if (!config.headers) {
                config.headers = {};
            }
            config.headers.Authorization = `Bearer ${token}`;
        }
        try {
            return handleRESTfulData<T>(await axios.post<T>(`${platformBaseURL}${url}`, data, config));
        } catch (error) {
            const err = error as AxiosError<any>;
            if (err.response && err.response.data && err.response.data.msg) {
                return Promise.reject(err.response.data.msg);
            }
            return Promise.reject(error);
        }
    }

    async put<T = any, D = any>(url: string, data?: D, config?: AxiosRequestConfig<D>) {
        const token = localStorage.getItem("opengauss-token");
        if (token) {
            if (!config) {
                config = {};
            }
            if (!config.headers) {
                config.headers = {};
            }
            config.headers.Authorization = `Bearer ${token}`;
        }
        try {
            return handleData<T>(await axios.put<ApiResponse<T>>(`${platformBaseURL}${url}`, data, config));
        } catch (error) {
            const err = error as AxiosError<any>;
            if (err.response && err.response.data && err.response.data.msg) {
                return Promise.reject(err.response.data.msg);
            }
            return Promise.reject(error);
        }
    }

    async patch<T = any, D = any>(url: string, data?: D, config?: AxiosRequestConfig<D>) {
        const token = localStorage.getItem("opengauss-token");
        if (token) {
            if (!config) {
                config = {};
            }
            if (!config.headers) {
                config.headers = {};
            }
            config.headers.Authorization = `Bearer ${token}`;
        }
        try {
            return handleData<T>(await axios.patch<ApiResponse<T>>(`${platformBaseURL}${url}`, data, config));
        } catch (error) {
            const err = error as AxiosError<any>;
            if (err.response && err.response.data && err.response.data.msg) {
                return Promise.reject(err.response.data.msg);
            }
            return Promise.reject(error);
        }
    }
}

const platformRequest = new Request({
    baseURL: import.meta.env.VITE_BASE_URL,
});

export default platformRequest;
