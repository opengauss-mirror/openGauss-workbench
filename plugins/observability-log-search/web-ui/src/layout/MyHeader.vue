<script setup lang="ts">
import { Fold, Expand } from '@element-plus/icons-vue';
import Lang from '../i18n/Lang.vue';

const props = withDefaults(defineProps<{
    showIcon: boolean
}>(), {
    showIcon: true
})

const themeText = ref("dark")
const collapse = ref(false)
const myEmit = defineEmits<{
    (event: 'collapse', collapse: boolean): void,
    (event: 'changeTheme'): void
}>()
const emitCollapse = (c: boolean) => {
    myEmit('collapse', c)
    collapse.value = c
}
const onChangeTheme = () => {
    myEmit('changeTheme')
    themeText.value = themeText.value === 'dark' ? 'light' : 'dark';
}
</script>

<template>
    <div class="header-container">
        <div class="left">
            <el-icon :size="18" v-if="collapse && props.showIcon" style="cursor: pointer;" @click="() => emitCollapse(false)"><Expand /></el-icon>
            <el-icon :size="18" v-if="!collapse && props.showIcon" style="cursor: pointer;" @click="() => emitCollapse(true)"><Fold /></el-icon>
            <my-breadcrumb class="bread" />
        </div>
        <div class="right">
            <span v-if="false" class="header-change-theme" @click.stop="onChangeTheme()">{{ themeText }}</span>
            <Lang />
        </div>
    </div>
</template>

<style scoped lang="scss">
.header-container {
    height: var(--vem-header-height);
    background-color: $og-sub-background-color;
    font-size: 16px;
    padding: 0 16px;
    letter-spacing: 1px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: nowrap;
    flex-shrink: 0;
}
.left {
    display: flex;
    align-items: center;
}
.bread {
    margin-left: 10px;
}
</style>
