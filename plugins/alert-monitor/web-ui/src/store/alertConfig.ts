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