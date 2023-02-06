<template>
  <div class="install-config-c">
    <div class="flex-row-center">
      <div class="flex-col-start">
        <div class="label-color ft-b mb">{{ $t('simple.InstallConfig.5mpmu0lapic0') }}</div>
        <a-form :model="data.form" :rules="data.rules" :style="{ width: '800px' }" ref="formRef">
          <a-form-item field="clusterId" :label="$t('simple.InstallConfig.5mpmu0laqc80')" validate-trigger="blur">
            <a-input v-model="data.form.clusterId" :placeholder="$t('simple.InstallConfig.5mpmu0laqiw0')" />
          </a-form-item>
          <a-form-item field="hostId" :label="$t('simple.InstallConfig.5mpmu0laqow0')">
            <a-select :loading="hostListLoading" v-model="data.form.hostId" @change="changeHostId"
              :placeholder="$t('simple.InstallConfig.5mpmu0laqss0')" @popup-visible-change="hostPopupChange">
              <a-option v-for="item in hostList" :key="item.hostId" :value="item.hostId">{{
                item.privateIp
                  + '(' +
                  (item.publicIp ? item.publicIp : '--') + ')'
              }}</a-option>
            </a-select>
          </a-form-item>
          <a-form-item field="rootPassword" :label="$t('simple.InstallConfig.else1')" validate-trigger="blur">
            <a-input-password v-model="data.form.rootPassword" :placeholder="$t('simple.InstallConfig.5mpmu0laqwo0')"
              allow-clear />
          </a-form-item>
          <a-form-item field="installUserId" :label="$t('simple.InstallConfig.5mpmu0lar0c0')">
            <a-select :loading="installUserLoading" v-model="data.form.installUserId"
              @popup-visible-change="hostUserPopupChange">
              <a-option v-for="item in userListByHost" :key="item.hostUserId" :value="item.hostUserId">{{
                item.username
              }}</a-option>
            </a-select>
          </a-form-item>
          <a-form-item field="installPath" :label="$t('simple.InstallConfig.5mpmu0lar480')" validate-trigger="blur">
            <a-input v-model="data.form.installPath" :placeholder="$t('simple.InstallConfig.5mpmu0lar800')" />
          </a-form-item>
          <!-- <a-form-item field="dataPath" :label="$t('simple.InstallConfig.5mpmu0larbk0')" validate-trigger="blur">
            <a-input v-model="data.form.dataPath" :placeholder="$t('simple.InstallConfig.5mpmu0larew0')" />
          </a-form-item> -->
          <a-form-item field="port" :label="$t('simple.InstallConfig.5mpmu0larj40')" validate-trigger="blur">
            <a-input-number v-model="data.form.port" :placeholder="$t('simple.InstallConfig.5mpmu0larmo0')" />
          </a-form-item>
          <a-form-item field="databaseUsername" :label="$t('simple.InstallConfig.5mpmu0larq40')" validate-trigger="blur"
            v-if="installType === 'import'">
            <a-input v-model="data.form.databaseUsername" :placeholder="$t('simple.InstallConfig.5mpmu0larto0')"
              allow-clear />
          </a-form-item>
          <a-form-item field="databasePassword" :label="$t('simple.InstallConfig.5mpmu0larx00')"
            validate-trigger="blur">
            <a-input-password v-model="data.form.databasePassword"
              :placeholder="$t('simple.InstallConfig.5mpmu0las0k0')" allow-clear />
          </a-form-item>
        </a-form>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { MiniNodeConfig, MinimalistInstallConfig, ClusterRoleEnum } from '@/types/ops/install'
import { KeyValue } from '@/types/global'

import { useOpsStore } from '@/store'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { hostListAll, hostUserListWithoutRoot, hasName } from '@/api/ops'
import { encryptPassword } from '@/utils/jsencrypt'
import { Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const installStore = useOpsStore()
const data = reactive({
  form: {
    clusterId: '',
    hostId: '',
    rootPassword: '',
    privateIp: '',
    publicIp: '',
    installUserId: '',
    installPath: '/opt/openGauss',
    dataPath: '/opt/openGauss/data',
    port: Number(5432),
    databaseUsername: '',
    databasePassword: '',
    isInstallDemoDatabase: true
  },
  rules: {}
})

const hostListLoading = ref<boolean>(false)
const hostList = ref<KeyValue[]>([])

onMounted(() => {
  initData()
  if (installStore.getMiniConfig && installStore.getMiniConfig.nodeConfigList) {
    const miniConfig: any = installStore.getMiniConfig
    Object.assign(data.form, {
      clusterId: miniConfig.nodeConfigList[0].clusterId,
      hostId: miniConfig.nodeConfigList[0].hostId,
      rootPassword: miniConfig.nodeConfigList[0].rootPassword,
      privateIp: miniConfig.nodeConfigList[0].privateIp,
      publicIp: miniConfig.nodeConfigList[0].publicIp,
      installUserId: miniConfig.nodeConfigList[0].installUserId,
      installPath: miniConfig.nodeConfigList[0].installPath,
      dataPath: miniConfig.nodeConfigList[0].dataPath,
      port: miniConfig.port,
      databaseUsername: miniConfig.databaseUsername,
      databasePassword: miniConfig.databasePassword,
      isInstallDemoDatabase: miniConfig.nodeConfigList[0].isInstallDemoDatabase
    })
  }
  getHostList()
})

const initData = () => {
  data.rules = {
    clusterId: [
      { required: true, 'validate-trigger': 'blur', message: t('simple.InstallConfig.5mpmu0laqiw0') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            const param = {
              name: value
            }
            hasName(param).then((res: KeyValue) => {
              if (Number(res.code) === 200) {
                if (res.data.has === 'Y') {
                  cb(t('simple.InstallConfig.5mpmu0las4g0'))
                  resolve(false)
                } else {
                  resolve(true)
                }
              } else {
                cb(t('simple.InstallConfig.5mpmu0las800'))
                resolve(false)
              }
            })
          })
        }
      }
    ],
    port: [{ required: true, 'validate-trigger': 'blur', message: t('simple.InstallConfig.5mpmu0larmo0') }],
    rootPassword: [{ required: true, 'validate-trigger': 'blur', message: t('simple.InstallConfig.5mpmu0laqwo0') }],
    installPath: [{ required: true, 'validate-trigger': 'blur', message: t('simple.InstallConfig.5mpmu0lar800') }],
    dataPath: [{ required: true, 'validate-trigger': 'blur', message: t('simple.InstallConfig.5mpmu0larew0') }],
    databaseUsername: [{ required: true, 'validate-trigger': 'blur', message: t('simple.InstallConfig.5mpmu0larto0') }],
    databasePassword: [{ required: true, 'validate-trigger': 'blur', message: t('simple.InstallConfig.5mpmu0las0k0') }]
  }
}

const hostObj = ref<KeyValue>({})
const getHostList = () => {
  hostListLoading.value = true
  hostListAll().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      hostList.value = []
      hostList.value = res.data
      res.data.forEach((item: KeyValue) => {
        hostObj.value[item.hostId] = item
      })
      if (!data.form.hostId) {
        data.form.hostId = hostList.value[0].hostId
        data.form.privateIp = hostList.value[0].privateIp
        data.form.publicIp = hostList.value[0].publicIp
      } else {
        const getOldHost = hostList.value.find((item: KeyValue) => {
          return item.hostId === data.form.hostId
        })
        if (!getOldHost) {
          data.form.hostId = hostList.value[0].hostId
          data.form.privateIp = hostList.value[0].privateIp
          data.form.publicIp = hostList.value[0].publicIp
        }
      }
      changeHostId()
    } else {
      Message.error('Failed to obtain the host list data')
    }
  }).finally(() => {
    hostListLoading.value = false
  })
}
const installUserLoading = ref<boolean>(false)
const userListByHost = ref<KeyValue[]>([])
const changeHostId = () => {
  if (data.form.hostId) {
    if (hostObj.value[data.form.hostId]) {
      data.form.privateIp = hostObj.value[data.form.hostId].privateIp
      data.form.publicIp = hostObj.value[data.form.hostId].publicIp
    }
    installUserLoading.value = true
    hostUserListWithoutRoot(data.form.hostId).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        userListByHost.value = []
        userListByHost.value = res.data
        if (userListByHost.value.length) {
          data.form.installUserId = userListByHost.value[0].hostUserId
        } else {
          data.form.installUserId = ''
        }
      } else {
        Message.error('Failed to obtain user data from the host')
      }
    }).finally(() => {
      installUserLoading.value = false
    })
  }
}

const hostPopupChange = (val: boolean) => {
  if (val) {
    getHostList()
  }
}

const hostUserPopupChange = (val: boolean) => {
  if (val) {
    changeHostId()
  }
}

const formRef = ref<FormInstance>()

const beforeConfirm = async (): Promise<boolean> => {
  const validRes = await formRef.value?.validate()
  if (!validRes) {
    const param = JSON.parse(JSON.stringify(data.form))
    param.clusterRole = ClusterRoleEnum.MASTER
    param.clusterName = ''
    installStore.setInstallContext({ clusterId: param.clusterId })
    const miniConfig = {
      clusterName: '',
      port: param.port,
      databaseUsername: param.databaseUsername,
      databasePassword: param.databasePassword,
      nodeConfigList: [param as MiniNodeConfig]
    }
    installStore.setMiniConfig(miniConfig as MinimalistInstallConfig)
    return true
  } else {
    return false
  }
}
const installType = computed(() => installStore.getInstallConfig.installType)
defineExpose({
  beforeConfirm
})

</script>

<style lang="less" scoped>
.install-config-c {
  height: calc(100% - 28px - 42px);
  overflow-y: auto;
}
</style>
