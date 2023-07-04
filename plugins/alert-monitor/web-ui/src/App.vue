<template>
    <el-config-provider size="small" :locale="$i18n.locale === 'zhCn' ? zhCn : en">
        <div class="container">
            <aside v-if="!isFromObservability">
                <my-menu :collapse="collapse" />
            </aside>
            <div class="main" ref="mainDiv">
                <div class="page" v-if="alertConfigEnv.showMain"><router-view />
                </div>
                <AlertConfig class="page" v-else />
            </div>
        </div>
    </el-config-provider>
</template>

<script setup lang="ts">
import zhCn from "element-plus/lib/locale/lang/zh-cn";
import en from "element-plus/lib/locale/lang/en";
import { useDark, useToggle } from "@vueuse/core";
import { useWindowStore } from "@/store/window";
import router from '@/router';
import { i18n } from '@/i18n'
import { useMonitorStore } from "@/store/monitor";
import { useAlertConfigStore } from "@/store/alertConfig";
import AlertConfig from "@/components/AlertConfig.vue";
import { checkPrometheus } from "@/api/environment";
import { getAlertConf } from "@/api/alertConfig"

const observabilitySign = 'alert-monitor';

const themeBool = ref<boolean>(false)

const isDark = useDark({
    // Key stored in localStorage/sessionStorage, modify according to your own needs.
    storageKey: 'theme',
    valueDark: 'dark',
    valueLight: 'light',
})

useToggle(isDark);

const collapse = ref(false)
const isFromObservability = ref<boolean>(false);
const mainDiv = ref<HTMLDivElement | null>();
const windowStore = useWindowStore();
const monitorStore = useMonitorStore();
const alertConfigEnv = useAlertConfigStore();

// wujie frame special params
const getWujieParam = () => {
    // @ts-ignore
    if (window.$wujie) {
        // @ts-ignore
        const { id } = window.$wujie.props;
        if (typeof id === 'string' && id !== '') {
            monitorStore.instanceId = id;
        }
    }
}

const dealUrlTo = () => {
    let urlTo = '/vem';
    const curUrl = window.location.href;
    if (curUrl.indexOf(observabilitySign) > -1) {
        isFromObservability.value = true;
        if (curUrl.indexOf('/vem') > -1) {
            urlTo = curUrl.slice(curUrl.indexOf('/vem'));
        }
    }
    router.replace(urlTo);
}

const onThemeChange = (bool: boolean, themeType: string) => {
    themeBool.value = bool;
    localStorage.setItem("theme", themeType)
    windowStore.setTheme(themeType);
}

const wujieInit = () => {
    if (localStorage.getItem("locale") === "en-US") {
        i18n.global.locale.value = 'en';
    } else {
        i18n.global.locale.value = 'zhCn';
    }
    if (localStorage.getItem("opengauss-theme") === "dark") {
        onThemeChange(false, 'dark');
    } else {
        onThemeChange(true, 'auto');
    }
}

onMounted(async () => {
    new ResizeObserver(() => {
        windowStore.setMainHeight(mainDiv.value!.clientHeight);
        windowStore.setMainWidth(mainDiv.value!.clientWidth);
    }).observe(mainDiv.value!);
    dealUrlTo();
    getWujieParam();

    // @ts-ignore
    const wujie = window.$wujie;
    // Judge whether it is a plug-in environment or a local environment through wujie
    if (wujie) {
        localStorage.setItem("INSTANCE_CURRENT_MODE", "wujie")

        // init theme and language
        wujieInit();

        // Monitoring platform theme changes
        wujie?.bus.$on('opengauss-theme-change', (val: string) => {
            if (val === 'dark') {
                onThemeChange(false, 'dark');
            } else {
                onThemeChange(true, 'auto');
            }
        })

        // Monitoring platform language change
        wujie?.bus.$on('opengauss-locale-change', (val: string) => {
            if (val === "en-US") {
                i18n.global.locale.value = 'en';
            } else {
                i18n.global.locale.value = 'zhCn';
            }
        })
    } else {
        // local default 
        localStorage.setItem("INSTANCE_CURRENT_MODE", "local")
    }
    if (!alertConfigEnv.isInstall || !alertConfigEnv.isConfig) {
        if (!alertConfigEnv.isInstall) {
            await checkPrometheus().then(async (res: any) => {
                if (res && res.code === 200) {
                    alertConfigEnv.isInstall = true
                } else {
                    alertConfigEnv.isInstall = false
                }
            }).catch(() => {
                alertConfigEnv.isInstall = false
            })
        }
        if (!alertConfigEnv.isConfig) {
            await getAlertConf().then(async (res: any) => {
                if (res && res.code === 200 && res.data && res.data.alertIp && res.data.alertPort) {
                    alertConfigEnv.isConfig = true
                } else {
                    alertConfigEnv.isConfig = false
                }
            }).catch(() => {
                alertConfigEnv.isConfig = false
            })
        }
        alertConfigEnv.showMain = alertConfigEnv.isInstall && alertConfigEnv.isConfig
    }
})
</script>

<style scoped lang="scss">
.container {
    height: 100%;
    overflow: hidden;
    display: flex;
    flex-flow: row nowrap;
}

.main {
    flex: 1;
    min-width: 0;
    background-color: var(--el-bg-color);
    display: flex;
    flex-direction: column;
}

.page {
    padding: 16px;
    height: calc(100% - var(--vem-header-height));
    overflow: auto;
}
</style>
