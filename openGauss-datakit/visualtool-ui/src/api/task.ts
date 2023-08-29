import axios from 'axios'

export function list (query?: any) {
  return axios.get('/sys/task/list/all', { params: query })
}

export function start (id: string | number) {
  return axios.post(`/sys/task/start/${id}`)
}

export function stop (id: string | number) {
  return axios.post(`/sys/task/stop/${id}`)
}

export function deleteTask (ids: string) {
  return axios.delete(`/sys/task/${ids}`)
}

export function taskById (id: string | number) {
  return axios.get(`/sys/task/${id}`)
}
