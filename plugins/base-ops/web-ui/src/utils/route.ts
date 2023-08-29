import { DEFAULT_LAYOUT, ParentView } from '@/router/constants'

const loadView = (view: string) => { // Routing lazy loading
  return () => import(
    /* webpackExclude: /\.d.ts$/ */
    `@/views/${view}`
  )
}

function filterChildren (childrenMap: any[], lastRouter: any = false) {
  let children: any[] = []
  childrenMap.forEach((el) => {
    if (el.children && el.children.length) {
      if (el.component === 'ParentView') {
        el.children.forEach((c: any) => {
          c.path = el.path + '/' + c.path
          if (c.children && c.children.length) {
            children = children.concat(filterChildren(c.children, c))
            return
          }
          children.push(c)
        })
        return
      }
    }
    if (lastRouter) {
      el.path = lastRouter.path + '/' + el.path
    }
    children = children.concat(el)
  })
  return children
}

// Traverse the routing string from the background and convert it to a component object
export function filterAsyncRouter (asyncRouterMap: any[], lastRouter = false, type = false) { // eslint-disable-line
  return asyncRouterMap.filter(route => {
    if (type && route.children) {
      route.children = filterChildren(route.children)
    }
    if (route.component) {
      // Layout Component Special Handling
      if (route.component === 'DEFAULT_LAYOUT') {
        route.component = DEFAULT_LAYOUT
      } else if (route.component === 'ParentView') {
        route.component = ParentView
      } else {
        route.component = loadView(route.component)
      }
    }
    if (route.children != null && route.children && route.children.length) {
      route.children = filterAsyncRouter(route.children, route, type)
    } else {
      delete route['children']
      delete route['redirect']
    }
    return true
  })
}

export function getQuery (url: string) {
  const obj: any = {}
  const i = url.indexOf('?') + 1
  const str = url.substr(i)
  const arr = str.split('&')
  for (let i = 0; i < arr.length; i++) {
    const arr1 = arr[i].split('=')
    obj[arr1[0]] = arr1[1]
  }
  return obj
}
