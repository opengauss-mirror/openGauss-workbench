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

import { defineStore } from "pinia";

interface State {
    showMain: boolean,
    isInstall: boolean,
    isConfig: boolean

}
export const useAlertConfigStore = defineStore("alertConfig", {
    state: (): State => ({
        showMain: true,
        isInstall: false,
        isConfig: false
    }),
    actions: {
        setShow(showMain: boolean) {
            this.$state.showMain = showMain;
        },
        setIsInstall(isInstall: boolean) {
            this.$state.isInstall = isInstall;
        },
        setIsConifg(isConfig: boolean) {
            this.$state.isConfig = isConfig;
        },
        
    }
})