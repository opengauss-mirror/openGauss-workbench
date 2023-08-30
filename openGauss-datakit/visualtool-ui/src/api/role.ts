import axios from 'axios'

export function getRoleList (query: any) {
  return axios.get('/system/role/list', {
    params: query
  })
}

export function createRole (payload: any) {
  return axios.post('/system/role', payload)
}

export function updateRole (payload: any) {
  return axios.put('/system/role', payload)
}

export function changeStatus (payload: any) {
  return axios.put('/system/role/changeStatus', payload)
}

export function menuTree (payload: any) {
  return axios.get('/system/menu/treeselect', {
    params: payload
  })
}

export function roleDelete (roleId: string | number) {
  return axios.delete(`/system/role/${roleId}`)
}

export function roleMenuTree (roleId: string | number) {
  return axios.get(`/system/menu/roleMenuTreeselect/${roleId}`)
}
