const TOKEN_KEY = 'opengauss-token'

const isLogin = () => {
  return !!localStorage.getItem(TOKEN_KEY)
}

const getToken = () => {
  return localStorage.getItem(TOKEN_KEY)
}

const setToken = (token: string) => {
  localStorage.setItem(TOKEN_KEY, token)
}

const clearToken = () => {
  localStorage.removeItem(TOKEN_KEY)
}

const getPublckey = () => {
  return localStorage.getItem('publckey')
}

const setPublckey = (Publckey: string) => {
  localStorage.setItem('publckey', Publckey)
}

const clearPublckey = () => {
  localStorage.removeItem('publckey')
}

export { isLogin, getToken, setToken, clearToken, getPublckey, setPublckey, clearPublckey }
