<template>
    <el-table ref="multipleTableRef" v-bind="$attrs" class="z-table" :data="_data" :max-height="maxHeight"
        :style="{ 'max-height': `${maxHeight}px` }" row-key="__vem_id" border @sort-change="handleSortChange">
        <slot />
    </el-table>
    <el-pagination v-model:currentPage="state.page" v-model:pageSize="state.size" :total="state.total" class="pagination"
        layout="total,prev,pager,next" hide-on-single-page background small />
</template>

<script setup lang="ts">
import { useDebounceFn, useEventListener } from "@vueuse/core";
import { ElTable } from "element-plus";
import { useRequest } from "vue-request";
import ogRequest from "@/request";

const props = withDefaults(
    defineProps<{
        api: string;
        post?: boolean;
        maxHeight?: string | number;
        extra?: Record<string, any>;
        flag?: boolean;
        formatter?: (d: any) => void;
        frontPagination?: boolean
    }>(),
    {}
);

const state = reactive({
    data: [] as any[],
    total: 0,
    page: 1,
    size: 10,
});
const maxHeight = ref(props.maxHeight || 500);

const myEmit = defineEmits<{ (event: 'load-data', data: unknown[]): void }>()
const multipleTableRef = ref<InstanceType<typeof ElTable>>()
const toggleRowSelection = (row: any, selected: boolean, prop: string) => {
    nextTick(() => {
        multipleTableRef.value!.toggleRowSelection(state.data.find(d => d[prop] === row[prop]), selected)
    })
}
defineExpose({ toggleRowSelection })

interface TableData {
    list: any[],
    total: number,
}
const { run, data } = useRequest(() => {
    const payload = {
        page: state.page,
        pageSize: state.size,
        ...(props.extra || {}),
        sortKey: sortKey.value,
        sortOrder: sortOrder.value,
    }
    if (props.post) {
        return ogRequest.post<TableData>(props.api, payload)
    } else {
        return ogRequest.get<TableData>(props.api, payload)
    }
}, { manual: true, debounceInterval: 200 });
watch(data, (d: any) => {
    state.data = d?.list.map((item: any) => {
        if (props.formatter) {
            props.formatter(item)
        }
        const r = { ...item }
        if (r.children && Array.isArray(r.children)) {
            r["__vem_id"] = Math.random().toString(36).slice(2);
            r.children.forEach(
                (c: any) =>
                    (c["__vem_id"] = Math.random().toString(36).slice(2))
            );
        }
        return r;
    })!;
    state.total = d?.total!;
    myEmit('load-data', d?.list || [])
})
const top = ref(0);
useEventListener(
    window,
    "resize",
    useDebounceFn(
        () => (maxHeight.value = document.body.clientHeight - top.value - 60),
        500
    )
);
onMounted(() => {
    if (!props.maxHeight) {
        const topRect = document
            .querySelector(".z-table")
            ?.getBoundingClientRect() as DOMRect;
        top.value = topRect.top;
        maxHeight.value = document.body.clientHeight - topRect.top - 60;
        state.size = Math.max(Number.parseInt(`${maxHeight.value / 32}`) - 1, 10)
    }
    run();
});
if (!props.frontPagination) {
    watch(() => state.page, run);
    if (props.extra) {
        watch(() => props.extra, run, { deep: true });
    }
    watch(() => props.flag, run);
}
const _data = computed(() => {
    if (!props.frontPagination) {
        return state.data
    }
    return state.data.filter((d, i) => i >= (state.page - 1) * state.size && i < state.page * state.size)
})

// sort
const sortKey = ref('')
const sortOrder = ref('')
const handleSortChange = (cell: { prop: string | null, order: 'ascending' | 'descending' | null }) => {
    sortKey.value = cell.prop || ''
    sortOrder.value = cell.order || ''
    run()
}
</script>

<style scoped>
.pagination {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
}
</style>
