import { defineStore } from 'pinia'
import {
  logout as userLogout,
  getUserInfo
} from '@/api/user'
import { clearToken } from '@/utils/auth'
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

    logoutCallBack () {
      const appStore = useAppStore()
      this.resetInfo()
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
