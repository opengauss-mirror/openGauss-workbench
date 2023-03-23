import axios from 'axios'

export function list (query) {
  return axios.get('/plugins/data-migration/migration/list', { params: query })
}

export function start (id) {
  return axios.post(`/plugins/data-migration/migration/start/${id}`)
}

export function stop (id) {
  return axios.post(`/plugins/data-migration/migration/finish/${id}`)
}

export function deleteTask (ids) {
  return axios.delete(`/plugins/data-migration/migration/${ids}`)
}

export function userList () {
  return axios.get('/plugins/data-migration/migration/list/createUsers')
}
