import axios from 'axios'
import qs from 'query-string'
export function listOperLog (params: any) {
  return axios.get('/system/operlog/list', {
    params,
    paramsSerializer: function (p) {
      return qs.stringify(p)
    }
  })
}
