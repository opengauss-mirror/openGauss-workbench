<template>
  <div class="cluster-form-c">
    <a-form
      :model="data"
      :rules="formRules"
      auto-label-width
      ref="formRef"
    >
      <a-form-item
        field="clusterId"
        :label="$t('enterprise.ClusterConfig.5mpm3ku3hcg0')"
        validate-trigger="blur"
      >
        <a-input
          v-model.trim="data.clusterId"
          :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3hik0')"
        />
      </a-form-item>
      <a-form-item
            field="databaseKernelArch"
            :label="$t('enterprise.ClusterConfig.test0')"
            validate-trigger="change"
          >
            <a-select
              v-model="data.databaseKernelArch"
              :placeholder="$t('enterprise.ClusterConfig.test00')"
            >
              <a-option value="MASTER_SLAVE">{{$t('enterprise.ClusterConfig.option1')}}</a-option>
              <a-option value="SHARING_STORAGE">{{$t('enterprise.ClusterConfig.option2')}}</a-option>
            </a-select>
      </a-form-item>
      <a-form-item
        field="installPath"
        :label="$t('enterprise.ClusterConfig.5mpm3ku3hv40')"
        validate-trigger="blur"
      >
        <div class="flex-col-start full-w">
          <div class="mb-s full-w">
            <a-input
              v-model.trim="data.installPath"
              :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3i340')"
            />
          </div>
          <div class="label-color">{{ $t('simple.InstallConfig.else12') }}</div>
        </div>
      </a-form-item>
      <a-form-item
        field="installPackagePath"
        :label="$t('simple.InstallConfig.else6')"
        validate-trigger="blur"
      >
        <div class="flex-col-start full-w">
          <div class="mb-s full-w">
            <a-input
              v-model.trim="data.installPackagePath"
              :placeholder="$t('simple.InstallConfig.else7')"
            />
          </div>
          <div class="label-color">{{ $t('lightweight.InstallConfig.else2') }}</div>
        </div>
      </a-form-item>
      <a-form-item
        field="port"
        :label="$t('enterprise.ClusterConfig.5mpm3ku3iz80')"
        validate-trigger="blur"
      >
        <a-input-number
          v-model="data.port"
          :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3j300')"
          :min="0"
          :max="65535"
        />
      </a-form-item>
      <a-form-item
        field="databaseUsername"
        :label="$t('enterprise.ClusterConfig.5mpm3ku3j6k0')"
        validate-trigger="blur"
        v-if="installType === 'import'"
      >
        <a-input
          v-model.trim="data.databaseUsername"
          :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3j9s0')"
          allow-clear
        />
      </a-form-item>
      <a-form-item
        field="databasePassword"
        :label="$t('enterprise.ClusterConfig.5mpm3ku3jdg0')"
        validate-trigger="blur"
      >
        <a-input-password
          v-model="data.databasePassword"
          :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3ji00')"
          allow-clear
        />
      </a-form-item>
      <a-row :gutter="16">
        <a-col :span="8">
          <a-form-item
            field="isInstallCM"
            :label="$t('enterprise.NodeConfig.else3')"
          >
            <a-switch
              @change="isInstallCMChange"
              v-model="data.isInstallCM"
            />
          </a-form-item>
        </a-col>
        <a-col :span="8">
          <a-form-item
            v-if="data.isInstallCM && data.databaseKernelArch === DatabaseKernelArch.MASTER_SLAVE"
            field="enableDCF"
            :label="$t('enterprise.ClusterConfig.5mpm3ku3jkw0')"
          >
            <a-switch
              @change="isEnableDcfChange"
              v-model="data.enableDCF"
            />
          </a-form-item>
        </a-col>
        <a-col :span="8">
          <a-form-item
            v-if="data.isInstallCM && data.enableDCF && data.databaseKernelArch === DatabaseKernelArch.MASTER_SLAVE"
            field="dcfPort"
            :label="$t('enterprise.ClusterConfig.5mpm3ku3jmn0')"
          >
            <a-input-number
              v-model="data.dcfPort"
              :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3j300')"
              :min="1"
              :max="65535"
            />
          </a-form-item>
        </a-col>
      </a-row>
      <a-form-item
        field="isEnvSeparate"
        :label="$t('simple.InstallConfig.else11')"
        validate-trigger="blur"
      >
        <a-switch
          v-model="data.isEnvSeparate"
          @change="isEnvSeparateChange"
          style="margin-right: 20px;"
        />
        <div v-if="!data.isEnvSeparate" class="label-color">{{ $t('simple.InstallConfig.else14') }}</div>
      </a-form-item>
      <a-form-item
        v-if="data.isEnvSeparate"
        field="envPath"
        :label="$t('simple.InstallConfig.else9')"
        validate-trigger="blur"
      >
        <a-input
          v-model.trim="data.envPath"
          :placeholder="$t('simple.InstallConfig.else10')"
        />
      </a-form-item>
      <a-collapse :bordered="false">
        <a-collapse-item
          :header="$t('components.openLooKeng.5mpiji1qpcc52')"
          key="1"
        >
          <a-form-item
            field="logPath"
            :label="$t('enterprise.ClusterConfig.5mpm3ku3i6s0')"
            validate-trigger="blur"
          >
            <a-input
              v-model.trim="data.logPath"
              :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3iag0')"
            />
          </a-form-item>
          <a-form-item
            field="tmpPath"
            :label="$t('enterprise.ClusterConfig.5mpm3ku3ie40')"
            validate-trigger="blur"
          >
            <a-input
              v-model.trim="data.tmpPath"
              :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3ihg0')"
            />
          </a-form-item>
          <a-form-item
            field="omToolsPath"
            :label="$t('enterprise.ClusterConfig.5mpm3ku3il80')"
            validate-trigger="blur"
          >
            <a-input
              v-model.trim="data.omToolsPath"
              :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3iok0')"
            />
          </a-form-item>
          <a-form-item
            field="corePath"
            :label="$t('enterprise.ClusterConfig.5mpm3ku3is00')"
            validate-trigger="blur"
          >
            <a-input
              v-model.trim="data.corePath"
              :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3ivk0')"
            />
          </a-form-item>
        </a-collapse-item>
      </a-collapse>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { useOpsStore } from '@/store'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { PropType, ref, computed, defineProps, watch } from 'vue'
import { hasName } from '@/api/ops'
import { useI18n } from 'vue-i18n'
import { onMounted } from 'vue'
import { reactive } from 'vue'
import { Message } from '@arco-design/web-vue'
import { DatabaseKernelArch, ConnectTypeEnum } from '@/types/ops/install'
import { ILLEGAL_REGEXP, LINUX_PATH } from '../constant'
const { t } = useI18n()

const installStore = useOpsStore()

const props = defineProps({
  formData: {
    type: Object as PropType<KeyValue>,
    required: true
  }
})

const installType = computed(() => installStore.getInstallConfig.installType)

const emits = defineEmits([`update:formData`, `isEnvSeparateChange`])
const data = computed({
  get: () => props.formData,
  set: (val) => {
    emits(`update:formData`, val)
  }
})

const formRules = computed(() => {
  function validatePort(value: any, cb: any) {
    return new Promise(resolve => {
      // 校验数据库端口
      if (value < 1024 || value > 65529) {
        cb(t('enterprise.ClusterConfig.5mpm3ku3jux0'))
        resolve(false)
      } else {
        resolve(true)
      }
    })
  }

  return {
    clusterId: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.5mpm3ku3hik0') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.else2'))
              resolve(false)
            }

            if (value.length > 20) {
              console.log("clusterId length = ", value.length)
              cb(t('enterprise.ClusterConfig.5mpm3ku3jro0'))
              resolve(false)
              return
            }

            if (ILLEGAL_REGEXP.test(value)) {
              cb(t('enterprise.ClusterConfig.5mpm3ku3juw0'))
              resolve(false)
              return
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
    databaseKernelArch: [
      { required: true, 'validate-trigger': 'change', message: t('enterprise.ClusterConfig.test00') }
    ],
    installPath: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.5mpm3ku3i340') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            const otherPaths = [
              data.value.logPath,
              data.value.tmpPath,
              data.value.omToolsPath,
              data.value.corePath,
              data.value.envPath
            ].filter(path => path && path.trim() !== '');

            const isParentOfOther = otherPaths.some(otherPath =>
              otherPath.startsWith(value + '/') || otherPath === value
            )
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.else2'))
              resolve(false)
            } else if (!LINUX_PATH.test(value)) {
              cb(t('enterprise.ClusterConfig.5mpm3ku3jvx0'))
              resolve(false)
            }  else if (isParentOfOther ) {
              cb(t('enterprise.ClusterConfig.5mpm3ku3jvx6'))
              resolve(false)
            } else if (data.value.installPackagePath === value) {
              cb(t('enterprise.ClusterConfig.5mpm3ku3jvx5'))
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
            } else if (!LINUX_PATH.test(value)) {
              cb(t('enterprise.ClusterConfig.5mpm3ku3jvx0'))
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
            } else if (!LINUX_PATH.test(value)) {
              cb(t('enterprise.ClusterConfig.5mpm3ku3jvx0'))
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
            } else if (!LINUX_PATH.test(value)) {
              cb(t('enterprise.ClusterConfig.5mpm3ku3jvx0'))
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
            } else if (!LINUX_PATH.test(value)) {
              cb(t('enterprise.ClusterConfig.5mpm3ku3jvx0'))
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
            } else if (!LINUX_PATH.test(value)) {
              cb(t('enterprise.ClusterConfig.5mpm3ku3jvx0'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    port: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.5mpm3ku3j300') },
      {
        validator: (value: any, cb: any) => {
          return validatePort(value, cb);
        }
      }
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
    dcfPort: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.5mpm3ku3jnl0')},
      {
        validator: (value: any, cb: any) => {
          return validatePort(value, cb);
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
            } else if (!LINUX_PATH.test(value)) {
              cb(t('enterprise.ClusterConfig.5mpm3ku3jvx0'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    "sharingStorageInstallConfig.dssHome": [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.test1') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.test1'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    "sharingStorageInstallConfig.dssVgName": [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.test3') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.test3'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    'sharingStorageInstallConfig.dssDataLunPath': [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.test5') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.test5'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    'sharingStorageInstallConfig.xlogVgName': [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.test7') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.test7'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    'sharingStorageInstallConfig.xlogLunPath': [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.test9') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.test9'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    'sharingStorageInstallConfig.cmSharingLunPath': [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.test11') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.test11'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    'sharingStorageInstallConfig.cmVotingLunPath': [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.test13') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.test13'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    'sharingStorageInstallConfig.interconnectType': [
      { required: true, 'validate-trigger': 'change', message: t('enterprise.ClusterConfig.test13') },
    ],
    'sharingStorageInstallConfig.rdmaConfig': [
      { required: function() {
          return data.sharingStorageInstallConfig.interconnectType === 'RDMA'
        }, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.test17')
      }
    ],
  }
})

// methods

const isInstallCMChange = (val: boolean) => {
  if (!val) {
    data.value.enableDCF = false
    data.value.dcfPort = 17783
  }
}

const isEnableDcfChange = (val: boolean) => {
  if (!val) {
    data.value.dcfPort = 17783
  }
}

const isEnvSeparateChange = (val: any) => {
  emits('isEnvSeparateChange', val)
}

const formRef = ref<null | FormInstance>(null)
const formValidate = async (): Promise<KeyValue> => {
  const validRes = await formRef.value?.validate()
  const result = {
    res: !validRes
  }

  return result
}

const pathValidate = (fieldName:string, message: string) => {
  formRef.value?.setFields({
    [fieldName]: {
      status: 'error',
      message: message
    }
  })
}



defineExpose({
  formValidate,
  pathValidate
})

</script>
<style lang="less" scoped>
.cluster-form-c {
  padding: 20px;
}</style>
