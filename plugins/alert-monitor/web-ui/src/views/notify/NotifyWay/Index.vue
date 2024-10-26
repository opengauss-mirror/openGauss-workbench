<template>
  <div class="page-header" id="notifyWayIndex">
    <div class="icon"></div>
    <div class="title" v-if="activeName === 'notifyWay'">{{ t('notifyWay.title') }}</div>
    <div class="title" v-if="activeName === 'notifyConfig'">{{ t('notifyConfig.title') }}</div>
    <div class="seperator"></div>
    <el-breadcrumb separator="/" style="flex-grow: 1">
      <el-breadcrumb-item>
        <div v-if="activeName === 'notifyConfig'">{{ t('notifyConfig.title') }} </div>
        <div v-if="activeName === 'notifyWay'">
          <div v-if="state" @click="goback">
            <a>{{ t('notifyWay.title') }}</a>
          </div>
          <div v-else>{{ t('notifyWay.title') }} </div>
        </div>
      </el-breadcrumb-item>
      <el-breadcrumb-item v-if="state && activeName === 'notifyWay'">
        <div v-if="state === 'addNotifyWay'">{{ t('notifyWay.addTitle') }} </div>
        <div v-if="state === 'editNotifyWay'">{{ t('notifyWay.editTitle') }} </div>
        <div v-if="state === 'detailNotifyWay'">{{ t('notifyWay.detailTitle') }} </div>
      </el-breadcrumb-item>
    </el-breadcrumb>
  </div>
  <el-tabs v-model="activeName" @tab-click="handleClick" style="margin: 0;">
    <el-tab-pane :label="$t('notifyWay.title')" name="notifyWay">
      <NotifyWay @updateState="updateState" ref="notifyWay"/>
    </el-tab-pane>
    <el-tab-pane :label="$t('notifyConfig.title')" name="notifyConfig">
      <NotifyConfig style="margin-top: 10px;" />
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
const notifyWay = ref(null)

const updateState = (val: string) => {
  state.value = val
}

const handleClick = () => {
  if (activeName.value === 'notifyConfig') {
    state.value = ''
  } else {
    notifyWay.value.cancelNotifyWay()
  }
}

const goback = () => {
  notifyWay.value.cancelNotifyWay()
}

onMounted(() => {
})

</script>
<style scoped lang='scss'></style>