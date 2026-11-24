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

import { defineStore } from 'pinia';

export const useWindowStore = defineStore('window', {
    state() {
        return {
            mainHeight: 0,
            mainWidth: 0,
            serverInfoText: '',
            theme: 'auto', // dark or auto
        }
    },
    actions: {
        setMainHeight(height: number) {
            this.$state.mainHeight = height;
        },
        setMainWidth(width: number) {
            this.$state.mainWidth = width;
        },
        setServerInfoText(value: string) {
            this.$state.serverInfoText = value;
        },
        setTheme(value: string) {
            this.$state.theme = value;
        }   
    }
});
