import type { RouteRecordNormalized } from 'vue-router'
import { filterAsyncRouter } from '@/utils/route'
import i18n from '@/locale/index'

const context = require.context('./modules', true, /\.ts$/)
const modules = context.keys()
const localeRoutes = localStorage.getItem('opengauss-routes')

function formatModules (_modules: any, result: RouteRecordNormalized[]) {
  if (localeRoutes) {
    const data = JSON.parse(localeRoutes)
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
    return routesRemovedChildIcon
  } else {
    _modules.forEach((key: any) => {
      result = result.concat(context(key).default)
    })
    console.log(result)
    return result
  }
}

export const appRoutes: RouteRecordNormalized[] = formatModules(modules, [])

// Avoid circular references, put here

export const DEFAULT_ROUTE_NAME = 'Dashboard'

export const DEFAULT_ROUTE = {
  title: i18n.global.t('components.login-form.5o62d201k380'),
  name: DEFAULT_ROUTE_NAME,
  fullPath: '/dashboard'
}
