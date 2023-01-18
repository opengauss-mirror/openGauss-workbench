<script lang="ts" setup>

const props = withDefaults(
    defineProps<{
        title: string;
    }>(),
    {
        title: ''
    }
);

const curCollapse = ref(['single'])

</script>

<template>
    <div class="single-collapse-container">
        <el-collapse :model-value="curCollapse">
            <el-collapse-item :title="props.title" name="single">
                <slot />
            </el-collapse-item>
        </el-collapse>
    </div>
</template>

<style scoped>

.single-collapse-container:deep(.el-collapse) {
    border-top: none;
}

.single-collapse-container:deep(.el-collapse-item__content) {
    background-color: #212121;
}

.single-collapse-container:deep(.el-collapse-item__header) {
    display: flex;
    flex-direction: row-reverse;
    justify-content: flex-end;
    background-color: #424242;
    border: none;
    border-radius: 8px 8px 0px 0px;
}

.single-collapse-container:deep(.el-collapse-item__arrow) {
    margin: none;
}

.single-collapse-container:deep(.el-collapse-item__wrap) {
    background-color: #424242;
}

.single-collapse-container:deep(.el-icon) {
    margin: 0 10px;
}

</style>
