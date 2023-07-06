<template>
    <div v-if="showMain">
        <div class="page-header">
            <div class="icon"></div>
            <div class="title">{{ t('AlertClusterNodeConf.title') }}</div>
            <div class="seperator"></div>
            <div class="alert-title">{{ t('AlertClusterNodeConf.title') }} </div>
            <div class="alert-seperator">&nbsp;/</div>
        </div>
        <div class="search-form">
            <div class="filter">
                <el-button type="primary" @click="addClusterNodeConf">{{ t('AlertClusterNodeConf.confBtn') }}</el-button>
            </div>
            <div class="seperator"></div>
            <div class="filter">
                <el-input v-model="formData.nodeName" style="width: 200px"
                    :placeholder="t('AlertClusterNodeConf.nodeNamePlaceholder')">
                    <template #suffix>
                        <el-button :icon="Search" @click="search" />
                    </template>
                </el-input>
            </div>
        </div>
        <div>
            <el-table size="small" :data="tableDatas" ref="table" style="width: 100%" header-cell-class-name="grid-header"
                border>
                <el-table-column type="selection" width="40" />
                <el-table-column prop="nodeName" :label="t('AlertClusterNodeConf.table[0]')" />
                <el-table-column prop="templateName" :label="t('AlertClusterNodeConf.table[3]')" />
                <el-table-column :label="t('AlertClusterNodeConf.table[4]')">
                    <template #default="scope">
                        <el-button link type="primary" size="small" @click="editClusterNodeConf(scope.row)">{{
                            t('AlertClusterNodeConf.confBtn') }}</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>
    </div>
    <div v-else>
        <Detail :clusterNodeList="selectedDatas" @updateConfigSuccess="updateConfigSuccess" @cancelConfig="cancelConfig" />
    </div>
</template>

<script setup lang='ts'>
import { Search } from "@element-plus/icons-vue";
import "element-plus/es/components/message-box/style/index";
import { useRequest } from "vue-request";
import request from "@/request";
import { ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";
import { ref, onMounted } from 'vue'
import Detail from "@/views/alert/AlertClusterNodeConf/Detail.vue"
import { cloneDeep } from "lodash";
const { t } = useI18n();

const showMain = ref<boolean>(true)
const formData = ref<any>({
    nodeName: ''
})
const table = ref();
const srcTableDatas = ref<any[]>([])
const tableDatas = ref<any[]>([])
const selectedDatas = ref<any[]>([])

const addClusterNodeConf = () => {
    const rows = table.value.getSelectionRows()
    if (rows.length === 0) {
        ElMessage({
            message: t('alertRecord.tableDataSelectTip'),
            type: 'warning',
        })
    } else {
        selectedDatas.value = rows;
        showMain.value = false
    }
}
const editClusterNodeConf = (row: any) => {
    selectedDatas.value = []
    selectedDatas.value.push(row)
    showMain.value = false
}

const updateConfigSuccess = () => {
    showMain.value = true
    requestData()
}
const cancelConfig = () => {
    showMain.value = true
}
const { data: res, run: requestData } = useRequest(
    () => {
        let params = {
        }
        return request.get("/api/v1/alertClusterNodeConf", params)
    },
    { manual: true }
)
watch(res, (res: any) => {
    if (res && res.code === 200) {
        tableDatas.value = srcTableDatas.value = res.data || []
    } else {
        srcTableDatas.value = []
        tableDatas.value = []
    }
})

const search = () => {
    if (!formData.value.nodeName) {
        tableDatas.value = cloneDeep(srcTableDatas.value)
    } else {
        tableDatas.value = srcTableDatas.value.filter(item => item.nodeName.indexOf(formData.value.nodeName) !== -1)
    }
}

onMounted(() => {
    requestData()
})
</script>
<style scoped lang='scss'></style>