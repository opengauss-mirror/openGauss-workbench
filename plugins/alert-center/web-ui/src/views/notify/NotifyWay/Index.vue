<template>
    <div class="page-header">
        <div class="icon"></div>
        <div class="title" v-if="activeName === 'notifyWay'">{{ t('notifyWay.title') }}</div>
        <div class="title" v-if="activeName === 'notifyConfig'">{{ t('notifyConfig.title') }}</div>
        <div class="seperator"></div>
        <div class="alert-title" v-if="activeName === 'notifyWay'">{{ t('notifyWay.title') }} </div>
        <div class="alert-title" v-if="activeName === 'notifyConfig'">{{ t('notifyConfig.title') }} </div>
        <div class="alert-seperator">&nbsp;/&nbsp;</div>
        <div class="alert-title" v-if="state === 'addNotifyWay'">{{ t('notifyWay.addTitle') }} </div>
        <div class="alert-title" v-if="state === 'editNotifyWay'">{{ t('notifyWay.editTitle') }} </div>
        <div class="alert-title" v-if="state === 'detailNotifyWay'">{{ t('notifyWay.detailTitle') }} </div>
    </div>
    <el-tabs v-model="activeName" @tab-click="handleClick" style="margin: 0;">
        <el-tab-pane :label="$t('notifyWay.title')" name="notifyWay">
            <NotifyWay @updateState="updateState" />
        </el-tab-pane>
        <el-tab-pane :label="$t('notifyConfig.title')" name="notifyConfig">
            <NotifyConfig style="margin-top: 10px;"/>
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
<style scoped lang='scss'>
</style>