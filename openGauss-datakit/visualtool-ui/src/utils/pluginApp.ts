/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

import WujieVue from 'wujie-vue3'
import credentialsFetch from './fetch'
import { DocElementRectPlugin } from "wujie-polyfill";

const { setupApp, destroyApp } = WujieVue
const degrade = window.localStorage.getItem('degrade') === 'true' || !window.Proxy || !window.CustomElementRegistry

export function createPluginApp (name: string, url = '/') {
  const actualUrl = process.env.NODE_ENV === 'development' ? `//localhost:8081${url}` : url
  setupApp({
    name,
    url: actualUrl,
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
