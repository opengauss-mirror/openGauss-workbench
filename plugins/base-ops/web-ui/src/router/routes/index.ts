import type { RouteRecordNormalized } from 'vue-router'

const context = require.context('./modules', true, /\.ts$/)
const modules = context.keys()

function formatModules (_modules: any, result: RouteRecordNormalized[]) {
  _modules.forEach((key: any) => {
    result = result.concat(context(key).default)
  })
  console.log(result)
  return result
}

export const appRoutes: RouteRecordNormalized[] = formatModules(modules, [])
