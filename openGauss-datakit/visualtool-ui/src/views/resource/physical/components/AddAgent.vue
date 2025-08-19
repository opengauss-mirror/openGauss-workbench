<template>
  <el-dialog
    v-model="data.show"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :title="data.title"
    :width="'650px'"
    @close="close"
    :z-index="1000"
  >
    <template #footer>
      <span class="flex-center">
        <el-button :loading="data.loading" type="primary" @click="submit">{{
            $t('components.AddHost.5mphy3snx7c0')
          }}</el-button>
        <el-button class="mr" @click="close">{{
            $t('components.AddHost.5mphy3snwxs0')
          }}</el-button>
      </span>
    </template>

    <el-form ref="formRef" :model="data.formData" :rules="formAgentRules" label-position="left" label-width="150px">
      <el-form-item :label="$t('components.AddAgent.agentName')" prop="agentName">
        <el-input v-model="data.formData.agentName" :placeholder="$t('components.AddAgent.namePlaceholder')"
                  style="width: 380px"/>
      </el-form-item>
      <el-form-item :label="$t('components.AddAgent.installPath')" prop="installPath">
        <el-input v-model="data.formData.installPath" :placeholder="$t('components.AddAgent.installPathPlaceholder')"
                  style="width: 380px"/>
      </el-form-item>
      <el-form-item v-if="!isNonstandaloneInstallation" :label="$t('components.AddHost.username')" prop="installUser">
        <el-select v-model="data.formData.installUser" :placeholder="$t('components.AddHost.usernamePlaceholder')"
                   style="width: 380px" @change="addHostuserId">
          <el-option v-for="item in hostUserList" :key="item" :label="item" :value="item"/>
        </el-select>
        <div class="refresh-con">
          <el-icon>
            <IconRefresh @click="getHostuserList"/>
          </el-icon>
          <el-button link type="primary" @click="handleAddUser('create')">{{
              $t('components.AddHostUser.5mphzt9peog0')
            }}</el-button>
        </div>
      </el-form-item>
      <el-form-item :label="$t('components.AddHost.5mphy3snxtc0')" prop="agentPort" >
        <el-input-number v-model.number="data.formData.agentPort" :max="65535" :min="0" class="inner-class"
                         :placeholder="$t('components.AddHost.5mphy3snxzk0')" @change="handlePortChange"
                         controls-position="right" style="width: 380px;"  :input-attrs="{ style: 'text-align: left' }" />
      </el-form-item>
    </el-form>

  </el-dialog>
  <add-host-user ref="addUserRef" @finish="getHostuserList"></add-host-user>
</template>

<script lang="ts" setup>
import {KeyValue} from '@/types/global'
import {FormInstance} from '@arco-design/web-vue/es/form'
import {nextTick, reactive, ref, toRaw, computed, onMounted} from 'vue'
import {addAgent, hostUserListAll} from '@/api/ops'
import AddHostUser from './AddHostUser.vue'
import {useI18n} from 'vue-i18n'
import showMessage from "@/hooks/showMessage"

const {t} = useI18n()
const data = reactive<KeyValue>({
  show: false,
  loading: false,
  title: t('components.AddHost.5mphy3snz5k0'),
  formData: {
    agentId: '',
    agentName: '',
    agentIp: '',
    agentPort: 10054,
    installPath: '',
    installUser: '',
    installUserId: ''
  }
})

const formAgentRules = computed(() => {
  return {
    agentName: [
      {required: true, trigger: 'blur', message: t('components.AddAgent.namePlaceholder')},
      {
        validator: (rule: any, value: any, cb: any) => {
          return new Promise(resolve => {
            if (value.trim().length === 0 && value.length > 0) {
              cb(t('components.AddAgent.namePlaceholder'))
              return resolve(false)
            }
            if (value.length < 100) {
              resolve(true)
            } else {
              cb(t('components.AddAgent.stringLengthOver'))
              resolve(false)
            }
          })
        },
      },
    ],
    installUser: [
      {required: true, trigger: 'change', message: t('components.AddAgent.userPlaceholder')}
    ],
    installPath: [
      {required: true, trigger: 'blur', message: t('components.AddAgent.installPathPlaceholder')},
      {
        validator: (rule: any, value: any, callback: any) => {
          if (value.length >= 255) {
            callback(new Error(t('components.AddAgent.stringLengthOver')))
            return
          }
          if (/[\u4e00-\u9fa5]/.test(value)) {
            callback(new Error(t('components.AddAgent.noChinesePath')))
            return
          }
          const isValidPath = (path: any) => {
            if (path === '/' || path === '~') return true
            if (path.startsWith('~/')) {
              return !path.endsWith('/') && !path.includes('//')
            }
            const pathRegex = /^(?!.*\/\/)(?!.*\/$)(\/|\.\/)?([\w-]+(\/[\w-]+)*)?$/
            return pathRegex.test(path)
          }
          const isDepthValid = (path: string) => {
            if (path === '/' || path === '~') return true
            let pathToCheck = path
            if (path.startsWith('~/')) {
              pathToCheck = path.substring(1)
            }
            if (pathToCheck.startsWith('./')) pathToCheck = pathToCheck.substring(2)
            if (pathToCheck.startsWith('../')) pathToCheck = pathToCheck.substring(3)
            const components = pathToCheck.split('/').filter(comp => comp !== '')
            return components.length <= 5
          }
          if (!isValidPath(value)) {
            callback(new Error(t('components.AddAgent.formaterror')))
            return
          }
          if (!isDepthValid(value)) {
            callback(new Error(t('components.AddAgent.pathDepthExceeded')))
            return
          }
          callback()
        },
        trigger: ['blur', 'change']
      }
    ],
    agentPort: [
      { required: true, trigger: 'blur', message: t('components.AddHost.5mphy3snxzk0') },
      {
        validator: (rule: any, value: any, cb: any) => {
          return new Promise(resolve => {
            if (value <= 65535 && value >0 ) {
              resolve(true)
            } else {
              cb(t('components.AddAgent.numberRangeOver'))
              resolve(false)
            }
          })
        },
      },
    ]
  }
})
const handlePortChange = () => {
  formRef.value?.validateField('agentPort')
}
const emits = defineEmits([`finish`])
const formRef = ref<null | FormInstance>(null)
const submit = () => {
  formRef.value?.validate().then(result => {
    if (result) {
      data.loading = true
      const agentData = {
        agentId: data.formData.agentId,
        agentIp: data.formData.agentIp,
        agentName: data.formData.agentName,
        agentPort: data.formData.agentPort,
        installPath: data.formData.installPath,
        installUser: data.formData.installUser,
        installUserId: data.formData.installUserId
      }
      addAgent(agentData).then((res: KeyValue) => {
        if (Number(res.code) === 200) {
          showMessage('success', t('components.AddAgent.insAgentSuc'))
          emits(`finish`)
          data.show = false
        }
      }).catch((error) => {
        console.log(error)
        showMessage('error', error)
      }).finally(() => {
        data.loading = false
      })


    }
  }).catch((error) => {
    showMessage('error', error)
    console.log(error)
  })
}
const close = () => {
  data.show = false
  nextTick(() => {
    formRef.value?.clearValidate()
    formRef.value?.resetFields()
  })
}

const addHostuserId = () => {
  data.formData.installUserId = hostUserId.value[data.formData.installUser]
}
const addUserRef = ref<null | InstanceType<typeof AddHostUser>>(null)

const handleAddUser = (type: string) => {
  const hostData = {
    hostId: data.formData.agentId,
    privateIp: data.formData.privateIp,
    publicIp: data.formData.publicIp,
    port: data.formData.port
  }
  addUserRef.value?.open(type, hostData)
  setTimeout(() => {
    const dialogs = document.querySelectorAll('.el-dialog__wrapper')
    if (dialogs.length > 0) {
      dialogs[dialogs.length - 1].style.zIndex = '1002'
    }
  }, 100)
}

const hostUserList = ref([])
const hostUserId = ref({})
const getHostuserList = () => {
  if (!isNonstandaloneInstallation.value) {
    hostUserListAll(data.formData.agentId).then((res: any) => {
      if (Number(res.code) === 200) {
        hostUserId.value.length = 0
        hostUserList.value = []
        res.data.forEach((item: any) => {
          hostUserId.value[item.username] = item.hostUserId
          hostUserList.value.push(item.username)
        })
      }
    }).catch((error) => {
      showMessage('error', error)
      console.log(error)
    })
  }
}
const isNonstandaloneInstallation = ref(true)

const open = (hostData: KeyValue) => {
  data.show = true
  data.loading = false
  isNonstandaloneInstallation.value = true
  isNonstandaloneInstallation.value = false
  data.title = t('components.AddAgent.agentTitle')
  Object.assign(data.formData, {
    agentId: hostData.hostId,
    agentName: '',
    agentIp: hostData.publicIp,
    agentPort: 10054,
    installPath: '',
    installUser: '',
    installUserId: '',
    privateIp: hostData.privateIp,
    publicIp: hostData.publicIp,
    port: hostData.port
  })
  getHostuserList()
  data.show = true
}

defineExpose({
  open
})

</script>

<style scoped>
.inner-class :deep(.el-input__inner) {
  text-align: left !important;
  padding-right: 30px;
}
</style>
