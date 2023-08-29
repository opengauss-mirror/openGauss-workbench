import axios from 'axios'

export function getWhiteList (query: any) {
  return axios.get('/sys/whiteList/list', {
    params: query
  })
}

export function createWhiteList (payload: any) {
  return axios.post('/sys/whiteList', payload)
}

export function updateWhiteList (payload: any) {
  return axios.put('/sys/whiteList', payload)
}

export function whiteListDelete (id: string | number) {
  return axios.delete(`/sys/whiteList/${id}`)
}
