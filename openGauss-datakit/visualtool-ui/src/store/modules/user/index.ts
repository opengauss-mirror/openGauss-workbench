import { defineStore } from 'pinia'
import {
  login as userLogin,
  logout as userLogout,
  getUserInfo,
  LoginData
} from '@/api/user'
import {setToken, clearToken, clearPublckey} from '@/utils/auth'
import { removeRouteListener } from '@/utils/route-listener'
import useAppStore from '../app'

const useUserStore = defineStore('user', {
  state: (): any => ({
    userName: '',
    nickName: '',
    userId: undefined,
    phonenumber: '',
    email: undefined,
    remark: undefined,
    avatar: undefined
  }),

  getters: {
    userInfo (state: any): any {
      return { ...state }
    }
  },

  actions: {
    // Set user's information
    setInfo (partial: any) {
      this.$patch(partial)
    },

    // Reset user's information
    resetInfo () {
      this.$reset()
    },

    // Get user's information
    async info () {
      const res = await getUserInfo()

      this.setInfo(res.data)
    },

    // Login
    async login (loginForm: LoginData) {
      try {
        const res: any = await userLogin(loginForm)
        setToken(res.token)
      } catch (err) {
        clearPublckey()
        clearToken()
        throw err
      }
    },
    logoutCallBack () {
      const appStore = useAppStore()
      this.resetInfo()
      clearPublckey()
      clearToken()
      removeRouteListener()
      appStore.clearServerMenu()
    },
    // Logout
    async logout () {
      try {
        await userLogout()
      } finally {
        this.logoutCallBack()
      }
    }
  }
})

export default useUserStore
