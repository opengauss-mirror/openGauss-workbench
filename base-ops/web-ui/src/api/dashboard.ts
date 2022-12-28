import axios from 'axios'

export function instanceRoute () {
  return axios.get('/getIndexInstanceRouters')
}
