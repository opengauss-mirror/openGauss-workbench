<template>
  <div class="panel-c" id="chooseVersion">
    <div class="panel-header">
      <div class="label-color mb ft-xlg">{{ $t('components.ChooseVersion.5mpmxod8xno0') }}</div>
      <div class="flex-row-start mb">
        <div class="label-color mr">{{ $t('components.ChooseVersion.5mpmxod8yh00') }}:</div>
        <a-tag
          class="mr"
          size="large"
          color="green"
        >{{ getDescVersion() }}</a-tag>
        <div
          v-if="currVersion !== OpenGaussVersionEnum.OPENlOOKENG"
          class="install-type flex-row"
        >
          <div class="label-color label mr">{{ $t('components.ChooseVersion.5mpmxod8yr40') }}:</div>
          <!-- <a-select style="width: 200px" v-model="data.installType" @change="installTypeChange">
            <a-option v-for="(item, index) in installTypes" :key="index" :label="item.label" :value="item.value">
            </a-option>
          </a-select> -->
          <a-radio-group
            v-model="data.installType"
            :options="installTypes"
            @change="installTypeChange"
          >
            <template #label="{ data }">
              {{ data.label }}
            </template>
          </a-radio-group>
          <a-button
            type="primary"
            @click="addHostbulk"
          >
          {{ $t('components.ChooseVersion.5mpmxod901g1') }}
          </a-button>
          <import-cluster ref="addHostBulkRef"></import-cluster>
        </div>
      </div>
      <div
        v-if="data.installType === 'import'"
        style="color: red; font-weight: bold; font-size: large;"
      >{{ $t('components.ChooseVersion.else3') }}</div>
    </div>
    <a-divider></a-divider>
    <div class="flex-row-center panel-body">
      <div
        :class="'version-c card-item-c mr-xlg ' + (currVersion === OpenGaussVersionEnum.MINIMAL_LIST ? 'center-item-active' : '')"
        @click="chooseVer(OpenGaussVersionEnum.MINIMAL_LIST)"
      >
        <svg-icon
          icon-class="ops-mini-version"
          class="icon-size image mb"
        ></svg-icon>
        <div class="label-color ft-lg mb">{{ $t('components.ChooseVersion.5mpmxod8yxs0') }}</div>
        <div class="label-color remark">{{ $t('components.ChooseVersion.5mpmxod8z3w0') }}</div>
      </div>
      <div
        :class="'version-c card-item-c mr-xlg ' + (currVersion === OpenGaussVersionEnum.LITE ? 'center-item-active' : '')"
        @click="chooseVer(OpenGaussVersionEnum.LITE)"
      >
        <svg-icon
          icon-class="ops-cluster"
          class="icon-size image mb"
        ></svg-icon>
        <div class="label-color ft-lg mb">{{ $t('components.ChooseVersion.5mpmxod8za00') }}</div>
        <div class="label-color remark">{{ $t('components.ChooseVersion.else1') }}</div>
      </div>
      <div
        :class="'version-c card-item-c mr-xlg ' + (currVersion === OpenGaussVersionEnum.ENTERPRISE ? 'center-item-active' : '')"
        @click="chooseVer(OpenGaussVersionEnum.ENTERPRISE)"
      >
        <svg-icon
          icon-class="ops-cluster"
          class="icon-size image mb"
        ></svg-icon>
        <div class="label-color ft-lg mb">{{ $t('components.ChooseVersion.5mpmxod8zg00') }}</div>
        <div class="label-color remark">{{ $t('components.ChooseVersion.5mpmxod8zlw0') }}</div>
      </div>
      <div
        :class="'version-c card-item-c ' + (currVersion === OpenGaussVersionEnum.OPENlOOKENG ? 'center-item-active' : '')"
        @click="chooseVer(OpenGaussVersionEnum.OPENlOOKENG)"
      >
        <svg-icon
          icon-class="ops-cluster"
          class="icon-size image mb"
        ></svg-icon>
        <div class="label-color ft-lg mb">{{ $t('components.ChooseVersion.openLooKeng') }}</div>
        <div class="label-color remark">{{ $t('components.ChooseVersion.openLooKengRemark') }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useOpsStore } from '@/store'
import { onMounted, reactive, ref, computed, inject } from 'vue'
import { OpenGaussVersionEnum } from '@/types/ops/install'
import { useI18n } from 'vue-i18n'
import importCluster from '../../import/importCluster.vue'
import {KeyValue} from '@/types/global'

const { t } = useI18n()
const currVersion = ref(OpenGaussVersionEnum.MINIMAL_LIST)
const versionStore = useOpsStore()

const loadingFunc = inject<any>('loading')

const data = reactive({
  installType: 'install'
})

const addHostBulkRef = ref<null | InstanceType<typeof importCluster>>(null)

const addHostbulk = () => {
  console.log("addHostBulkRef.value: "+addHostBulkRef.value);
  addHostBulkRef.value?.open()
}

const installTypes = computed(() => [
  { label: t('components.ChooseVersion.5mpmxod8zs80'), value: 'install' },
  { label: t('components.ChooseVersion.5mpmxod901g0'), value: 'import' }
])

onMounted(() => {
  localStorage.removeItem('Static-pluginBase-opsOpsInstall')
  loadingFunc.setBackBtnShow(false)
  if (versionStore.getInstallConfig.openGaussVersion) {
    currVersion.value = versionStore.getInstallConfig.openGaussVersion
    data.installType = versionStore.getInstallConfig.installType
  } else {
    versionStore.setInstallContext({
      installType: data.installType,
      openGaussVersion: OpenGaussVersionEnum.MINIMAL_LIST,
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

const chooseVer = (ver: OpenGaussVersionEnum) => {
  console.log(t('components.ChooseVersion.5mpmxod907k0'), ver)
  currVersion.value = ver
  versionStore.setInstallContext({
    openGaussVersion: ver,
    openGaussVersionNum: '5.0.0',
    packagePath: '',
    packageName: '',
    installPackagePath: ''
  })
}

const getDescVersion = (): string => {
  switch (currVersion.value) {
    case OpenGaussVersionEnum.LITE:
      return t('components.ChooseVersion.5mpmxod8za00')
    case OpenGaussVersionEnum.MINIMAL_LIST:
      return t('components.ChooseVersion.5mpmxod8yxs0')
    case OpenGaussVersionEnum.ENTERPRISE:
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
  width: 250px;
  height: 250px;

  .remark {
    display: none;
    line-height: 1.5em;
    padding: 10px;
    overflow: auto;
  }

  &:hover .remark {
    display: initial;
  }

  &:hover .image {
    display: none;
  }
}
</style>
