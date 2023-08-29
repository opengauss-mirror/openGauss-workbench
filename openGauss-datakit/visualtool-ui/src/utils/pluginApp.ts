import WujieVue from 'wujie-vue3'
import credentialsFetch from './fetch'

const { setupApp, destroyApp } = WujieVue
const degrade = window.localStorage.getItem('degrade') === 'true' || !window.Proxy || !window.CustomElementRegistry

export function createPluginApp (name: string, url = '/') {
  setupApp({
    name,
    url,
    attrs: {},
    exec: true,
    fetch: credentialsFetch,
    alive: true,
    degrade
  })
}

export function destroyPluginApp (name: string) {
  destroyApp(name)
}
