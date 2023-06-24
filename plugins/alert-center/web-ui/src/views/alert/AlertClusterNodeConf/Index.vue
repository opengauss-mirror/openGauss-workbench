<template>
    <div v-if="showMain">
        <el-breadcrumb separator="/">
            <el-breadcrumb-item>{{ t('AlertClusterNodeConf.title') }}</el-breadcrumb-item>
        </el-breadcrumb>
        <el-divider />
        <div class="search-form">
            <div class="filter">
                <el-button type="primary" @click="addClusterNodeConf">{{ $t('AlertClusterNodeConf.confBtn') }}</el-button>
                <!-- <el-button type="primary" @click="setAlertConfig">{{ $t('AlertClusterNodeConf.setInterface') }}</el-button> -->
            </div>
            <div class="seperator"></div>
            <div class="search-form">
                <div class="filter">
                    <el-input v-model="formData.nodeName" style="width: 200px"
                        :placeholder="$t('AlertClusterNodeConf.nodeNamePlaceholder')">
                        <template #suffix>
                            <el-button :icon="Search" @click="search" />
                        </template>
                    </el-input>
                </div>
            </div>
        </div>
        <div>
            <el-table size="small" :data="tableDatas" ref="table" style="width: 100%" header-cell-class-name="grid-header" border>
                <el-table-column type="selection" width="55" />
                <el-table-column prop="nodeName" :label="$t('AlertClusterNodeConf.table[0]')" />
                <!-- <el-table-column prop="dbType" :label="$t('AlertClusterNodeConf.table[1]')" />
                <el-table-column prop="hostIp" :label="$t('AlertClusterNodeConf.table[2]')" /> -->
                <el-table-column prop="templateName" :label="$t('AlertClusterNodeConf.table[3]')" />
                <el-table-column :label="$t('AlertClusterNodeConf.table[4]')">
                    <template #default="scope">
                        <el-button link type="primary" size="small" @click="editClusterNodeConf(scope.row)">{{
                            $t('AlertClusterNodeConf.confBtn') }}</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <!-- <div class="pagination">
                <el-pagination v-model:currentPage="page.currentPage" v-model:pageSize="page.pageSize" :total="page.total"
                    :page-sizes="[10, 20, 30, 40]" layout="total,sizes,prev,pager,next" background small
                    @size-change="handleSizeChange" @current-change="handleCurrentChange" />
            </div> -->
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
import { i18n } from "@/i18n";
import { ElMessageBox, ElMessage } from "element-plus";
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
// const page = reactive({
//     currentPage: 1,
//     pageSize: 10,
//     total: 0,
// })
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
    // page.pageSize = 10
    // page.currentPage = 1
    requestData()
}
const cancelConfig = () => {
    showMain.value = true
}
const { data: res, run: requestData } = useRequest(
    () => {
        let params = {
        }
        return request.get("/alertCenter/api/v1/alertClusterNodeConf", params)
    },
    { manual: true }
)
watch(res, (res: any) => {
    if (res && res.code === 200) {
        // tableDatas.value = res.rows || []
        // page.total = res.total
        tableDatas.value = srcTableDatas.value = res.data || []
    } else {
        // tableDatas.value = []
        // page.total = 0
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

// const handleSizeChange = (val: any) => {
//     page.pageSize = val
//     requestData()
// }
// const handleCurrentChange = (val: any) => {
//     page.currentPage = val
//     requestData()
// }

onMounted(() => {
    requestData()
})
</script>
<style scoped lang='scss'>
</style>