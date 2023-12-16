<template>
    <a-drawer :width="1200" :mask-closable="false" :footer="false" :visible="data.visible" @cancel="handleClose"
        :unmount-on-close="true" :closable="!data.queryLoading && !data.saveLoading">
        <template #title>
            {{ $t('operation.DailyOps.guc5cg0') }}-{{ data.info }}
        </template>
        <a-spin class="full-w" :loading="data.queryLoading || data.saveLoading" :tip="getLoadingTip()">
            <div class="flex-between mb-s">
                <div class="flex-row">
                    <div class="flex-row mr-s">
                        <div class="label-color mr-s" style="white-space: nowrap">{{ $t('operation.DailyOps.guc5cg1') }}
                        </div>
                        <div class="mr-s">
                            <a-input-search :placeholder="$t('operation.DailyOps.guc5cg2')" v-model="filter.keyword"
                                search-button @search="handleSearchSetting" @keyup.enter="handleSearchSetting" allow-clear>
                                <template #button-default>
                                    {{ $t('operation.DailyOps.guc5cg3') }}
                                </template>
                            </a-input-search>
                        </div>
                    </div>
                    <div>
                        <a-button class="mr-s" type="outline" @click="handleRestoreAllDefaultValue"
                            :loading="data.loading">{{ $t('operation.DailyOps.guc5cg4') }}</a-button>
                        <a-button type="outline" class="mr-s" @click="handleRestoreAllOldValue" :loading="data.loading">{{
                            $t('operation.DailyOps.guc5cg5') }}</a-button>
                    </div>
                </div>
                <div class="flex-row">
                    <a-checkbox v-if="local.data.version === OpenGaussVersionEnum.ENTERPRISE && data.role === ClusterRoleEnum.SLAVE"
                        v-model="data.isApplyToAllNode" class="mr-s">{{ $t('operation.DailyOps.guc5cg6')
                        }}</a-checkbox>
                    <a-button type="primary" @click="handleSave" :loading="data.loading">{{ $t('operation.DailyOps.guc5cg7')
                    }}</a-button>
                </div>
            </div>

            <a-table class="d-a-table-row full-h" :data="list.data" :columns="columns" :pagination="list.page"
                :loading="list.loading" @page-change="currentPage" @page-size-change="pageSizeChange" :scroll="{ y: 650 }">
                <template #name="{ record }">
                    <div class="flex-row">
                        <div>{{ record.name }}</div>
                        <icon-edit v-if="record.hasChanged" style="color:rgb(var(--primary-6))" />
                    </div>
                </template>
                <template #value="{ record, rowIndex }">
                    <div class="flex-row">
                        <div v-if="record.context !== GUC_CONTEXT.INTERNAL">
                            <a-select v-if="record.varType === VAR_TYPE.ENUM" v-model="record.value" class="container"
                                @change="handleChangeValue($event, record)">
                                <a-option v-for="(item, index) in record.options" :key="index">{{ item }}</a-option>
                            </a-select>
                            <a-input-number v-if="record.varType === VAR_TYPE.INTEGER ||
                                record.varType === VAR_TYPE.INT64 ||
                                record.varType === VAR_TYPE.REAL" v-model="record.value" class="container"
                                @change="handleChangeValue($event, record)" :max="record.maxVal" :min="record.minVal" />
                            <a-switch v-if="record.varType === VAR_TYPE.BOOL" v-model="record.value" checked-value="on"
                                unchecked-value="off" @change="handleChangeValue($event, record)" />
                            <a-input v-if="record.varType === VAR_TYPE.STRING" v-model="record.value" class="container"
                                @change="handleChangeValue($event, record)" />
                        </div>
                        <div v-else>
                            <div>{{ record.value }}</div>
                        </div>
                        <div class="label-color">{{ record.unit }}</div>
                    </div>
                </template>
                <template #operation="{ record }">
                    <a-button type="text" class="label-color" @click="handleRestoreRowDefaultValue($event, record)">{{
                        $t('operation.DailyOps.guc5ch3') }}</a-button>
                    <a-button type="text" class="label-color" v-if="record.hasChanged" @click="handleRestoreRowOldValue($event, record)">{{
                        $t('operation.DailyOps.guc5ch4') }}</a-button>
                </template>
            </a-table>
        </a-spin>
    </a-drawer>
    <a-modal v-model:visible="data.postMasterDlgVisible" @ok="saveProc" @cancel="data.postMasterDlgVisible = false">
        <template #title>
            {{ t('operation.DailyOps.guc5ch7') }}
        </template>
        <div>{{ t('operation.DailyOps.guc5ch8') }}</div>
    </a-modal>
</template>
<script setup lang="ts">
import { ClusterRoleEnum, OpenGaussVersionEnum } from '@/types/ops/install';
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { reactive, ref, computed } from 'vue'
import { KeyValue } from '@/types/global'
import { listGucSetting, batchSetGucSetting } from '@/api/ops'
import { IconSearch } from '@arco-design/web-vue/es/icon'

const enum VAR_TYPE {
    BOOL = 'bool',
    INTEGER = 'integer',
    INT64 = 'int64',
    STRING = 'string',
    ENUM = 'enum',
    REAL = 'real'
}

const enum GUC_CONTEXT {
    POSTMASTER = 'postmaster',
    USERSET = 'user',
    INTERNAL = 'internal',
    BACKEND = 'backend',
    SUSET = 'superuser',
    SIGHUP = 'sighup'
}

const local = reactive<KeyValue>({
    data: KeyValue,
    index: 9999
})

const data = reactive<KeyValue>({
    clusterId: '',
    hostId: '',
    info: '',
    isApplyToAllNode: true,
    dataPath: '',
    hasPostmaster: '',
    postMasterDlgVisible: false,
    visible: false,
    saveLoading: false,
    queryLoading: false
})

const filter = reactive({
    keyword: '',
    pageSize: 20,
    pageNum: 0
})

const list = reactive<KeyValue>({
    data: [],
    oldData: [],
    page: {
        total: 0,
        defaultPageSize: 20,
        'show-total': true,
        'show-jumper': true,
        'show-page-size': true
    }
})

const columns = computed(() => [{
    title: t('operation.DailyOps.guc5cg8'),
    dataIndex: 'name',
    slotName: 'name',
    width: 300
}, {
    title: t('operation.DailyOps.guc5cg9'),
    dataIndex: 'value',
    slotName: 'value',
    width: 280
}, {
    title: t('operation.DailyOps.guc5ch0'),
    dataIndex: 'shortDesc',
    slotName: 'shortDesc',
    ellipsis: true,
    tooltip: true,
}, {
    title: t('operation.DailyOps.guc5ch1'),
    dataIndex: 'extraDesc',
    slotName: 'extraDesc',
    width: 120,
    ellipsis: true,
    tooltip: true,
}, {
    title: t('operation.DailyOps.guc5ch2'),
    slotName: 'operation'
}])


const currentPage = (e: number) => {
    filter.pageNum = e
    handleSearchSetting()
}

const pageSizeChange = (e: number) => {
    filter.pageSize = e
    handleSearchSetting()
}

const open = (clusterData: KeyValue, instance: KeyValue, clusterIndex: number) => {
    local.data = Object.assign({}, clusterData)
    local.index = clusterIndex
    data.clusterId = clusterData.clusterId
    data.hostId = instance.hostId
    data.dataPath = instance.dataPath
    data.info = instance.publicIp
    data.role = instance.clusterRole
    getGucSetting()
    data.visible = true
}

const handleClose = () => {
    data.clusterId = ''
    data.hostId = ''
    data.dataPath = ''
    data.info = ''
    data.role = ''
    list.data = []
    list.oldData = []
    filter.keyword = ''
    data.isApplyToAllNode = true
    data.visible = false
}

const handleSearchSetting = () => {
    list.data = reactive([])
    list.data.push(...list.oldData)
    list.page.total = list.data.length
    if (!filter.keyword) {
        return
    }
    const searchData = list.data.filter((item: KeyValue) => item.name.indexOf(filter.keyword) > -1)
    if (searchData.length > 0) {
        list.data = reactive(searchData)
        list.page.total = list.data.length
    } else {
        list.data = reactive([])
        list.page.total = 0
    }
}

const getGucSetting = () => {
    const params = {
        clusterId: data.clusterId,
        hostId: data.hostId
    }
    data.queryLoading = true
    listGucSetting(params).then((res: KeyValue) => {
        res.data.map((item: KeyValue) => {
            if (item.varType === VAR_TYPE.ENUM) {
                item.options = getEnumValList(item)
            }
            if (item.varType === VAR_TYPE.INT64 || item.varType === VAR_TYPE.INTEGER || item.varType === VAR_TYPE.REAL) {
                item.value = Number(item.value)
                item.defaultVal = Number(item.defaultVal)
                item.minVal = Number(item.minVal)
                item.maxVal = Number(item.maxVal)
            }
            // save old val
            item.oldValue = item.value
            item.hasChanged = false
        })
        list.page.total = res.data.length
        list.data.push(...res.data)
        list.oldData.push(...list.data)
    }).finally(() => {
        data.queryLoading = false
    })
}

const getEnumValList = (record: KeyValue) => {
    if (record.enumVals === '{""}') {
        return [record.defaultVal]
    }
    let str = ''
    if (record.enumVals.indexOf('\"') > -1) {
        str = record.enumVals.replaceAll('\"', '')
    } else {
        str = record.enumVals
    }
    return str.replace('{', '').replace('}', '').split(',')
}

const handleRestoreRowDefaultValue = (e: Event, record: KeyValue) => {
    record.value = record.defaultVal
    handleChangeValue(record.value, record)
}

const handleRestoreRowOldValue = (e: Event, record: KeyValue) => {
    record.value = record.oldValue
    handleChangeValue(record.value, record)
}

const handleRestoreAllDefaultValue = () => {
    list.data.map((item: KeyValue) => {
        item.value = item.defaultVal
        if (item.value !== item.oldValue) {
            item.hasChanged = true
        } else {
            item.hasChanged = false
        }
    })
}

const handleRestoreAllOldValue = (record: KeyValue) => {
    list.data.map((item: KeyValue) => {
        item.value = item.oldValue
        item.hasChanged = false
    })
}

const handleChangeValue = (value: any, record: KeyValue) => {
    if (value !== record.oldValue) {
        record.hasChanged = true
    } else {
        record.hasChanged = false
    }
}

const handleSave = () => {
    const changedList = list.data.filter((item: KeyValue) => item.hasChanged)
    if (changedList.length <= 0) {
        handleClose()
        return
    }
    const hasPostmaster = changedList.some((item: KeyValue) => item.context === 'postmaster')
    if (hasPostmaster) {
        data.hasPostmaster = true
    } else {
        data.hasPostmaster = false
    }
    if (!hasPostmaster) {
        saveProc()
    } else {
        data.postMasterDlgVisible = true
    }
}

const saveProc = () => {
    const reqData = {
        clusterId: data.clusterId,
        hostId: data.hostId,
        dataPath: data.dataPath,
        isApplyToAllNode: data.isApplyToAllNode,
        settings: list.data
    }
    data.saveLoading = true
    batchSetGucSetting(reqData).then((res: any) => {
        if (data.hasPostmaster) {
            setTimeout(() => {
                emits('finish', local.data, local.index)
            }, 2000)
        }
        handleClose()
    }).finally(() => {
        data.saveLoading = false
    })
}

const getLoadingTip = () => {
    if (data.queryLoading) {
        return ''
    }
    if (data.saveLoading) {
        return data.hasPostmaster ? t('operation.DailyOps.guc5ch5') : t('operation.DailyOps.guc5ch6')
    }
}

const emits = defineEmits([`finish`])

defineExpose({
    open
})
</script>
<style lang="less" scoped>
.container {
    width: 200px !important;
}

:deep(.arco-select-view-single) {
    width: 200px !important;
}

.row-changed {
    background-color: rgb(var(--gray-10));
}
</style>