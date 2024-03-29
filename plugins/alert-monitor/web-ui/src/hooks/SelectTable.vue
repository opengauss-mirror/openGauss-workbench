<template>
    <el-dialog :width="$props.width" :title="$props.title" v-model="visible" custom-class="nodes-dialog"
        :close-on-click-modal="false" draggable @close="close">
        <div class="flex-end" style="margin-bottom: 10px;">
            <slot></slot>
        </div>
        <my-table ref="multipleTableRef" :api="$props.api" :max-height="360" :extra="$props.filter"
            @selection-change="selectionChange" @load-data="loadData" :post="$props.post">
            <el-table-column v-if="$props.type === 'radio'" width="50" align="center">
                <template #default="{ row }">
                    <el-radio v-model="radio" :label="row[$props.uid]"></el-radio>
                </template>
            </el-table-column>
            <el-table-column v-if="$props.type === 'selection'" width="50" align="center"
                type="selection"></el-table-column>
            <slot name="column"></slot>
        </my-table>
        <template #footer>
            <el-button style="padding: 5px 20px;" @click="() => visible = false">{{ t('app.cancel') }}</el-button>
            <el-button style="padding: 5px 20px;" type="primary" @click="confirm">{{ t('app.confirm') }}</el-button>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
const { t } = useI18n();
const props = withDefaults(
    defineProps<{
        id: string
        resolve: (value: unknown[]) => void
        api: string
        post?: boolean
        width?: number
        title?: string
        filter?: any
        selections?: unknown[]
        uid: string
        type: 'radio' | 'selection'
    }>(),
    {
        width: 700,
        title: '',
        post: false,
        type: 'radio',
        selections: () => ([])
    }
);

const multipleTableRef = ref()
let cacheData: string[] = []
let cacheList: unknown[] = []
const loadData = (data: unknown[]) => {
    cacheList = data
    cacheData = data.map((d: any) => d[props.uid])
    selections.value.forEach((s: any) => {
        if (cacheData.includes(s[props.uid])) {
            multipleTableRef.value!.toggleRowSelection(s, true, props.uid)
        }
    })
}
const selections = ref<unknown[]>(props.selections)
const selectionChange = (rows: unknown[]) => {
    const dbids = selections.value.map((s: any) => s[props.uid])
    const selectedDbids: string[] = []
    rows.forEach((r: any) => {
        selectedDbids.push(r[props.uid])
        if (!dbids.includes(r[props.uid])) {
            selections.value.push(r)
        }
    })
    const removeDbids = cacheData.filter(key => !selectedDbids.includes(key))
    selections.value = selections.value.filter((s: any) => !removeDbids.includes(s[props.uid]))
}

const radio = ref('')

const visible = ref(true)
const close = () => document.getElementById(props.id)?.remove()
const confirm = () => {
    if (props.type === 'selection') {
        props.resolve(selections.value)
    } else {
        props.resolve([cacheList.find((l: any) => l[props.uid] === radio.value)])
    }
    visible.value = false
}
</script>

<style>
.nodes-dialog .el-dialog__footer {
    border-top: 1px solid #eee;
    padding-bottom: 10px;
}

.nodes-dialog .el-dialog__header {
    border-bottom: 1px solid #eee;
    margin-right: 0px;
    padding-top: 10px;
}

.nodes-dialog .el-dialog__header .el-dialog__title {
    font-size: 14px;
}

.nodes-dialog .el-dialog__headerbtn {
    top: -2px;
}

.nodes-dialog .el-radio .el-radio__label {
    display: none;
}
</style>
