<template>
  <div class="agentDrawer">
    <el-drawer v-model="drawer2" :title="$t('physical.index.agentManager')" class="drawer openDesignDrawer" :direction="direction"
               :before-close="emitClose" :z-index="980">
      <template #default>
        <div class="agentTitle">{{ $t('physical.index.baseInfo') }}</div>
        <div class="baseInfo">
          <div v-for="(item, index) in expressList" class="baseItem">
            <div class="agentLabel">{{ item.label }}</div>
            <div class="agentValue">
              {{ item.value }}
              <el-button
                v-if="item.label === t('physical.index.operationStatus') && props.rowInfo?.agentStatus === AgentStatus.RUNNING"
                text @click="stopAgent" :loading="stopAgentLoading">
                <span class="statusDot onLineColor"></span>{{ $t('physical.index.stop') }}
              </el-button>
              <el-button
                v-if="item.label === t('physical.index.operationStatus') && props.rowInfo?.agentStatus === AgentStatus.STOP"
                text @click="startAgent" :loading="startAgentLoading">
                <span class="statusDot offLineColor"></span>{{ $t('physical.index.start') }}
              </el-button>
            </div>
          </div>
        </div>
      </template>
      <template #footer>
        <div style="flex: auto" class="footerBtn">
          <el-button @click="unInstall" :loading="unInstallLoading">{{ $t('physical.index.uninstall') }}</el-button>
          <el-button type="primary" @click="emitClose">{{ $t('physical.index.close') }}</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { DrawerProps } from 'element-plus'
import { stopHostAgent, startHostAgent, uninstallAgent, hostUserListAll } from '@/api/ops'
import showMessage from '@/hooks/showMessage'
import { AgentStatus } from '../physicalType'
import { KeyValue } from '@/types/global'
const props = defineProps({
  rowInfo: {
    type: Object,
  }
})

const { t } = useI18n()
const emits = defineEmits(['closeDrawer', 'updateTableData'])
const direction = ref<DrawerProps['direction']>('rtl')
const drawer2 = ref(true)
const expressList = computed(() => [
  {
    label: t('physical.index.agentName'),
    value: props.rowInfo?.agentName || '--'
  },
  {
    label: t('physical.index.operationStatus'),
    value: unInstallLoading.value ? '--' : (props.rowInfo?.agentStatus !== AgentStatus.STOP ? t('physical.index.running') : t('physical.index.offline'))
  },
  {
    label: t('physical.index.serverIp'),
    value: props.rowInfo?.publicIp || '--'
  },
  {
    label: t('physical.index.port'),
    value: props.rowInfo?.agentInstallPort || '--'
  },
  {
    label: t('physical.index.installPath'),
    value: props.rowInfo?.agentInstallPath || '--'
  },
  {
    label: t('physical.index.userName'),
    value: props.rowInfo?.agentInstallUsername || '--'
  },
])

const emitClose = () => {
  emits('closeDrawer')
}

const stopAgentLoading = ref(false)
const stopAgent = () => {
  stopAgentLoading.value = true
  if (props.rowInfo?.hostId) {
    stopHostAgent(props.rowInfo.hostId).then((res: KeyValue) => {
      if (res.code === 200) {
        showMessage('success', t('physical.index.stopSuccess'))
        emits('updateTableData')
        stopAgentLoading.value = false
      }
    }) .catch((error) => {
      console.log(error)
      stopAgentLoading.value = false
    })
  }
}

const startAgentLoading = ref(false)
const startAgent = () => {
  startAgentLoading.value = true
  if (props.rowInfo?.hostId) {
    startHostAgent(props.rowInfo.hostId).then((res: KeyValue) => {
      if (res.code === 200) {
        showMessage('success', t('physical.index.startSuccess'))
        emits('updateTableData')
        startAgentLoading.value = false
      }
    }) .catch((error) => {
      console.log(error)
      startAgentLoading.value = false
    })
  }
}

const unInstallLoading = ref(false)
const unInstall = () => {
  unInstallLoading.value = true
  if (props.rowInfo?.hostId) {
    uninstallAgent(props.rowInfo.hostId).then((res: KeyValue) => {
      if (res.code === 200) {
        showMessage('success', t('physical.index.uninstallSuccess'))
        emits('updateTableData')
      }
    }) .catch((error) => {
      console.log(error)
    })
  }
}

onMounted(() => {
  unInstallLoading.value = false
  stopAgentLoading.value = false
  startAgentLoading.value = false
})

</script>
<style lang="less" scoped>
.agentDrawer {
  ::v-deep(.el-drawer) {
    width: 432px !important;
    .el-drawer__header {
      margin-bottom: 0;
      border-bottom: 1px solid #dfe5ef;
      padding: 16px 16px 16px 24px;
      color: #000;
    }

    .el-drawer__body {
      .agentTitle {
        height: 22px;
        line-height: 22px;
        font-size: 14px;
      }

      .baseInfo {
        width: 100%;
        height: fit-content;
        display: flex;
        flex-direction: column;
        gap: 16px;
        margin-top: 16px;

        .baseItem {
          width: 100%;
          height: 22px;
          line-height: 22px;
          display: flex;

          .agentLabel {
            width: 124px;
            color: var(--o-text-color-secondary);
          }

          .agentValue {
            flex: 1;
            color: var(--o-text-color-primary);
          }
        }
      }
    }
  }
}
</style>
