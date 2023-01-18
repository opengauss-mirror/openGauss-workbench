<template>
    <div class="page-container">
        <my-operator>
            <template #right>
                <el-input 
                    v-model="searchText"
                    style="width:200px;"
                    :prefix-icon="Search"
                    :placeholder="$t('datasource.configPlaceholder')"
                />
                <el-button type="primary" @click="handleModal">{{$t('app.query')}}</el-button>
                <el-button type="primary" :icon="Refresh" :title="$t('app.refresh')"/>
            </template>
        </my-operator>
        <div class="table-wrap">
            <el-table style="width:100%" align="center" :data="tableData.data">
                <template v-for="columns in tableData.columns" >
                    <el-table-column :label="columns.title" :prop="columns.key" width="250"/>
                </template>
                <el-table-column :label="$t('app.operate')">
                    <template #default="scope">
                        <span class="editBtn" @click="clickHandleEdit(scope.row)">{{$t('app.edit')}}</span>
                    </template>
                </el-table-column>
            </el-table>
        </div>
        <ConfigEdit :show="addModel" :configItem="configItem" :clusterId="clusterId" :nodeId="nodeId"  @changeModal="changeModalCurrent" @changeValue="getChangeValue"/>
    </div>
</template>

<script lang="ts" setup>
import { Search, Refresh } from '@element-plus/icons-vue';
import ConfigEdit from './ConfigEdit.vue'
import ogRequest from '../../request';
import { useRequest } from "vue-request";

const searchText = ref('');
const addModel = ref(false);
const clusterId=ref('');
const nodeId=ref('');
const configItem=ref('');
const tableData=reactive<{
    data:any[],
    columns:any[]
}>({
    data:[],
    columns:[] 
})
const handleModal = () => {
    addModel.value = true;
}
const changeModalCurrent = (val:boolean) => {
    addModel.value = val;
}
const getChangeValue = (val:any)=>{
    console.log(Object.entries(val))
    let query:any[]=[];
    for(let [k,v] of Object.entries(val)){
        if(k !== 'nodeId' && k !== 'clusterId')
        query.push({
            key:k,
            value:v
        })
    }
    nextTick(()=>{
        requestEdit(query)
    })
}
const clickHandleEdit = (val:any) =>{
    nodeId.value=val.nodeId;
    clusterId.value=val.clusterId;
    configItem.value=val;
    addModel.value = true;
   
}
const requestEdit = (data:any) =>{
    useRequest(()=>{
        return ogRequest.put(`/sql-diagnosis/api/v1/public/node-settings/${nodeId.value}`, data) 
    },{
        onSuccess:(data)=>{
            if(JSON.stringify(data)){
                addModel.value = false;
                requestData();
            }
        }
    })
}
onMounted(()=>{
    requestData();
})

const { data: res, run: requestData } = useRequest(() => {
    return ogRequest.get("/sql-diagnosis/api/v1/public/node-settings", '') 
}, { manual: true })

watch(res, (res: any) => {
    if (res && Object.keys(res).length) {
        tableData.data=res.data;
        tableData.columns=res.columns;
    }
})


</script>

<style lang="scss" scoped>
.editBtn{
    color:#d4d4d4;
    cursor: pointer;
}
:deep(.el-table .el-table__cell.is-center){
    background-color: #424242;
}
</style>
