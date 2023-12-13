import axios from 'axios'

export function syncConfig (payload) {
  return axios.post('/plugins/datasync-mysql/sync/config', payload)
}

export function syncStatus () {
  return axios.get('/plugins/datasync-mysql/sync/getCurSyncStatus')
}
