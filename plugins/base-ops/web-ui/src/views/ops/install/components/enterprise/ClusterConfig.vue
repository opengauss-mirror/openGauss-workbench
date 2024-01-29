<template>
  <div class="cluster-config-c">
    <div class="flex-col">
      <div class="flex-col-start">
        <div class="node-top ft-b full-w mb">
          {{ $t('enterprise.ClusterConfig.5mpm3ku3go40') }}
        </div>
        <a-form :model="data.form" :rules="data.rules" auto-label-width :style="{ width: '850px' }" ref="formRef">
          <a-form-item field="clusterId" :label="$t('enterprise.ClusterConfig.5mpm3ku3hcg0')" validate-trigger="blur">
            <a-input v-model="data.form.clusterId" :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3hik0')" />
          </a-form-item>
          <a-form-item field="installPath" :label="$t('enterprise.ClusterConfig.5mpm3ku3hv40')" validate-trigger="blur">
            <a-input v-model="data.form.installPath" :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3i340')" />
          </a-form-item>
          <a-form-item field="installPackagePath" :label="$t('simple.InstallConfig.else6')" validate-trigger="blur">
            <a-input v-model="data.form.installPackagePath" :placeholder="$t('simple.InstallConfig.else7')" />
          </a-form-item>
          <a-form-item field="logPath" :label="$t('enterprise.ClusterConfig.5mpm3ku3i6s0')" validate-trigger="blur">
            <a-input v-model="data.form.logPath" :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3iag0')" />
          </a-form-item>
          <a-form-item field="tmpPath" :label="$t('enterprise.ClusterConfig.5mpm3ku3ie40')" validate-trigger="blur">
            <a-input v-model="data.form.tmpPath" :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3ihg0')" />
          </a-form-item>
          <a-form-item field="omToolsPath" :label="$t('enterprise.ClusterConfig.5mpm3ku3il80')" validate-trigger="blur">
            <a-input v-model="data.form.omToolsPath" :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3iok0')" />
          </a-form-item>
          <a-form-item field="corePath" :label="$t('enterprise.ClusterConfig.5mpm3ku3is00')" validate-trigger="blur">
            <a-input v-model="data.form.corePath" :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3ivk0')" />
          </a-form-item>
          <a-form-item field="port" :label="$t('enterprise.ClusterConfig.5mpm3ku3iz80')" validate-trigger="blur">
            <a-input-number v-model="data.form.port" :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3j300')" :min="0" :max="65535"/>
          </a-form-item>
          <!-- <a-form-item field="databaseKernelArch" :label="$t('enterprise.ClusterConfig.test0')" validate-trigger="blur">
            <a-select v-model="data.form.databaseKernelArch" :placeholder="$t('enterprise.ClusterConfig.test00')">
              <a-select-option value="MASTER_SLAVE">{{ $t('enterprise.ClusterConfig.option1') }}</a-select-option>
              <a-select-option value="SHARING_STORAGE">{{ $t('enterprise.ClusterConfig.option2') }}</a-select-option>
            </a-select>
          </a-form-item> -->
          <a-form-item field="databaseKernelArch" :label="$t('enterprise.ClusterConfig.test0')" validate-trigger="blur">
            <a-input v-model="data.form.databaseKernelArch" :placeholder="$t('enterprise.ClusterConfig.test00')" />
          </a-form-item>
          <a-form-item field="dssHome" :label="$t('enterprise.ClusterConfig.test2')" validate-trigger="blur">
            <a-input v-model="data.form.dssHome" :placeholder="$t('enterprise.ClusterConfig.test1')" />
          </a-form-item>
          <a-form-item field="dssVgName" :label="$t('enterprise.ClusterConfig.test4')" validate-trigger="blur">
            <a-input v-model="data.form.dssVgName" :placeholder="$t('enterprise.ClusterConfig.test3')" />
          </a-form-item>
          <a-form-item field="dssDataLunPath" :label="$t('enterprise.ClusterConfig.test6')" validate-trigger="blur">
            <a-input v-model="data.form.dssDataLunPath" :placeholder="$t('enterprise.ClusterConfig.test5')" />
          </a-form-item>
          <a-form-item field="xlogVgName" :label="$t('enterprise.ClusterConfig.test8')" validate-trigger="blur">
            <a-input v-model="data.form.xlogVgName" :placeholder="$t('enterprise.ClusterConfig.test7')" />
          </a-form-item>
          <a-form-item field="xlogLunPath" :label="$t('enterprise.ClusterConfig.test10')" validate-trigger="blur">
            <a-input v-model="data.form.xlogLunPath" :placeholder="$t('enterprise.ClusterConfig.test9')" />
          </a-form-item>
          <a-form-item field="cmSharingLunPath" :label="$t('enterprise.ClusterConfig.test12')" validate-trigger="blur">
            <a-input v-model="data.form.cmSharingLunPath" :placeholder="$t('enterprise.ClusterConfig.test11')" />
          </a-form-item>
          <a-form-item field="cmVotingLunPath" :label="$t('enterprise.ClusterConfig.test14')" validate-trigger="blur">
            <a-input v-model="data.form.cmVotingLunPath" :placeholder="$t('enterprise.ClusterConfig.test13')" />
          </a-form-item>
          <a-form-item field="databaseUsername" :label="$t('enterprise.ClusterConfig.5mpm3ku3j6k0')"
            validate-trigger="blur" v-if="installType === 'import'">
            <a-input v-model="data.form.databaseUsername" :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3j9s0')"
              allow-clear />
          </a-form-item>
          <a-form-item field="databasePassword" :label="$t('enterprise.ClusterConfig.5mpm3ku3jdg0')"
            validate-trigger="blur">
            <a-input-password v-model="data.form.databasePassword"
              :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3ji00')" allow-clear />
          </a-form-item>
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item field="isInstallCM" :label="$t('enterprise.NodeConfig.else3')">
                <a-switch @change="isInstallCMChange" v-model="data.form.isInstallCM" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item v-if="data.form.isInstallCM" field="enableDCF"
                :label="$t('enterprise.ClusterConfig.5mpm3ku3jkw0')">
                <a-checkbox v-model="data.form.enableDCF">
                </a-checkbox>
              </a-form-item>
            </a-col>
          </a-row>
          <a-form-item field="isEnvSeparate" :label="$t('simple.InstallConfig.else11')" validate-trigger="blur">
            <a-switch v-model="data.form.isEnvSeparate" />
          </a-form-item>
          <a-form-item v-if="data.form.isEnvSeparate" field="envPath" :label="$t('simple.InstallConfig.else9')"
            validate-trigger="blur">
            <a-input v-model="data.form.envPath" :placeholder="$t('simple.InstallConfig.else10')" />
          </a-form-item>
        </a-form>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>

import { computed, onMounted, reactive, ref } from 'vue'
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { useOpsStore } from '@/store'
import { DeployTypeEnum, EnterpriseInstallConfig } from '@/types/ops/install'
import { hasName } from '@/api/ops'
import { useI18n } from 'vue-i18n'
import { Select, Option } from '@arco-design/web-vue'
const { t } = useI18n()
const installStore = useOpsStore()

const data: {
  form: KeyValue,
  rules: KeyValue
} = reactive({
  form: {
    clusterId: '',
    clusterName: '',
    // databaseKernelArch: DatabaseKernelArch.SHARING_STORAGE,
    installPath: '/opt/openGauss/install/app',
    installPackagePath: '/opt/software/openGauss',
    logPath: '/opt/openGauss/log/omm',
    tmpPath: '/opt/openGauss/tmp',
    omToolsPath: '/opt/openGauss/install/om',
    corePath: '/opt/openGauss/corefile',
    port: '5432',
    enableDCF: false,
    databaseUsername: '',
    databasePassword: '',
    isInstallCM: false,
    isEnvSeparate: false,
    envPath: '',
    dssHome: '',
    dssVgName: 'data',
    dssDataLunPath: '/dev/sda',
    xlogVgName: 'xlog0',
    xlogLunPath: '/dev/sdb',
    cmSharingLunPath: '/dev/sdc',
    cmVotingLunPath: '/dev/sdd'
  },
  rules: {}
})

onMounted(() => {
  initData()
  if (Object.keys(installStore.getEnterpriseConfig).length) {
    Object.assign(data.form, installStore.getEnterpriseConfig)
  }
})

const initData = () => {
  data.rules = {
    clusterId: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.5mpm3ku3hik0') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.else2'))
              resolve(false)
            }
            const param = {
              name: value
            }
            hasName(param).then((res: KeyValue) => {
              if (Number(res.code) === 200) {
                if (res.data.has === 'Y') {
                  cb(t('enterprise.ClusterConfig.5mpm3ku3jo00'))
                  resolve(false)
                } else {
                  resolve(true)
                }
              } else {
                cb(t('enterprise.ClusterConfig.5mpm3ku3jqo0'))
                resolve(false)
              }
            })
          })
        }
      }
    ],
    installPath: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.5mpm3ku3i340') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.else2'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    installPackagePath: [
      { required: true, 'validate-trigger': 'blur', message: t('simple.InstallConfig.else7') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.else2'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    logPath: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.5mpm3ku3iag0') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.else2'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    tmpPath: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.5mpm3ku3ihg0') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.else2'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    omToolsPath: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.5mpm3ku3iok0') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.else2'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    corePath: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.5mpm3ku3ivk0') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.else2'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    port: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.5mpm3ku3j300') }
    ],
    databaseUsername: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.5mpm3ku3j9s0') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.else2'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    databasePassword: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.5mpm3ku3ji00') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            const reg = /^(?![\da-z]+$)(?![\dA-Z]+$)(?![\d~!@#$%^&*()_=+\|{};:,<.>/?]+$)(?![a-zA-Z]+$)(?![a-z~!@#$%^&*()_=+\|{};:,<.>/?]+$)(?![A-Z~!@#$%^&*()_=+\|{};:,<.>/?]+$)[\da-zA-z~!@#$%^&*()_=+\|{};:,<.>/?]{8,}$/
            const re = new RegExp(reg)
            if (re.test(value)) {
              resolve(true)
            } else {
              cb(t('simple.InstallConfig.else5'))
              resolve(false)
            }
          })
        }
      }
    ],
    envPath: [
      { required: true, 'validate-trigger': 'blur', message: t('simple.InstallConfig.else10') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.else2'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    dssHome: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.test1') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.error1'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    dssVgName: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.test3') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.error2'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    dssDataLunPath: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.test5') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.error3'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    xlogVgName: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.test7') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.error4'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    xlogLunPath: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.test9') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.error5'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    cmSharingLunPath: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.test11') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.error6'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    cmVotingLunPath: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.test13') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.error7'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ]
  }
}

const isInstallCMChange = (val: boolean) => {
  if (!val) {
    data.form.enableDCF = false
  }
}

const formRef = ref<FormInstance>()

const saveStore = () => {
  const param = JSON.parse(JSON.stringify(data.form))
  installStore.setInstallContext({
    clusterId: param.clusterId,
    clusterName: param.clusterName,
    deployType: DeployTypeEnum.CLUSTER
  })
  param.nodeConfigList = []
  installStore.setEnterpriseConfig(param as EnterpriseInstallConfig)
}

const beforeConfirm = async (): Promise<boolean> => {
  const validRes = await formRef.value?.validate()
  if (!validRes) {
    const param = JSON.parse(JSON.stringify(data.form))
    installStore.setInstallContext({
      clusterId: param.clusterId,
      clusterName: param.clusterName,
      deployType: DeployTypeEnum.CLUSTER,
      envPath: param.envPath
    })
    console.log('get envPath', installStore.getInstallConfig)
    if (Object.keys(installStore.getEnterpriseConfig).length) {
      if (param.isInstallCM !== installStore.getEnterpriseConfig.isInstallCM) {
        param.nodeConfigList = []
      }
    } else {
      param.nodeConfigList = []
    }
    installStore.setEnterpriseConfig(param as EnterpriseInstallConfig)
    return true
  } else {
    return false
  }
}

defineExpose({
  beforeConfirm,
  saveStore
})

const installType = computed(() => installStore.getInstallConfig.installType)

</script>

<style lang="less" scoped>
.cluster-config-c {
  padding: 20px;
  height: 100%;
  overflow-y: auto;

  .node-top {
    display: flex;
    background-color: #f2f3f5;
    border-radius: 8px;
    padding: 8px 12px;
  }
}
</style>
