import { defineStore } from 'pinia'
import type { RouteRecordNormalized } from 'vue-router'
import defaultSettings from '@/config/settings.json'
import { getMenuList } from '@/api/user'
import { filterAsyncRouter } from '@/utils/route'
import { AppState } from './types'

const useAppStore = defineStore('app', {
  state: (): AppState => ({ ...defaultSettings }),

  getters: {
    appCurrentSetting (state: AppState): AppState {
      return { ...state }
    },
    appDevice (state: AppState) {
      return state.device
    },
    appAsyncMenus (state: AppState): RouteRecordNormalized[] {
      return state.serverMenu as unknown as RouteRecordNormalized[]
    }
  },

  actions: {
    // Update app settings
    updateSettings (partial: Partial<AppState>) {
      // @ts-ignore-next-line
      this.$patch(partial)
    },
    // Change theme color
    toggleTheme (dark: boolean) {
      if (dark) {
        this.theme = 'dark'
        document.body.setAttribute('arco-theme', 'dark')
        localStorage.setItem('opengauss-theme', 'dark')
      } else {
        this.theme = 'light'
        document.body.removeAttribute('arco-theme')
        localStorage.removeItem('opengauss-theme')
      }
    },
    toggleDevice (device: string) {
      this.device = device
    },
    toggleMenu (value: boolean) {
      this.hideMenu = value
    },
    async fetchServerMenuConfig () {
      const locale = localStorage.getItem('locale') || 'zh-CN'
      try {
        const { data } = await getMenuList()
        data.unshift({
          path: '/',
          name: 'Home',
          component: 'DEFAULT_LAYOUT',
          redirect: '/dashboard',
          meta: {
            title: locale === 'zh-CN' ? '首页' : 'Home',
            requiresAuth: false,
            icon: 'home',
            order: 0,
            hideChildrenInMenu: true
          },
          children: [
            {
              path: '/dashboard',
              name: 'Dashboard',
              component: 'ops/home/Index',
              meta: {
                title: locale === 'zh-CN' ? '首页' : 'Home',
                requiresAuth: true,
                roles: ['*']
              }
            }
          ]
        })
        localStorage.setItem('opengauss-routes', JSON.stringify(data))
        const routes = filterAsyncRouter(data)
        const deepRemoveChildIcon = (ary: any[]) => ary.map((val: any) => {
          if (val.children) {
            return {
              ...val,
              children: val.children.map((child: any) => {
                return {
                  ...child,
                  meta: {
                    ...child.meta,
                    icon: null
                  }
                }
              })
            }
          } else {
            return val
          }
        })
        const routesRemovedChildIcon = deepRemoveChildIcon(routes || [])
        this.serverMenu = routesRemovedChildIcon
      } catch (error) {
        console.log(error)
      }
    },
    clearServerMenu () {
      this.serverMenu = []
    }
  }
})

export default useAppStore
