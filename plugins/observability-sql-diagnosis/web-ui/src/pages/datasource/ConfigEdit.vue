<template>
    <div class="config-dialog">
    
    <el-dialog
        width="800px"
        :title="$t('datasource.editConfigTitle')"
        v-model="visible"
        :close-on-click-modal="false"
        draggable
        @close="taskClose"
    >
    <div class="dialog-content">
        <el-form :model="formData">
            <el-form-item :label="$t('datasource.trackTable[7]')" prop="cluster">
                <span class="config-content">{{props.clusterId}}</span>
            </el-form-item>
            <el-form-item :label="$t('datasource.trackTable[8]')" prop="cluster">
                <span class="config-content">{{props.nodeId}}</span>
            </el-form-item>
            <template v-for="item in configData">
                <el-form-item :label="item[0]" v-if="item[0] !== 'clusterId' && item[0] !== 'nodeId'">
                    <el-input class="form-input" v-model="item[1]" type="text" @change="(value:any)=>getValueInput(value, item[0])"/>
                </el-form-item>
            </template>
            
        </el-form>
     </div>
   
    <template #footer>
        <el-button style="padding: 5px 20px;" type="primary" @click="handleCancelModel">{{$t('app.cancel')}}</el-button>
        <el-button style="padding: 5px 20px;" type="primary" @click="handleconfirmModel">{{$t('app.confirm')}}</el-button>
    </template>
    </el-dialog>

</div>
</template>

<script lang="ts" setup>

const emit = defineEmits(['changeModal','changeValue'])
const props = defineProps<{
    show:boolean,
    configItem:any
    clusterId:string,
    nodeId:string,
}>()
const visible = ref(false);
const configData=ref<Array<any>[]>([]);
let formData = reactive({});
watch(() => props.show, (newValue) => {
    visible.value = newValue;
})
watch(() => props.configItem, (newValue) => {
    configData.value=Object.entries(newValue);
    formData=newValue;  
})
const taskClose = () => {
    visible.value = false;
    emit('changeModal', visible.value)
}
const getValueInput = (value:any,key:any) =>{
   for(let k of Object.keys(formData)){
        if(k === key){
            Object.defineProperty(formData, k,{
                value:value,
                writable:true,
                enumerable:true,
                configurable:true,
            });   
        }
    }
}
const handleconfirmModel = () => {
    visible.value = false;
    emit('changeValue',formData);
    // Object.assign(formData,cloneDeep(initFormData));
}
const handleCancelModel = () => {
    visible.value = false;
    emit('changeModal', visible.value)
}
</script>
<style lang="scss" scoped>
.config-dialog{
    &:deep(.el-dialog .el-dialog__header){
        text-align: center;
    }
    &:deep(.el-dialog .el-dialog__title){
        color:#fff;
    }
    &:deep(.el-form-item--small .el-form-item__label){
        width:110px;
    }
    &:deep(.el-dialog .el-dialog__footer){
        border:none !important;
    }
    &:deep(.el-input--small .el-input__wrapper){
        background-color: #000;
    }
    .dialog-content{
        padding-bottom:180px;
    }
}
</style>
