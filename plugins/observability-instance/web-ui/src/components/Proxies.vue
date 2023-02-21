<template>
    <div>
        <el-select v-model="machineValue" @change="selectMachine" :clearable="!notClearable" :style="{ width: width ? width + 'px' : 'auto' }">
            <el-option v-for="item in machineList" :key="item.hostId" :label="item.privateIp + '(' + item.publicIp + ')'" :value="item.hostId" />
        </el-select>
    </div>
</template>

<script lang="ts" setup>
import ogRequest from "../request";
import { useRequest } from "vue-request";

const props = withDefaults(
    defineProps<{
        width?: string;
        notClearable?: boolean;
        autoSelectFirst?: boolean;
    }>(),
    {
        width: "",
        notClearable: false,
        autoSelectFirst: false,
    }
);
const emit = defineEmits(["change", "loaded"]);

const machineValue = ref<any>();
const machineList = ref<Array<any>>([]);

const selectMachine = (val: string[]) => {
    emit("change", machineValue);
};

onMounted(() => {
    clusterData();
});
const { data: rer, run: clusterData } = useRequest(
    () => {
        return ogRequest.get("/observability/v1/environment/prometheus", "");
    },
    { manual: true }
);

watch(rer, (rer) => {
    if (rer && rer.data && Object.keys(rer.data).length) {
        machineList.value = rer.data.rows;
        emit("loaded");
        if (props.autoSelectFirst && machineList.value.length > 0) {
            machineValue.value = machineList.value[0].hostId;
            emit("change", machineValue.value);
        }
    }
});
</script>
