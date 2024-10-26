<template>
  <div class="panel-c" id="tuningChooseVersion">
    <div class="panel-header">
      <div class="label-color mb ft-xlg">{{ $t('components.ChooseVersion.5mpmxod8xno0') }}</div>
      <div class="flex-row-start mb">
        <div class="label-color mr">{{ $t('components.ChooseVersion.5mpmxod8yh00') }}:</div>
        <a-tag
          class="mr"
          size="large"
          color="green"
        >{{ getDescVersion() }}</a-tag>
      </div>
      <div
        v-if="data.installType === 'import'"
        style="color: red; font-weight: bold; font-size: large;"
      >{{ $t('components.ChooseVersion.else3') }}</div>
    </div>
    <div class="flex-row-center panel-body">
      <div
        :class="'version-c card-item-c mr-xlg ' + (currVersion === InstallModeEnum.OFF_LINE ? 'center-item-active' : '')"
        @click="chooseVer(InstallModeEnum.OFF_LINE)"
      >
        <svg-icon
          icon-class="ops-mini-version"
          class="icon-size image mb"
        ></svg-icon>
        <div class="label-color ft-lg mb">{{ $t('components.ChooseVersion.5mpmxod8yxs0') }}</div>
        <div class="label-color remark">{{ $t('components.ChooseVersion.5mpmxod8z3w0') }}</div>
      </div>
      <div
        :class="'version-c card-item-c ' + (currVersion === InstallModeEnum.ON_LINE ? 'center-item-active' : '')"
        @click="chooseVer(InstallModeEnum.ON_LINE)"
      >
        <svg-icon
          icon-class="ops-cluster"
          class="icon-size image mb"
        ></svg-icon>
        <div class="label-color ft-lg mb">{{ $t('components.ChooseVersion.5mpmxod8za00') }}</div>
        <div class="label-color remark">{{ $t('components.ChooseVersion.else1') }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useOpsStore } from '@/store'
import { onMounted, reactive, ref, computed, inject } from 'vue'
import { InstallModeEnum } from '@/types/ops/install'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const currVersion = ref(InstallModeEnum.OFF_LINE)
const versionStore = useOpsStore()
const loadingFunc = inject<any>('loading')

const data = reactive({
  installType: 'install'
})

const installTypes = computed(() => [
  { label: t('components.ChooseVersion.5mpmxod8zs80'), value: 'install' },
  { label: t('components.ChooseVersion.5mpmxod901g0'), value: 'import' }
])

onMounted(() => {
  localStorage.removeItem('Static-pluginBase-opsOpsInstall')
  loadingFunc.setBackBtnShow(false)
  if (versionStore.getInstallConfig.installMode) {
    currVersion.value = versionStore.getInstallConfig.installMode
    data.installType = versionStore.getInstallConfig.installType
  } else {
    versionStore.setInstallContext({
      installType: data.installType,
      installMode: InstallModeEnum.OFF_LINE,
      openGaussVersionNum: '5.0.0'
    })
  }
})

const installTypeChange = (val: any) => {
  if (val) {
    versionStore.setInstallContext({
      installType: data.installType
    })
  }
}

const chooseVer = (ver: InstallModeEnum) => {
  currVersion.value = ver
  versionStore.setInstallContext({
    installMode: ver,
    openGaussVersionNum: '5.0.0',
    packagePath: '',
    packageName: '',
    installPackagePath: ''
  })
}

const getDescVersion = (): string => {
  switch (currVersion.value) {
    case InstallModeEnum.ON_LINE:
      return t('components.ChooseVersion.5mpmxod8za00')
    case InstallModeEnum.OFF_LINE:
      return t('components.ChooseVersion.5mpmxod8yxs0')
    case InstallModeEnum.ENTERPRISE:
      return t('components.ChooseVersion.5mpmxod8zg00')
    default:
      return t('components.ChooseVersion.else2')
  }
}

</script>

<style lang="less" scoped>
@import url('~@/assets/style/ops/ops.less');

.install-type {
  .label {
    white-space: nowrap;
  }

  .select-w {
    width: 200px;
  }
}

.version-c {
  width: 300px;
  height: 300px;

  .remark {
    display: none;
    line-height: 1.5em;
    padding: 10px;
    overflow: auto;
    white-space: pre-line;
  }

  &:hover .remark {
    display: initial;
  }

  &:hover .image {
    display: none;
  }
}
</style>
