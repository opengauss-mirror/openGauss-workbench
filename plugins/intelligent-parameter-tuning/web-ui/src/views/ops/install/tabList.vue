<template>
    <a-tabs v-model:active-key="activeTab.value" lazy-load  @change="handleSwitchTab">
      <a-tab-pane key="1" :title="$t('install.Install.5mpn60ejqkg1')" destroy-on-hide >
        <install ref="installRef" />
      </a-tab-pane>
      <a-tab-pane key="2" :title="$t('install.Install.5mpn60ejqkg2')" destroy-on-hide >
        <task-list ref="taskListRef" />
      </a-tab-pane>
      <a-tab-pane key="3" :title="$t('install.Install.5mpn60ejqkg3')" destroy-on-hide >
        <view-params ref="viewParamsRef" />
      </a-tab-pane>
      <a-tab-pane key="4" :title="$t('install.Install.5mpn60ejqkg4')" destroy-on-hide >
        <logs-list ref="logListRef" />
      </a-tab-pane>
    </a-tabs>
</template>

<script setup lang="ts">
import 'animate.css'
import { firstStep, chooseStep } from './components/installConfig'
import { ref, reactive, computed, provide, onMounted, onBeforeUnmount } from 'vue'
import { Message } from '@arco-design/web-vue'
import { OpenGaussVersionEnum } from '@/types/ops/install'
import Install from './Install.vue'
import TaskList from './components/taskManage/index.vue'
import ViewParams from './components/paramsManage/index.vue'
import LogsList from './components/logsManage/index.vue'

const installRef = ref<null | InstanceType<typeof Install>>(null)
const taskListRef = ref<null | InstanceType<typeof TaskList>>(null)
const viewParamsRef = ref<null | InstanceType<typeof ViewParams>>(null)
const logListRef = ref<null | InstanceType<typeof LogsList>>(null)
const activeTab = reactive({
  value:'1'
})
const handleSwitchTab = (tabName) => {
  activeTab.value = tabName;
}

provide('changeTabs', {handleSwitchTab})
provide('activeTab', activeTab)
</script>

<style>
  body,html {
  width: 100% !important;
}
</style>
