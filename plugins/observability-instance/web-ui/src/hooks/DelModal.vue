<script setup lang="ts">
const props = withDefaults(
    defineProps<{
        content: string
        id: string
        resolve:() => void
    }>(),
    {}
);
const visible = ref(true)
const close = () => document.getElementById(props.id)?.remove()
const confirm = () => {
    props.resolve()
    visible.value = false
}
</script>

<template>
<el-dialog
    :width="400"
    v-model="visible"
    :close-on-click-modal="false"
    custom-class="del-dialog"
    center
    draggable
    @close="close"
>
    <template #header></template>
    <div class="del-text">确认删除</div>
    <div class="content-text">{{props.content}}</div>
    <template #footer>
        <el-button style="padding: 5px 20px;" type="primary" @click="confirm">确定</el-button>
        <el-button style="padding: 5px 20px;" @click="() => visible = false">取消</el-button>
    </template>
</el-dialog>
</template>

<style>
.del-dialog .el-dialog__header {
    display: none;
}
</style>
<style scoped>
.del-text {
    text-align: center;
    font-weight: bold;
    font-size: 16px;
    margin-bottom: 20px;
}
.content-text {
    text-align: center;
}
</style>
