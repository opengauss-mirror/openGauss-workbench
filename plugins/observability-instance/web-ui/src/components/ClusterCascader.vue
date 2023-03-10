<template>
    <div class="filter">
        <span>{{ props.title }}&nbsp;</span>
        <el-cascader v-model="cluster" :options="clusterList" @change="getClusterValue" style="max-width: 200px" :style="{ width: width ? width + 'px' : 'auto' }" :clearable="!notClearable" />
    </div>
</template>

<script lang="ts" setup>
import ogRequest from "../request";
import { useRequest } from "vue-request";

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
        title: "",
        width: "",
        instanceValueKey: "nodeId",
        notClearable: false,
        clusterOnly: false,
        autoSelectFirst: false,
    }
);
const emit = defineEmits(["getCluster", "loaded"]);

const cluster = ref<Array<any>>([]);
const clusterList = ref<Array<any>>([]);

const treeTransform = (arr: any) => {
    let obj: any = [];
    if (arr instanceof Array) {
        arr.forEach((item) => {
            obj.push({
                label: item.clusterId ? item.clusterId : item.azName + "_" + item.publicIp,
                value: item.clusterId ? item.clusterId : item[props.instanceValueKey],
                children: props.clusterOnly ? null : treeTransform(item.clusterNodes),
                nodeId: item.nodeId ? item.nodeId : null,
            });
        });
        if (props.autoSelectFirst && obj.length > 0) {
            if (props.clusterOnly) cluster.value = [obj[0].value];
            else if (obj[0].children.length > 0) cluster.value = [obj[0].value, obj[0].children[0].value];
            emit("getCluster", cluster.value);
        }
    }
    return obj;
};
const getClusterValue = (val: string[]) => {
    if (val == null) emit("getCluster", []);
    else emit("getCluster", val);
};
const setNodeId = (val: string) => {
    if (val === "" || val === null || val === undefined) return;
    nextTick(() => {
        if (clusterList.value.length <= 0) return;
        for (let p1 = 0; p1 < clusterList.value.length; p1++) {
            const clusterTemp = clusterList.value[p1];
            for (let p2 = 0; p2 < clusterTemp.children.length; p2++) {
                const node = clusterTemp.children[p2];
                if (node.nodeId === val) {
                    cluster.value = [clusterTemp.value, node.value];
                    emit("getCluster", cluster.value);
                    return;
                }
            }
        }
    });
};
defineExpose({ setNodeId });

onMounted(() => {
    clusterData();
});
const { data: rer, run: clusterData } = useRequest(
    () => {
        return ogRequest.get("/observability/v1/topsql/cluster", "");
    },
    { manual: true }
);

watch(rer, (rer: Rer) => {
    if (rer && Object.keys(rer).length) {
        clusterList.value = treeTransform(rer);
        emit("loaded", rer);
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
