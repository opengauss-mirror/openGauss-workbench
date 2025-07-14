import { defineStore } from 'pinia'
import type { RouteRecordNormalized } from 'vue-router'
import defaultSettings from '@/config/settings.json'
import { getMenuList } from '@/api/user'
import { filterAsyncRouter } from '@/utils/route'
import { AppState } from './types'
import i18n from '@/locale/index'

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
        document.body.setAttribute('theme', 'dark')
        localStorage.setItem('opengauss-theme', 'dark')
      } else {
        this.theme = 'light'
        document.body.removeAttribute('arco-theme')
        document.body.removeAttribute('theme')
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
      try {
        const { data } = await getMenuList()
        if(!data.some((item: any) => item && item.name === 'Static-pluginBase-opsOps')) {
          let found = false;
          for (let i = 0; i < data.length && !found; i++) {
            if (data[i].name === 'Resource') {
              if(data[i].children[3].name === 'ResourcePackage') {
                data[i].children[3].meta.hideInMenu = true
                found = true
              }
            }
          }
        }
        data.unshift({
          path: '/',
          name: 'Home',
          component: 'DEFAULT_LAYOUT',
          redirect: '/dashboard',
          meta: {
            title: i18n.global.t('components.login-form.5o62d201k380'),
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
                title: i18n.global.t('components.login-form.5o62d201k380'),
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
