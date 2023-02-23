<template>
    <div class="filter">
        <span>{{ props.title }}&nbsp;</span>
        <el-cascader v-model="cluster" :options="clusterList" @change="getClusterValue" style="max-width: 200px" :style="{ width: width ? width + 'px' : 'auto' }" :clearable="!notClearable" />
    </div>
</template>

<script lang="ts" setup>
import ogRequest from '../request';
import { useRequest } from 'vue-request';

type Rer =
    | [
          {
              [propName: string]: string | number;
          }
      ]
    | undefined;
const props = withDefaults(
    defineProps<{
        title?: string;
        width?: string;
        instanceValueKey?: string;
        notClearable?: boolean;
        clusterOnly?: boolean;
        autoSelectFirst?: boolean; // now only support one level
    }>(),
    {
        title: '',
        width: '',
        instanceValueKey: 'nodeId',
        notClearable: false,
        clusterOnly: false,
        autoSelectFirst: false,
    }
);
const emit = defineEmits(['getCluster', 'loaded']);

const cluster = ref<Array<any>>([]);
const clusterList = ref<Array<any>>([]);

const treeTransform = (arr: any) => {
    let obj: any = [];
    if (arr instanceof Array) {
        arr.forEach((item) => {
            obj.push({
                label: item.clusterId ? item.clusterId : item.azName + '_' + item.publicIp,
                value: item.clusterId ? item.clusterId : item[props.instanceValueKey],
                children: props.clusterOnly ? null : treeTransform(item.clusterNodes),
            });
        });
        // now only support one level
        if (props.autoSelectFirst && obj.length > 0) {
            if (props.clusterOnly) cluster.value = [obj[0].value];
            else if (obj[0].children.length > 0) cluster.value = [obj[0].value, obj[0].children[0].value];
            emit('getCluster', cluster.value);
        }
    }
    return obj;
};
const getClusterValue = (val: string[]) => {
    console.log('getClusterValue');
    if (val == null) emit('getCluster', []);
    else emit('getCluster', val);
};

onMounted(() => {
    clusterData();
});
const { data: rer, run: clusterData } = useRequest(
    () => {
        return ogRequest.get('/logSearch/api/v1/clusters', '');
    },
    { manual: true }
);

watch(rer, (rer: Rer) => {
    if (rer && Object.keys(rer).length) {
        clusterList.value = treeTransform(rer);
        emit('loaded');
    }
});
</script>

<style lang="scss" scoped>
.filter {
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
}
</style>
