<template>
    <el-breadcrumb separator="/">
        <el-breadcrumb-item v-if="activeName === 'notifyWay'">{{ $t('notifyWay.title') }}</el-breadcrumb-item>
        <el-breadcrumb-item v-if="activeName === 'notifyConfig'">{{ $t('notifyConfig.title') }}</el-breadcrumb-item>
        <el-breadcrumb-item v-if="state === 'addNotifyWay'">{{ $t('notifyWay.addTitle') }}</el-breadcrumb-item>
        <el-breadcrumb-item v-if="state === 'editNotifyWay'">{{ $t('notifyWay.editTitle') }}</el-breadcrumb-item>
        <el-breadcrumb-item v-if="state === 'detailNotifyWay'">{{ $t('notifyWay.detailTitle') }}</el-breadcrumb-item>
    </el-breadcrumb>
    <el-divider />
    <el-tabs v-model="activeName" @tab-click="handleClick">
        <el-tab-pane :label="$t('notifyWay.title')" name="notifyWay">
            <NotifyWay @updateState="updateState"/>
        </el-tab-pane>
        <el-tab-pane :label="$t('notifyConfig.title')" name="notifyConfig">
            <NotifyConfig />
        </el-tab-pane>
    </el-tabs>
</template>

<script setup lang='ts'>
import "element-plus/es/components/message-box/style/index";
import { useI18n } from "vue-i18n";
import NotifyWay from "@/views/notify/NotifyWay/NotifyWay.vue";
import NotifyConfig from "@/views/notify/NotifyWay/NotifyConfig.vue";

const { t } = useI18n();

const activeName = ref<string>('notifyWay')
const state = ref<string>('')

const updateState = (val: string) => {
    state.value = val
}

const handleClick = () => {
    if (activeName.value === 'notifyConfig') {
        state.value = ''
    }
}

onMounted(() => {
})

</script>
<style scoped lang='scss'></style>