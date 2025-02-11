import WujieVue from 'wujie-vue3'
import credentialsFetch from './fetch'
import { DocElementRectPlugin } from "wujie-polyfill";

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
    plugins: [DocElementRectPlugin()],
    degrade
  })
}

export function destroyPluginApp (name: string) {
  destroyApp(name)
}
