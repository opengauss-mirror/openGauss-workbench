<script setup lang="ts">
import type { TabPanelName } from 'element-plus';
import ogRequest from '../../request';
import { useRequest } from "vue-request";
import { osLogType, dbLogType, optionType } from './common';
import { cloneDeep } from 'lodash-es';
import { Refresh } from '@element-plus/icons-vue';
    
    type Res = {
        tableData: string[],
        page: {
            count: number,
            current: number
        }
    } | undefined;
    
const logTypeOption = ref<Array<optionType>>(osLogType);
const system = ref('OS');
const tableData = ref<Array<any>>([]);
const cluster = ref<Array<any>>([]);
const initFormData = { 
    searchPhrase: '',
    type: 'all',
    dateValue: [],
}
const formData = reactive(cloneDeep(initFormData));
const page = reactive({
    pageSize: 10,
    currentPage: 1,
    total: 0
});
const queryData = computed(() => {
    const { dateValue, searchPhrase, type } = formData;
    const { pageSize: rowCount, currentPage: current } = page;
    const queryObj = {
        startDate: dateValue.length ? dateValue[0] : '',
        endDate: dateValue.length ? dateValue[1] : '',
        system: system.value,
        type,
        rowCount,
        searchPhrase,
        current,
        clusterId: cluster.value.length ? cluster.value[0] : '',
        nodeId: cluster.value.length ? cluster.value[1] : '',
    }
    return queryObj;
});
    
onMounted(() => {
    requestData();
});
    
const { data: res, run: requestData } = useRequest(() => {
    return ogRequest.post("/sql-diagnosis/api/v1/monitoring/logRetrieval", queryData.value) 
}, { manual: true })
    
const changetTabs = (name: TabPanelName) => {
    logTypeOption.value = name === 'OS' ? osLogType : dbLogType;
    formData.type = initFormData.type;
    requestData();
}
const handleQuery = () => { 
    page.currentPage = 1;
    requestData();
}
const handleReset = () => {
    page.currentPage = 1;
    Object.assign(formData, cloneDeep(initFormData));
    requestData();
}
const handleSizeChange = (val: number) => {
    page.currentPage = 1;
    page.pageSize = val;
    requestData();
}
const handleCurrentChange = (val: number) => {
    page.currentPage = val;
    requestData();
}
const showHighLightWord = (val: string) => {
    const reg = new RegExp(formData.searchPhrase, 'ig');
    return val.replace(reg, (substring) => {
        return `<font color="orange">${substring}</font>`;
    });
}
const handleClusterValue = (val:any) => {
    cluster.value = val;
}
    
watch(res, (res: Res) => {
    if (res && Object.keys(res).length) {
        const { count: total, current: currentPage } = res.page
        tableData.value = res.tableData;
        Object.assign(page, { pageSize: page.pageSize, total, currentPage });
    } else {
        tableData.value = [];
        Object.assign(page, { pageSize: page.pageSize, total: 0, currentPage: 1 });
    } 
})
</script>
    
    <template>
        <div class="page-container">
            <div class="tab-wrapper">
                    <el-form class="tab-wrapper-filter" :inline="true" :model="formData">
                        <ClusterCascader @getCluster="handleClusterValue"/>
                        <el-form-item class="filter-keyword" label="关键字">
                            <el-input v-model="formData.searchPhrase" placeholder="请输入关键字" />
                        </el-form-item>
                        <el-form-item :label="$t('datasource.logType')">
                            <el-select v-model="formData.type" :placeholder="$t('datasource.selectLogType')">
                                <el-option v-for="item in logTypeOption" :key="item.value" :label="item.label" :value="item.value" />
                            </el-select>
                        </el-form-item>
                        <el-form-item :label="$t('datasource.executeTime')">
                            <MyDatePicker
                                v-model="formData.dateValue"
                                type='datetimerange'
                                :valueFormatToISO="true"
                                class="form-data-picker"
                            />
                        </el-form-item>
                        <el-button type="primary" @click="handleQuery">{{$t('app.query')}}</el-button>
                        <el-button type="primary" @click="handleReset" :icon="Refresh" :title="$t('app.refresh')"/>
                    </el-form>
            </div>
            <div>
                <el-tabs v-model="system" @tab-change="changetTabs">
                    <el-tab-pane :label="$t('datasource.os')" name="OS" />
                    <el-tab-pane :label="$t('datasource.database')" name="DB" />
                </el-tabs>
            </div>
            <div class="table-wrapper">
                <el-table :data="tableData" style="width: 100%;" :default-sort="{ prop: 'date', order: 'descending' }">
                    <el-table-column prop="date" sortable :label="$t('datasource.executeTime')" width="250" />
                    <el-table-column :label="$t('datasource.logContent')">
                        <template #default="scope">
                        <span v-html="showHighLightWord(scope.row.data)"></span>
                        </template>
                    </el-table-column>
                </el-table>
                <el-pagination
                    v-if="tableData.length"
                    v-model:currentPage="page.currentPage"
                    v-model:pageSize="page.pageSize"
                    :total="page.total"
                    :page-sizes="[10, 20, 30, 40]"
                    class="pagination"
                    layout="total,sizes,prev,pager,next"
                    background
                    small
                    @current-change="handleCurrentChange"
                    @size-change="handleSizeChange"
                />
            </div>
        </div>
    </template>
    
    <style lang="scss" scoped>
    .page-container {
        height: 100%;
    }
    .tabs-wrapper{
        .tab-wrapper-filters{
            display: flex;
            align-items: center;
            margin:20px 0 30px;
        }
    }
    .tab-wrapper {
        background-color: #424242;
        margin-bottom: 20px;
        width: 100%;
        overflow: auto;
        &-filter {
            padding-left: 16px;
            display: flex;
            flex-wrap: nowrap;
            align-items: center;
            height: 50px;
            background-color: #424242;
        }
    }
    .table-wrapper {
        height: calc(100% - 50px);
        display: flex;
        flex-direction: column;
        justify-content: space-between;
    }
    :deep(.el-form-item) {
        margin-bottom: 0;
        margin-right: 10px;
    }
    :deep(.el-form-item__label) {
        line-height: 2.7;
    }
    :deep(.el-select) {
        width: 150px;
    }
    :deep(.el-tabs__header) {
        padding: 0 16px;
        width: calc(100% - 1080px);
        margin: 0;
    }
    :deep(.el-table__body-wrapper) {
      height: 100%;
      overflow: auto;
    }
    :deep(.el-pagination) {
        display: flex;
        justify-content: flex-end;
    }
    :deep(.form-data-picker){
        width:220px;
    }
    </style>
