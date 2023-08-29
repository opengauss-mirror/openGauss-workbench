import axios from 'axios'

export function getSysSetting () {
    return axios.get('/system/setting')
}