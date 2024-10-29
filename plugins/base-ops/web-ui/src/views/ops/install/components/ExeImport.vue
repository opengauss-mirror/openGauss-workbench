<template>
  <div class="exe-install-c" id="exeImport">
    <div
      class="flex-col full-w full-h"
      v-if="exeResult === exeResultEnum.SUCESS"
    >
      <svg-icon
        icon-class="ops-install-success"
        class="icon-size mb"
      ></svg-icon>
      <div class="label-color mb-xlg">{{ $t('components.ExeImport.5mpmzg3zqu80') }}</div>
      <div
        class="install-connect-c flex-col mb-xlg"
        v-if="installStore.getInstallConfig.openGaussVersion === OpenGaussVersionEnum.ENTERPRISE"
      >
        <div class="ft-b mb">{{ $t('enterprise.ExeInstall.5mpm9j35a4o0') }}</div>
        <div class="flex-row mb-s">
          <div class="label-w">{{ $t('enterprise.ClusterConfig.5mpm3ku3hcg0') }}</div>
          <div class="label-value">{{ installStore.getInstallConfig.clusterId }}</div>
        </div>
        <div class="flex-row mb-s">
          <div class="label-w">{{ $t('enterprise.ClusterConfig.5mpm3ku3iz80') }}</div>
          <div class="label-value">{{ installStore.getEnterpriseConfig.port }}</div>
        </div>
        <div class="flex-row mb-s">
          <div class="label-w">{{ $t('enterprise.ExeInstall.else1') }}</div>
          <div class="label-value">{{
            databaseUsername ? databaseUsername : '--'
          }}</div>
        </div>
        <div
          class="flex-row mb-s"
          v-for="(nodeData, index) in installStore.getEnterpriseConfig.nodeConfigList"
          :key="index"
        >
          <div class="label-w">{{ nodeData.clusterRole === ClusterRoleEnum.MASTER ? $t('enterprise.ClusterConfig.else3') :
            ($t('enterprise.ClusterConfig.else4') + index) }}</div>
          <div class="label-value">{{ nodeData.publicIp }}</div>
        </div>
      </div>
      <div
        class="install-connect-c flex-col mb-xlg"
        v-if="installStore.getInstallConfig.openGaussVersion === OpenGaussVersionEnum.LITE"
      >
        <div class="ft-b mb">{{ $t('lightweight.ExeInstall.5mpmjd1mbdc0') }}</div>
        <div class="flex-row mb-s">
          <div class="label-w">{{ $t('lightweight.InstallConfig.5mpmkfqy8nc0') }}</div>
          <div class="label-value">{{ installStore.getInstallConfig.clusterId }}</div>
        </div>
        <div class="flex-row mb-s">
          <div class="label-w">{{ $t('lightweight.InstallConfig.5mpmkfqyasw0') }}</div>
          <div class="label-value">{{ installStore.getLiteConfig.port }}</div>
        </div>
        <div class="flex-row mb-s">
          <div class="label-w">{{ $t('lightweight.ExeInstall.5mpmjd1mbo00') }}</div>
          <div class="label-value">{{
            databaseUsername ? databaseUsername : '--'
          }}</div>
        </div>
        <div
          class="flex-row mb-s"
          v-for="(nodeData, index) in installStore.getLiteConfig.nodeConfigList"
          :key="index"
        >
          <div class="label-w">{{ nodeData.clusterRole === ClusterRoleEnum.MASTER ? $t('enterprise.ClusterConfig.else3') :
            ($t('enterprise.ClusterConfig.else4') + index) }}</div>
          <div class="label-value">{{ nodeData.publicIp }}</div>
        </div>
      </div>
      <div
        class="install-connect-c flex-col mb-xlg"
        v-if="installStore.getInstallConfig.openGaussVersion === OpenGaussVersionEnum.MINIMAL_LIST"
      >
        <div class="ft-b mb">{{ $t('simple.ExeInstall.5mpmsp16oy40') }}</div>
        <div class="flex-row mb-s">
          <div class="label-w">{{ $t('simple.InstallConfig.5mpmu0laqc80') }}</div>
          <div class="label-value">{{ installStore.getInstallConfig.clusterId }}</div>
        </div>
        <div class="flex-row mb-s">
          <div class="label-w">{{ $t('simple.InstallConfig.5mpmu0larj40') }}</div>
          <div class="label-value">{{ installStore.getMiniConfig.port }}</div>
        </div>
        <div class="flex-row mb-s">
          <div class="label-w">{{ $t('simple.ExeInstall.5mpmsp16pb40') }}</div>
          <div class="label-value">{{
            databaseUsername ? databaseUsername : '--'
          }}</div>
        </div>
        <div
          class="flex-row mb-s"
          v-for="(nodeData, index) in installStore.getMiniConfig.nodeConfigList"
          :key="index"
        >
          <div class="label-w">{{ nodeData.clusterRole === ClusterRoleEnum.MASTER ? $t('enterprise.ClusterConfig.else3') :
            ($t('enterprise.ClusterConfig.else4') + index) }}</div>
          <div class="label-value">{{ nodeData.publicIp }}</div>
        </div>
      </div>
      <div class="flex-row">
        <a-button
          type="outline"
          class="mr"
          @click="goHome"
        >{{
          $t('components.ExeImport.5mpmzg3zseo0')
        }}</a-button>
        <a-button
          type="primary"
          @click="goOps"
        >{{
          $t('components.ExeImport.5mpmzg3zskw0')
        }}</a-button>
      </div>
    </div>
    <div
      class="flex-col full-w full-h ft-lg"
      v-if="exeResult === exeResultEnum.FAIL"
    >
      <div class="label-color flex-row">
        <icon-close-circle-fill
          class="mr"
          style="color: red; width: 24px; height: 24px"
        />
        {{ $t('components.ExeImport.5mpmzg3zsu80') }} {{ failMsg }}
      </div>
    </div>
    <a-spin
      v-if="loading"
      class="flex-col full-w full-h"
      :loading="loading"
      :tip="$t('components.ExeImport.5mpmzg3zt0g0')"
    >
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
import { inject, onMounted, ref } from 'vue'
import 'xterm/css/xterm.css'
import { importOpenGauss } from '@/api/ops'
import { useOpsStore } from '@/store'
import { KeyValue } from '@/types/global'
import { useI18n } from 'vue-i18n'
import { ClusterRoleEnum, OpenGaussVersionEnum } from '@/types/ops/install'
const { t } = useI18n()
const installStore = useOpsStore()

enum exeResultEnum {
  UN_INSTALL = Number(-1),
  SUCESS = Number(1),
  FAIL = Number(0)
}
const exeResult = ref<number>(exeResultEnum.UN_INSTALL)

const databaseUsername = ref('')

const loading = ref(false)

const failMsg = ref('')

const loadingFunc = inject<any>('loading')

onMounted(() => {
  loadingFunc.setNextBtnShow(false)
  exeImportMini()
})

const exeImportMini = () => {
  // execute import
  loading.value = true
  const param: KeyValue = Object.assign({}, installStore.getInstallConfig)
  param.minimalistInstallConfig = installStore.getMiniConfig
  param.liteInstallConfig = installStore.getLiteConfig
  param.enterpriseInstallConfig = installStore.getEnterpriseConfig
  loadingFunc.toLoading()
  importOpenGauss(param).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      loadingFunc.setBackBtnShow(false)
      loadingFunc.setNextBtnShow(false)
      if (param.openGaussVersion === OpenGaussVersionEnum.MINIMAL_LIST) {
        databaseUsername.value = param.minimalistInstallConfig.databaseUsername
      }
      if (param.openGaussVersion === OpenGaussVersionEnum.LITE) {
        databaseUsername.value = param.liteInstallConfig.databaseUsername
      }
      if (param.openGaussVersion === OpenGaussVersionEnum.ENTERPRISE) {
        databaseUsername.value = param.enterpriseInstallConfig.databaseUsername
      }
      exeResult.value = exeResultEnum.SUCESS
    } else {
      loading.value = false
      loadingFunc.cancelLoading()
      exeResult.value = exeResultEnum.FAIL
      failMsg.value = res.msg
    }
  }).catch((err) => {
    loading.value = false
    loadingFunc.cancelLoading()
    exeResult.value = exeResultEnum.FAIL
    console.log(t('components.ExeImport.5mpmzg3ztak0'), err)
    failMsg.value = err.toString()
  }).finally(() => {
    loadingFunc.cancelLoading()
    loading.value = false
  })
}

const goHome = () => {
  window.$wujie?.props.methods.jump({
    name: 'Dashboard'
  })
  loadingFunc.initData()
}

const goOps = () => {
  window.$wujie?.props.methods.jump({
    name: 'Static-pluginBase-opsMonitorDailyOps'
  })
  loadingFunc.initData()
}

const beforeConfirm = async (): Promise<boolean> => {
  return true
}

defineExpose({
  beforeConfirm
})

</script>

<style lang="less" scoped>
.exe-install-c {
  width: 100%;
  height: calc(100% - 28px - 50px);
  overflow-y: auto;

  .install-connect-c {
    padding: 20px 100px;
    background-color: #f2f3f5;
    border-radius: 8px;

    .content {
      color: rgb(var(--arcoblue-6));
    }

    .label-w {
      width: 80px;
      text-align: left;
      margin-right: 15px;
    }

    .label-value {
      width: 100px;
      text-align: left;
    }
  }

  .icon-size {
    width: 100px;
    height: 100px;
  }
}
</style>
