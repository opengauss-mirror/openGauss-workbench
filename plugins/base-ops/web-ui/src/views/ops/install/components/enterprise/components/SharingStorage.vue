<template>
  <div class="sharingStorage-form-c">
    <a-form
      :model="form"
      :rules="formRules"
      auto-label-width
      ref="formRef"
    >
      <a-form-item
        field="dssHome"
        :label="$t('enterprise.ClusterConfig.test2')"
        validate-trigger="blur"
      >
        <a-input
          v-model.trim="form.dssHome"
          :placeholder="$t('enterprise.ClusterConfig.test1')"
        />
      </a-form-item>
      <a-form-item
        field="dssVgName"
        :label="$t('enterprise.ClusterConfig.test4')"
        validate-trigger="blur"
      >
        <a-input
          v-model.trim="form.dssVgName"
          :placeholder="$t('enterprise.ClusterConfig.test3')"
        />
      </a-form-item>
      <a-form-item
        field="dssDataLunPath"
        :label="$t('enterprise.ClusterConfig.test6')"
        validate-trigger="blur"
      >
        <a-select
          v-model="form.dssDataLunPath"
          :placeholder="$t('enterprise.ClusterConfig.test5')"
          class="mr-s"
          show-search
        >
          <a-option
            v-for="(item, index) in data.lunPathList"
            :key="index"
            :value="item"
          >{{ item }}</a-option>
        </a-select>
      </a-form-item>
      <a-form-item
        field="xlogVgName"
        :label="$t('enterprise.ClusterConfig.test8')"
        validate-trigger="blur"
      >
      <div class="flex-col-start full-w">
      <div class="mb-s full-w">
        <a-input
          v-model.trim="form.xlogVgName"
          :placeholder="$t('enterprise.ClusterConfig.test7')"
        />
      </div>
      <div class="label-color">{{ $t('enterprise.ClusterConfig.test7') }}</div>
    </div>       
      </a-form-item>
      <a-form-item
        field="xlogLunPath"
        :label="$t('enterprise.ClusterConfig.test10')"

      >
      <div class="flex-col-start full-w">
      <div class="mb-s full-w">
        <a-select
          v-model="form.xlogLunPath"
          :placeholder="$t('enterprise.ClusterConfig.test9')"
          show-search
          multiple
          allow-clear
        >
          <a-option
            v-for="(item, index) in data.lunPathList"
            :key="index"
            :value="item"
          >{{ item }}</a-option>
        </a-select>
      </div>
      <div class="label-color">{{ $t('enterprise.ClusterConfig.test9') }}</div>
    </div>
    </a-form-item>
      <a-form-item
        field="cmSharingLunPath"
        :label="$t('enterprise.ClusterConfig.test12')"
        validate-trigger="blur"
      >
      <a-select
          v-model="form.cmSharingLunPath"
          :placeholder="$t('enterprise.ClusterConfig.test11')"
          class="mr-s"
          show-search
        >
          <a-option
            v-for="(item, index) in data.lunPathList"
            :key="index"
            :value="item"
          >{{ item }}</a-option>
        </a-select>
      </a-form-item>
      <a-form-item
        field="cmVotingLunPath"
        :label="$t('enterprise.ClusterConfig.test14')"
        validate-trigger="blur"
      >
            <a-select
          v-model="form.cmVotingLunPath"
          :placeholder="$t('enterprise.ClusterConfig.test13')"
          class="mr-s"
          show-search
        >
          <a-option
            v-for="(item, index) in data.lunPathList"
            :key="index"
            :value="item"
          >{{ item }}</a-option>
        </a-select>
      </a-form-item>
      <a-form-item
      field="interconnectType"
      :label="$t('enterprise.ClusterConfig.test16')"
      validate-trigger="change"
    >
      <a-select
        v-model="form.interconnectType"
        :placeholder="$t('enterprise.ClusterConfig.test15')"
      >
        <a-option
          v-for="(value, key) in ConnectTypeEnum"
          :key="key"
          :value="value"
        >
        {{ key }}
        </a-option>
      </a-select>
      </a-form-item>
      <template v-if="form.interconnectType === ConnectTypeEnum.RDMA">
      <a-form-item
        field="rdmaConfig"
        :label="$t('enterprise.ClusterConfig.test18')"
        validate-trigger="blur"
      >
        <div class="flex-col-start full-w">
      <div class="mb-s full-w">
        <a-input
            v-model.trim="form.rdmaConfig"
            :placeholder="$t('enterprise.ClusterConfig.test17')"
          />
      </div>
      <div class="label-color">{{ $t('simple.InstallConfig.else13') }}</div>
    </div>
      </a-form-item>
      </template>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { useOpsStore } from '@/store'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { PropType, ref, computed, defineProps, inject, reactive, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { hostListAll, hostUserListWithoutRoot, portUsed, pathEmpty, fileExist, multiPathQuery, hostPingById } from '@/api/ops'
import HostTerminal from "@/views/ops/install/components/hostTerminal/HostTerminal.vue";
import { encryptPassword } from '@/utils/jsencrypt'
import { useI18n } from 'vue-i18n'
import { ClusterRoleEnum, DatabaseKernelArch, ConnectTypeEnum } from '@/types/ops/install'
import { watch } from 'vue'
import { isTemplateElement } from '@babel/types'
import { ILLEGAL_REGEXP, LINUX_PATH } from '../constant'
const { t } = useI18n()


const props = defineProps({
  formData: {
    type: Object as PropType<KeyValue>,
    required: true
  },
  clusterData: {
    type: Object as PropType<KeyValue>,
    default: true
  }
})

const data = reactive<KeyValue>({
  lunListLoading: false,
  lunPathList: []
})

watch(() => props.clusterData.nodes[0].hostId, (newValue) => {
  if (newValue) {
    getLunList()
  }
});


onMounted(() => {
  getLunList()
})

const getLunList = () => {
  if (props.clusterData.nodes.length > 0) {
    data.lunListLoading = true
    
    multiPathQuery(props.clusterData.nodes[0].hostId, props.clusterData.nodes[0].rootPassword).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        let result = res.data
        data.lunPathList = res.data
      } else {
        Message.error('Failed to obtain lun list data')
      }
    }).finally(() => {
      data.lunListLoading = false
    })
  }

}

const emits = defineEmits([`update:formData`])
const form = computed({
  get: () => props.formData,
  set: (val) => {
    emits(`update:formData`, val)
  }
})

const formRules = computed(() => {
  return {
    "dssHome": [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.test1') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.test1'))
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
    "dssVgName": [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.test3') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.test3'))
              resolve(false)
            } 
            
            if (value.length > 20) {
              cb(t('enterprise.ClusterConfig.5mpm3ku3jro0'))
              resolve(false)
            }

            
            if (ILLEGAL_REGEXP.test(value)) {
              cb(t('enterprise.ClusterConfig.5mpm3ku3juw0'))
              resolve(false)
            }

            const numberRegExp = /^\d+$/;
            if (numberRegExp.test(value)) {
              cb(t('enterprise.ClusterConfig.5mpm3ku3juw0'))
              resolve(false)
            }

            resolve(true)
          })
        }
      }
    ],
    'dssDataLunPath': [
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
    'xlogVgName': [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.test7') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.test7'))
              resolve(false)
            } 

            if (value.length > 20) {
              cb(t('enterprise.ClusterConfig.5mpm3ku3jro0'))
              resolve(false)
            }

            const values = value.split(',');

            for (const item of values) {
              if (ILLEGAL_REGEXP.test(item.trim())) {
                cb(t('enterprise.ClusterConfig.5mpm3ku3juw0'))
                resolve(false)
                return;
              }

              const numberRegExp = /^\d+$/;
              if (numberRegExp.test(item.trim())) {
                cb(t('enterprise.ClusterConfig.5mpm3ku3juw0'))
                resolve(false)
                return;
              }
            }
            

            resolve(true)
          })
        }
      }
    ],
    'xlogLunPath': [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.test9') },
    ],
    'cmSharingLunPath': [
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
    'cmVotingLunPath': [
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
    'interconnectType': [
      { required: true, 'validate-trigger': 'change', message: t('enterprise.ClusterConfig.test13') },
    ],
    'rdmaConfig': [
      { required: function() {
          return form.interconnectType === 'RDMA'
        }, 'validate-trigger': 'blur', message: t('enterprise.ClusterConfig.test17')
      }
    ],
  }
})

const formRef = ref<null | FormInstance>(null)
const formValidate = async (): Promise<KeyValue> => {
  const validRes = await formRef.value?.validate()
  const result = {
    res: !validRes
  }
  return result
}

const validateLunPath = async () => {
  if (props.clusterData.databaseKernelArch === DatabaseKernelArch.MASTER_SLAVE) {
    return true;
  }

  let result = true
  if (props.formData.dssDataLunPath === props.formData.cmSharingLunPath) {
    formRef.value?.setFields({
      cmSharingLunPath: {
          status: 'error',
          message: t('enterprise.EnterpriseInstall.error8')
        }
      })
      result = false
  }

  if (props.formData.dssDataLunPath === props.formData.cmVotingLunPath) {
    formRef.value?.setFields({
      cmVotingLunPath: {
          status: 'error',
          message: t('enterprise.EnterpriseInstall.error8')
        }
      })
      result = false
  }

  if (props.formData.cmSharingLunPath === props.formData.cmVotingLunPath) {
    formRef.value?.setFields({
      cmVotingLunPath: {
          status: 'error',
          message: t('enterprise.EnterpriseInstall.error8')
        }
      })
      result = false
  }

  if (props.formData.dssHome === props.clusterData.cluster.installPath) {
    formRef.value?.setFields({
      dssHome: {
          status: 'error',
          message: t('enterprise.EnterpriseInstall.error13')
        }
      })
      result = false
  }

  props.formData.xlogLunPath.forEach((path, index) => {
    if (path === props.formData.dssDataLunPath || path === props.formData.cmSharingLunPath ||
        path === props.formData.cmVotingLunPath) {
          formRef.value?.setFields({
            xlogLunPath: {
              status: 'error',
              message: t('enterprise.EnterpriseInstall.error8')
            }
          })
          result = false
    }
  })

  let xlogVgNames = props.formData.xlogVgName.split(",");
  if (xlogVgNames.length !== props.formData.xlogLunPath.length) {
    formRef.value?.setFields({
      xlogVgName: {
          status: 'error',
          message: t('enterprise.EnterpriseInstall.error9')
        }
      })
      result = false
  }

  if ((xlogVgNames.length > 1 && xlogVgNames.length !== props.clusterData.nodes.length)) {
    formRef.value?.setFields({
      xlogVgName: {
          status: 'error',
          message: t('enterprise.EnterpriseInstall.error10')
        }
      })
      result = false
  }

  for (let i = 0; i < xlogVgNames.length; ++i) {
    // xlog卷名不能和dss卷名相同
    if (xlogVgNames[i] === props.formData.dssVgName) {
      formRef.value?.setFields({
      xlogVgName: {
          status: 'error',
          message: t('enterprise.EnterpriseInstall.error11')
        }
      })
      result = false
    }
  }

  if (xlogVgNames.length > 1) {
    for (let i = 1; i < xlogVgNames.length; ++i) {
    // xlog卷名之间不能相同
      if (xlogVgNames[i] === xlogVgNames[0]) {
        formRef.value?.setFields({
        xlogVgName: {
            status: 'error',
            message: t('enterprise.EnterpriseInstall.error12')
          }
        })
        result = false
      }
    }
  }
  return result
}

const lunValidate = async (): Promise<KeyValue> => {
  let validResult = false
  // lun path 
  validResult = await validateLunPath()
  const result = {
    res: validResult
  }
  return result
}

defineExpose({
  formValidate,
  lunValidate
})


</script>
<style lang="less" scoped>
.node-form-c {
  padding: 20px;
}
</style>
