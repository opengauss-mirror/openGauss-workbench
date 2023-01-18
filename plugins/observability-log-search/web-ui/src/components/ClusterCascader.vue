<template>
    <div class="filter">
        <span>{{props.title}}&nbsp;</span>
        <el-cascader v-model="cluster" :options="clusterList" @change="getClusterValue"/>
    </div>
</template>

<script lang="ts" setup>
import ogRequest from '../request';
import { useRequest } from "vue-request";

type Rer = [{
    [propName:string]:string | number;
}] | undefined;
const props = withDefaults(defineProps<{
    title?:string,
}>(), {
    title: 'Cluster/Instance'
})
const emit = defineEmits(['getCluster'])

const cluster = ref([]);
const clusterList = ref<Array<any>>([]);

const treeTransform = (arr:any) => {
    let obj:any = [];
    if (arr instanceof Array) {
        arr.forEach(item => {
            obj.push({
                label: item.clusterId ? item.clusterId : item.nodeId,
                value: item.clusterId ? item.clusterId : item.nodeId,
                children: treeTransform(item.clusterNodes)
            })
        })
    }
    return obj;
}  
const getClusterValue = (val:string[]) => {
    emit('getCluster', val)
}

onMounted(() => {
    clusterData();
})
const { data: rer, run: clusterData } = useRequest(() => {
    return ogRequest.get('/observability/v1/public/listCluster', '')
}, { manual: true })

watch(rer, (rer:Rer) => {
    if (rer && Object.keys(rer).length) {
        clusterList.value = treeTransform(rer);
    } 
})
</script>

<style lang="scss" scoped>
.filter{
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
}
</style>
