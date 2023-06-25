<template>
    <div class="" style="padding: 0px 15px">
        <div class="search-form head">
            <div class="filter title" style="margin-right: auto">{{ $t('configParam.tabTitle') }}</div>

            <div class="query filter">
                <el-button @click="handleQuery">{{ $t('app.refresh') }}</el-button>
                <el-button type="primary" @click="refreshData('', '0')">{{ $t('app.query') }}</el-button>
            </div>
        </div>
        <div class="list" v-loading="loadingDBData">
            <div class="list-item">
                <div class="item-title">{{ $t('configParam.systemConfig') }}</div>
                <div>
                    <div class="item-list" v-for="item in data.osParamData" :key="item.seqNo">
                        <div class="item-list-left">
                            <div class="item-name">{{ item.paramName }}</div>
                            <el-popover
                                placement="top-start"
                                :title="$t('configParam.paramDesc')"
                                :width="200"
                                trigger="hover"
                                :content="item.paramDetail"
                            >
                                <template #reference>
                                    <el-icon class="detail-btn" :color="'#7d7d7d'" size="18px">
                                        <View />
                                    </el-icon>
                                </template>
                            </el-popover>
                        </div>
                        <div class="item-list-right">
                            <div class="item-value">
                                {{
                                    item.actualValue === undefined || item.actualValue === null
                                        ? '--'
                                        : item.actualValue
                                }}
                            </div>
                            <div class="suggest-btn-container">
                                <el-popover
                                    placement="top-start"
                                    :title="$t('configParam.paramTuning')"
                                    :width="300"
                                    trigger="hover"
                                    v-if="
                                        item.suggestValue != undefined &&
                                        item.suggestValue != null &&
                                        item.suggestValue != '' &&
                                        item.suggestValue != item.actualValue
                                    "
                                >
                                    <template #reference>
                                        <el-icon
                                            class="suggest-btn"
                                            :color="color"
                                            size="18px"
                                            v-if="item.actualValue != item.suggestValue"
                                        >
                                            <Guide />
                                        </el-icon>
                                    </template>
                                    <template #default>
                                        <div
                                            class="demo-rich-conent"
                                            style="display: flex; gap: 16px; flex-direction: column"
                                        >
                                            <div>
                                                <p class="demo-rich-content__name" style="margin: 0; font-weight: 500">
                                                    {{ $t('configParam.suggestValue') }}{{ item.suggestValue }}
                                                </p>
                                                <p
                                                    class="demo-rich-content__mention"
                                                    style="margin: 0; font-size: 14px"
                                                    v-if="item.suggestExplain"
                                                >
                                                    {{ $t('configParam.suggestReason') }}
                                                </p>
                                                <p
                                                    class="demo-rich-content__mention"
                                                    style="margin: 0; font-size: 14px"
                                                    v-if="item.suggestExplain"
                                                >
                                                    {{ item.suggestExplain }}
                                                </p>
                                            </div>
                                        </div>
                                    </template>
                                </el-popover>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="list-item">
                <div class="item-title">{{ $t('configParam.databaseConfig') }}</div>
                <div>
                    <div class="item-list" v-for="item in data.dbParamData" :key="item.seqNo">
                        <div class="item-list-left">
                            <div class="item-name">{{ item.paramName }}</div>
                            <el-popover
                                placement="top-start"
                                :title="$t('configParam.paramDesc')"
                                :width="200"
                                trigger="hover"
                                :content="item.paramDetail"
                            >
                                <template #reference>
                                    <el-icon class="detail-btn" :color="'#7d7d7d'" size="18px">
                                        <View />
                                    </el-icon>
                                </template>
                            </el-popover>
                        </div>
                        <div class="item-list-right">
                            <div class="item-value">
                                {{
                                    item.actualValue === undefined || item.actualValue === null
                                        ? '--'
                                        : item.actualValue
                                }}
                            </div>
                            <div class="suggest-btn-container">
                                <el-popover
                                    placement="top-start"
                                    :title="$t('configParam.paramTuning')"
                                    :width="300"
                                    trigger="hover"
                                    v-if="
                                        item.suggestValue != undefined &&
                                        item.suggestValue != null &&
                                        item.suggestValue != '' &&
                                        item.suggestValue != item.actualValue
                                    "
                                >
                                    <template #reference>
                                        <el-icon
                                            class="suggest-btn"
                                            :color="color"
                                            size="18px"
                                            v-if="item.actualValue != item.suggestValue"
                                        >
                                            <Guide />
                                        </el-icon>
                                    </template>
                                    <template #default>
                                        <div
                                            class="demo-rich-conent"
                                            style="display: flex; gap: 16px; flex-direction: column"
                                        >
                                            <div>
                                                <p class="demo-rich-content__name" style="margin: 0; font-weight: 500">
                                                    {{ $t('configParam.suggestValue') }}{{ item.suggestValue }}
                                                </p>
                                                <p
                                                    class="demo-rich-content__mention"
                                                    style="margin: 0; font-size: 14px"
                                                    v-if="item.suggestExplain"
                                                >
                                                    {{ $t('configParam.suggestReason') }}
                                                </p>
                                                <p
                                                    class="demo-rich-content__mention"
                                                    style="margin: 0; font-size: 14px"
                                                    v-if="item.suggestExplain"
                                                >
                                                    {{ item.suggestExplain }}
                                                </p>
                                            </div>
                                        </div>
                                    </template>
                                </el-popover>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <my-message v-if="errorInfo" type="error" :tip="errorInfo" defaultTip="" />
        <Password :show="snapshotManageShown" @changeModal="changeModalSnapshotManage" @confirm="refreshData" />
    </div>
</template>

<script setup lang="ts">
import { useRequest } from 'vue-request'
import restRequest from '@/request/restful'
import { useI18n } from 'vue-i18n'
import { View, Guide } from '@element-plus/icons-vue'
import Password from '@/pages/dashboardV2/systemConfiguration/password.vue'
import { ElMessage } from 'element-plus'
import { useMonitorStore } from '@/store/monitor'
import { tabKeys } from '@/pages/dashboardV2/common'
import { storeToRefs } from 'pinia'

const { t } = useI18n()

const errorInfo = ref<string | Error>()

const props = withDefaults(defineProps<{ tabId: string }>(), {})
const { updateCounter, sourceType, tabNow } = storeToRefs(useMonitorStore(props.tabId))

const data = reactive<{
    dbParamData: Array<Record<string, string>>
    osParamData: Array<Record<string, string>>
}>({
    dbParamData: [],
    osParamData: [],
})

// same for every page in index
onMounted(() => {
    if (useMonitorStore(props.tabId).instanceId) {
        refreshData('', '0')
    }
    // @ts-ignore
    const wujie = window.$wujie
    // Judge whether it is a plug-in environment or a local environment through wujie
    if (wujie) {
        // Monitoring platform language change
        wujie?.bus.$on('opengauss-locale-change', (val: string) => {
            console.log('log-search catch locale change')
            nextTick(() => {
                if (useMonitorStore(props.tabId).instanceId) {
                    refreshData('', '0')
                }
            })
        })
    }
})
watch(
    updateCounter,
    () => {
        if (tabNow.value === tabKeys.SystemConfig) {
            if (updateCounter.value.source === sourceType.value.INSTANCE) refreshData('', '0')
            if (updateCounter.value.source === sourceType.value.TABCHANGE) refreshData('', '0')
        }
    },
    { immediate: false }
)

// password dialog
const handleQuery = () => {
    if (!useMonitorStore(props.tabId).instanceId) {
        ElMessage({
            showClose: true,
            message: t('configParam.queryValidInfo'),
            type: 'warning',
        })
        return
    }
    showSnapshotManage()
}
const snapshotManageShown = ref(false)
const showSnapshotManage = () => {
    snapshotManageShown.value = true
}
const changeModalSnapshotManage = (val: boolean) => {
    snapshotManageShown.value = val
}

const refreshData = (password: string, isRefresh: string) => {
    if (!useMonitorStore(props.tabId).instanceId) {
        ElMessage({
            showClose: true,
            message: t('configParam.queryValidInfo'),
            type: 'warning',
        })
        return
    }
    requestData(password, isRefresh)
}
const {
    data: res,
    run: requestData,
    loading: loadingDBData,
} = useRequest(
    (password, isRefresh = '0') => {
        return restRequest
            .get('/observability/v1/param/paramInfo', {
                paramName: '',
                nodeId: useMonitorStore(props.tabId).instanceId,
                dbName: null,
                password,
                paramType: '',
                isRefresh,
            })
            .then(function (res) {
                return res
            })
            .catch(function (res) {
                data.dbParamData = []
                data.osParamData = []
            })
    },
    { manual: true }
)
watch(res, (res) => {
    if (res && res.length > 0) {
        data.dbParamData = res.filter((item: any) => item.paramType === 'DB')
        data.osParamData = res.filter((item: any) => item.paramType === 'OS')
    }
})
const color = computed(() => {
    if (localStorage.getItem('theme') === 'dark') return '#fcef92'
    else return '#E41D1D'
})
</script>

<style scoped lang="scss">
.head {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    .title {
        font-size: 16px;
        font-weight: bold;
    }
}
.list {
    display: flex;
    flex-direction: row;
    .list-item {
        width: 50%;
        .item-title {
            font-size: 14px;
            font-weight: bold;
            margin-bottom: 5px;
        }
        .item-list {
            display: flex;
            margin: 5px 0px;
            .item-list-left {
                width: 55%;
                display: flex;
                align-items: center;
                flex-shrink: 0;
                .detail-btn {
                    margin-left: 10px;
                }
            }
            .item-list-right {
                width: 40%;
                display: flex;
                align-items: center;
                overflow: hidden;
                padding-right: 20px;
                padding-left: 20px;
                vertical-align: middle;
                .item-value {
                    display: inline-block;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    text-align: left;
                }
                .suggest-btn-container {
                    width: 20px;
                    display: flex;
                }
                .suggest-btn {
                    margin-left: 5px;
                }
            }
        }
    }
}
</style>
