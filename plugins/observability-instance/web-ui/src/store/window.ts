///
/// Copyright (c) 2023 Huawei Technologies Co.,Ltd.
///

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
