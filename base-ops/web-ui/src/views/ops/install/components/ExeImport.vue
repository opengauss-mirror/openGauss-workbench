<template>
  <div class="exe-install-c">
    <div class="flex-col full-w full-h" v-if="exeResult === exeResultEnum.SUCESS">
      <svg-icon icon-class="ops-install-success" class="icon-size mb"></svg-icon>
      <div class="mb-xlg">{{ $t('components.ExeImport.5mpmzg3zqu80') }}</div>
      <div class="install-connect-c flex-col mb-lg">
        <div class="ft-b mb">{{ $t('components.ExeImport.5mpmzg3zrrw0') }}</div>
        <div class="mb">{{ $t('components.ExeImport.5mpmzg3zs0o0') }}: <span class="content">gaussdb</span></div>
        <div>{{ $t('components.ExeImport.5mpmzg3zs6w0') }}: <span class="content">{{
            installStore.getMiniConfig.databasePassword
        }}</span></div>
      </div>
      <div class="flex-row">
        <a-button type="outline" class="mr" @click="$router.push({ name: 'Dashboard' })">{{
            $t('components.ExeImport.5mpmzg3zseo0')
        }}</a-button>
        <a-button type="primary" @click="$router.push({ name: 'DailyOps' })">{{ $t('components.ExeImport.5mpmzg3zskw0')
        }}</a-button>
      </div>
    </div>
    <div class="flex-col full-w full-h ft-lg" v-if="exeResult === exeResultEnum.FAIL">
      <div class="flex-row">
        <icon-close-circle-fill class="mr" style="color: red; width: 24px; height: 24px" />
        {{ $t('components.ExeImport.5mpmzg3zsu80') }} {{ failMsg }}
      </div>
    </div>
    <a-spin v-if="loading" class="flex-col full-w full-h" :loading="loading"
      :tip="$t('components.ExeImport.5mpmzg3zt0g0')">
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
const { t } = useI18n()
const installStore = useOpsStore()

enum exeResultEnum {
  UN_INSTALL = Number(-1),
  SUCESS = Number(1),
  FAIL = Number(0)
}
const exeResult = ref<number>(exeResultEnum.UN_INSTALL)

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
  }

  .icon-size {
    width: 100px;
    height: 100px;
  }
}
</style>
