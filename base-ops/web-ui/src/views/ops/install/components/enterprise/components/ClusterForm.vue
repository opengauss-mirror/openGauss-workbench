<template>
  <div class="cluster-form-c">
    <a-form :model="data" :rules="formRules" auto-label-width ref="formRef">
      <a-form-item field="clusterId" :label="$t('enterprise.ClusterConfig.5mpm3ku3hcg0')" validate-trigger="blur">
        <a-input v-model.trim="data.clusterId" :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3hik0')" />
      </a-form-item>
      <a-form-item field="installPath" :label="$t('enterprise.ClusterConfig.5mpm3ku3hv40')" validate-trigger="blur">
        <a-input v-model.trim="data.installPath" :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3i340')" />
      </a-form-item>
      <a-form-item field="installPackagePath" :label="$t('simple.InstallConfig.else6')" validate-trigger="blur">
        <a-input v-model.trim="data.installPackagePath" :placeholder="$t('simple.InstallConfig.else7')" />
      </a-form-item>
      <a-form-item field="port" :label="$t('enterprise.ClusterConfig.5mpm3ku3iz80')" validate-trigger="blur">
        <a-input v-model.trim="data.port" :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3j300')" />
      </a-form-item>
      <a-form-item field="databaseUsername" :label="$t('enterprise.ClusterConfig.5mpm3ku3j6k0')" validate-trigger="blur"
        v-if="installType === 'import'">
        <a-input v-model.trim="data.databaseUsername" :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3j9s0')"
          allow-clear />
      </a-form-item>
      <a-form-item field="databasePassword" :label="$t('enterprise.ClusterConfig.5mpm3ku3jdg0')" validate-trigger="blur">
        <a-input-password v-model="data.databasePassword" :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3ji00')"
          allow-clear />
      </a-form-item>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item field="isInstallCM" :label="$t('enterprise.NodeConfig.else3')">
            <a-switch @change="isInstallCMChange" v-model="data.isInstallCM" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item v-if="data.isInstallCM" field="enableDCF" :label="$t('enterprise.ClusterConfig.5mpm3ku3jkw0')">
            <a-checkbox v-model="data.enableDCF">
            </a-checkbox>
          </a-form-item>
        </a-col>
      </a-row>
      <a-form-item field="isEnvSeparate" :label="$t('simple.InstallConfig.else11')" validate-trigger="blur">
        <a-switch v-model="data.isEnvSeparate" />
      </a-form-item>
      <a-form-item v-if="data.isEnvSeparate" field="envPath" :label="$t('simple.InstallConfig.else9')"
        validate-trigger="blur">
        <a-input v-model.trim="data.envPath" :placeholder="$t('simple.InstallConfig.else10')" />
      </a-form-item>
      <a-collapse :bordered="false">
        <a-collapse-item :header="$t('components.openLooKeng.5mpiji1qpcc52')" key="1">
          <a-form-item field="azId" :label="$t('enterprise.NodeConfig.5mpme7w6aj40')" validate-trigger="change">
            <a-select :loading="azData.azListLoading" v-model="data.azId"
              :placeholder="$t('enterprise.NodeConfig.5mpme7w6ap00')" @change="azChange"
              @popup-visible-change="azPopChange">
              <a-option v-for="item in azData.azList" :key="item.azId" :value="item.azId">{{
                item.name
              }}</a-option>
            </a-select>
          </a-form-item>
          <a-form-item field="logPath" :label="$t('enterprise.ClusterConfig.5mpm3ku3i6s0')" validate-trigger="blur">
            <a-input v-model.trim="data.logPath" :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3iag0')" />
          </a-form-item>
          <a-form-item field="tmpPath" :label="$t('enterprise.ClusterConfig.5mpm3ku3ie40')" validate-trigger="blur">
            <a-input v-model.trim="data.tmpPath" :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3ihg0')" />
          </a-form-item>
          <a-form-item field="omToolsPath" :label="$t('enterprise.ClusterConfig.5mpm3ku3il80')" validate-trigger="blur">
            <a-input v-model.trim="data.omToolsPath" :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3iok0')" />
          </a-form-item>
          <a-form-item field="corePath" :label="$t('enterprise.ClusterConfig.5mpm3ku3is00')" validate-trigger="blur">
            <a-input v-model.trim="data.corePath" :placeholder="$t('enterprise.ClusterConfig.5mpm3ku3ivk0')" />
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
import { hasName, azListAll } from '@/api/ops'
import { useI18n } from 'vue-i18n'
import { onMounted } from 'vue'
import { reactive } from 'vue'
import { Message } from '@arco-design/web-vue'
const { t } = useI18n()

const installStore = useOpsStore()

const props = defineProps({
  formData: {
    type: Object as PropType<KeyValue>,
    required: true
  }
})

const installType = computed(() => installStore.getInstallConfig.installType)

const emits = defineEmits([`update:formData`])
const data = computed({
  get: () => props.formData,
  set: (val) => {
    emits(`update:formData`, val)
  }
})

const azData = reactive<KeyValue>({
  azListLoading: false,
  azObj: {},
  azList: []
})

onMounted(() => {
  getAZList()
})

const formRules = computed(() => {
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
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.5mpm3ku3j300') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            const reg = /^([0-9]|[1-9]\d{1,3}|[1-5]\d{4}|6[0-4]\d{4}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/
            const re = new RegExp(reg)
            if (re.test(value)) {
              resolve(true)
            } else {
              cb(t('simple.InstallConfig.else2'))
              resolve(false)
            }
          })
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
    azId: [{ required: true, 'validate-trigger': 'change', message: t('enterprise.NodeConfig.5mpme7w6ap00') }]
  }
})

// methods

const getAZList = () => {
  azData.azListLoading = true
  azListAll().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      azData.azList = []
      azData.azList = res.data
      res.data.forEach((item: KeyValue) => {
        azData.azObj[item.azId] = item.name
      })
      data.value.azId = res.data[0].azId
      data.value.azName = res.data[0].name
    } else {
      Message.error('Failed to obtain the AZ list data')
    }
  }).finally(() => {
    azData.azListLoading = false
  })
}

const azPopChange = (val: any) => {
  if (val) {
    getAZList()
  }
}

const azChange = () => {
  if (data.value.azId) {
    if (azData.azObj[data.value.azId]) {
      data.value.azName = azData.azObj[data.value.azId]
    }
  }
}

const isInstallCMChange = (val: boolean) => {
  if (!val) {
    data.value.enableDCF = false
  }
}

const formRef = ref<null | FormInstance>(null)
const formValidate = async (): Promise<KeyValue> => {
  const validRes = await formRef.value?.validate()
  const result = {
    res: !validRes
  }
  return result
}

defineExpose({
  formValidate
})

</script>
<style lang="less" scoped>
.cluster-form-c {
  padding: 20px;
}
</style>