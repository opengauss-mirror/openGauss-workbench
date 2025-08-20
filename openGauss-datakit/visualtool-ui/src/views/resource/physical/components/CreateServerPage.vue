<template>
  <div class="home-container">
    <el-container>
      <el-header class="page-header openDesignHeader">
        <el-page-header class="text-color-second" :title="t('physical.index.reback')" @back="backToIndex">
          <template #content>
            <span class="text-color font-600 mr-3">{{ t('physical.index.createServer') }}</span>
          </template>
        </el-page-header>
      </el-header>
      <el-main>
        <el-space direction="vertical" fill fill-ratio="100" size="15px" style="width: -webkit-fill-available;" wrap>
          <el-card class="box-card">
            <div class="card-header">
              <el-text tag="b">{{ t('physical.index.serverConfig') }}</el-text>
            </div>
            <div>
              <el-form ref="formServerRef" class="openDesignForm" :model="data.serverData" :rules="formServerRules"
                       label-position="left" label-width="150px">
                <el-form-item :label="$t('components.AddHost.name')" prop="name">
                  <el-input v-model.trim="data.serverData.name" :placeholder="$t('components.AddHost.namePlaceholder')"
                            class="input-width" maxlength="101" />
                </el-form-item>
                <el-form-item :label="$t('components.AddHost.ipAddress')" prop="privateIp" validate-trigger="blur">
                  <el-input v-model.trim="data.serverData.privateIp" :disabled="!isAdd"  maxlength="101"
                            :placeholder="$t('components.AddHost.5mphy3snxdo0')" class="input-width"/>
                </el-form-item>
                <el-form-item :label="$t('components.AddHost.5mphy3snxis0')" prop="publicIp" validate-trigger="blur">
                  <el-input v-model.trim="data.serverData.publicIp" :disabled="!isAdd"  maxlength="101"
                            :placeholder="$t('components.AddHost.5mphy3snxmw0')" class="input-width"
                            @blur="handleBlur"/>
                </el-form-item>
                <el-form-item :label="$t('components.AddHost.5mphy3snxtc0')" prop="port" validate-trigger="blur">
                  <el-input-number v-model="data.serverData.port" :max="65535" :min="0" :placeholder="$t('components.AddHost.5mphy3snxzk0')"
                                   class="input-width inner-class" controls-position="right"/>
                </el-form-item>
                <el-form-item v-if="isAdd" :label="$t('components.AddHost.username')" prop="username"
                              validate-trigger="blur">
                  <el-input v-model.trim="data.serverData.username"  maxlength="101"
                            :placeholder="$t('components.AddHost.usernamePlaceholder')" class="input-width"/>
                </el-form-item>
                <el-form-item v-if="isAdd" :label="$t('components.AddHost.5mphy3sny4w0')" prop="password"
                              validate-trigger="blur">
                  <el-input ref="formPwdRef" v-model="data.serverData.password"  maxlength="101"
                            :invisible-button="data.serverData.password !== data.emptyPwd" show-password
                            :placeholder="$t('components.AddHost.5mphy3snyao0')" class="input-width" clearable
                            type="password" @blur="passwordBlur"
                            @focus="passwordFocus"/>
                </el-form-item>
                <el-form-item :label="$t('components.AddHost.tags')" prop="tags">
                  <el-select v-model="data.serverData.tags" :loading="data.tagsLoading"
                             :placeholder="$t('components.AddHost.tagsPlaceholder')"
                             allow-create class="input-width" clearable multiple filterable
                             @change="tagsChange">
                    <el-option v-for="item in data.tagsList" :key="item.value" :value="item.value">{{
                        item.label
                      }}
                    </el-option>
                  </el-select>
                </el-form-item>
                <el-form-item :label="$t('components.AddHost.5mphy3snysg0')">
                  <el-input v-model.trim="data.serverData.remark" :placeholder="$t('components.AddHost.5mphy3snyxc0')"
                            class="textarea-width" type="textarea"  maxlength="255" />
                </el-form-item>
              </el-form>
            </div>
          </el-card>
          <el-card class="box-card">
            <div>
              <div class="card-header">
                <el-text tag="b">{{ t('physical.index.agentInstall') }}</el-text>
              </div>
              <el-form ref="formAgentRef" :model="data.agentData" :rules="formAgentRules" label-position="left"
                       label-width="150px">
                <el-form-item :label="t('physical.index.installAgent')">
                  <el-switch v-model="data.agentData.isAgent"/>
                </el-form-item>
                <div v-if="data.agentData.isAgent">
                  <el-form-item :label="$t('components.AddAgent.agentName')" prop="agentName">
                    <el-input v-model="data.agentData.agentName"  maxlength="101"
                              :placeholder="$t('components.AddAgent.namePlaceholder')" class="input-width"/>
                  </el-form-item>
                  <el-form-item :label="$t('components.AddAgent.installPath')" prop="installPath">
                    <el-input v-model.trim="data.agentData.installPath"
                              :placeholder="$t('components.AddAgent.installPathPlaceholder')" class="input-width"/>
                  </el-form-item>
                  <el-form-item :label="$t('components.AddHost.5mphy3snxtc0')" prop="agentPort">
                    <el-input-number v-model="data.agentData.agentPort" :max="65535" :min="0" :placeholder="$t('components.AddHost.5mphy3snxzk0')"
                                     class="input-width inner-class" controls-position="right"/>
                  </el-form-item>
                </div>
              </el-form>
            </div>
          </el-card>
        </el-space>
      </el-main>
    </el-container>
        <div class="openDesignFooterButterGap">
          <div class="footerButtonArea">
            <div v-if="data.status !== hostStatusEnum.unTest" class="label-color mr">
              {{ $t('components.AddHost.currentStatus') }}
            </div>
            <el-tag v-if="data.status === hostStatusEnum.success" type="success">
              {{ $t('components.AddHost.5mphy3snvg80') }}
            </el-tag>
            <el-tag v-if="data.status === hostStatusEnum.fail" type="danger">
              {{ $t('components.AddHost.5mphy3snwq40') }}
            </el-tag>
          </div>
          <div class="footerButtonArea">
            <el-button class="mr" @click="close">{{ $t('components.AddHost.5mphy3snwxs0') }}</el-button>
            <el-button v-if="isAdd" :loading="data.testLoading" class="mr" @click="handleTestHost">
              {{ $t('components.AddHost.5mphy3snx3o0') }}
            </el-button>
            <el-button :loading="data.loading" type="primary" @click="submit">
              {{ $t('components.AddHost.5mphy3snx7c0') }}
            </el-button>
          </div>
        </div>
  </div>
</template>

<script lang="ts" setup>
import { IpRegex, KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { computed, nextTick, onMounted, reactive, ref, toRaw } from 'vue'
import { addAgent, addHost, hostPage, hostPing, hostTagListAll, hostUserListAll, hostUserPage } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
import { encryptPassword } from '@/utils/jsencrypt'
import showMessage from '@/hooks/showMessage'
import { useRoute, useRouter } from 'vue-router'
import WujieVue from 'wujie-vue3'
const { bus } = WujieVue
const { t } = useI18n()

enum hostStatusEnum {
  unTest = -1,
  success = 1,
  fail = 0
}

const data = reactive<KeyValue>({
  show: false,
  title: t('components.AddHost.5mphy3snz5k0'),
  loading: false,
  testLoading: false,
  status: hostStatusEnum.unTest,
  tagsLoading: false,
  tagsList: [],
  oldPwd: '',
  emptyPwd: 'emptyPassword',
  serverData: {
    name: '',
    hostId: '',
    privateIp: '',
    publicIp: '',
    port: 22,
    password: '',
    username: '',
    isRemember: true,
    tags: [],
    remark: ''
  },
  agentData: {
    isAgent: false,
    agentId: '',
    agentName: '',
    agentIp: '',
    agentPort: 10054,
    installPath: '',
    installUser: '',
    installUserId: ''
  }
})

const formServerRules = computed(() => {
  return {
    name: [
      { required: true, trigger: ['change', 'blur'], message: t('components.AddHost.namePlaceholder') },
      {
        validator: (rule: any, value: any, cb: any) => {
          return new Promise(resolve => {
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
    privateIp: [
      { required: true, trigger: 'blur', message: t('components.AddHost.5mphy3snxdo0') },
      {
        validator: (rule: any, value: any, cb: any) => {
          console.log(value)
          return new Promise(resolve => {
            if (IpRegex.ipv4Reg.test(value) || IpRegex.ipv6Reg.test(value)) {
              resolve(true)
            } else {
              cb(t('database.JdbcInstance.5oxhtcboblw0'))
              resolve(false)
            }
          })
        }, trigger: 'blur'
      },
    ],
    publicIp: [
      { required: true, trigger: 'blur', message: t('components.AddHost.5mphy3snxmw0') },
      {
        validator: (rule: any, value: any, cb: any) => {
          return new Promise(resolve => {
            if (IpRegex.ipv4Reg.test(value) || IpRegex.ipv6Reg.test(value)) {
              resolve(true)
            } else {
              cb(t('database.JdbcInstance.5oxhtcboblw0'))
              resolve(false)
            }
          })
        }, trigger: 'blur'
      },
    ],
    port: [
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
    ],
    username: [
      { required: true, trigger: 'blur', message: t('components.AddHost.usernamePlaceholder') },
      {
        validator: (rule: any, value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('database.JdbcInstance.5oxhtcbobtc0'))
              resolve(false)
            } else {
              if (value.length > 100) {
                cb(t('components.AddAgent.stringLengthOver'))
                resolve(false)
              } else {
                resolve(true)
              }
            }
          })
        }
      }
    ],
    password: [
      { required: true, 'validate-trigger': 'blur', message: t('components.AddHost.5mphy3snyao0') },
      {
        validator: (rule: any, value: any, cb: any) => {
          return new Promise(resolve => {
            if (value.length < 100) {
              resolve(true)
            } else {
              cb(t('components.AddAgent.stringLengthOver'))
              resolve(false)
            }
          })
        },
      },
    ]
  }
})

const formAgentRules = computed(() => {
  return {
    agentName: [
      { required: data.agentData.isAgent, trigger: 'blur', message: t('components.AddHost.namePlaceholder') },
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
    installPath: [
      { required: data.agentData.isAgent, trigger: 'blur', message: t('components.AddHost.5mphy3snxdo0') },
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
      { required: data.agentData.isAgent, trigger: 'blur', message: t('components.AddHost.5mphy3snxzk0') },
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

const emits = defineEmits([`finish`])
const formServerRef = ref<null | FormInstance>(null)
const formAgentRef = ref<null | FormInstance>(null)

let agentInstallError = false
const submit = async () => {
  try {
    await formServerRef.value?.validate()
    await formAgentRef.value?.validate()
    data.loading = true
    encryptPassword(data.serverData.password).then(async (res) => {
      const param = Object.assign({}, data.serverData)
      param.password = res
      const finishServer = (showMessageInfo: any) => {
        if (showMessageInfo === '1') {
          showMessage('success', t('components.AddHost.addAgentSuc'))
        } else {
          showMessage('success', t('components.AddHost.addServerSuc'))
        }
        emits('finish')
        close()
      }
      const addAgentHandler = async () => {
        try {
          const agentData = {
            agentId: data.agentData.agentId,
            agentIp: data.agentData.agentIp,
            agentName: data.agentData.agentName,
            agentPort: data.agentData.agentPort,
            installPath: data.agentData.installPath,
            installUser: data.agentData.installUser,
            installUserId: data.agentData.installUserId
          }
          const res = await addAgent(agentData)
          if (Number(res.code) === 200) {
            agentInstallError = false
            finishServer('1')
          }
        } catch (error) {
          console.log(error)
          agentInstallError = true
        }
      }
      if (data.serverData.hostId === '' && !agentInstallError) {
        try {
          const res = await addHost(param)
          data.loading = false
          if (Number(res.code) !== 200) return
          if (!data.agentData.isAgent) {
            finishServer('2')
            return
          }
          data.agentData.installUser = data.serverData.username
          const hostRes = await hostPage({
            name: data.serverData.privateIp,
            tagIds: '',
            os: '',
            pageNum: 1,
            pageSize: 10
          })
          if (Number(hostRes.code) === 200) {
            data.agentData.agentId = hostRes.rows[0].hostId
            data.serverData.hostId = hostRes.rows[0].hostId
          }
          const userRes = await hostUserPage(data.agentData.agentId)
          data.agentData.installUserId = userRes.rows[0].hostUserId
          data.agentData.agentIp = data.serverData.privateIp
          await addAgentHandler()
        } catch (error) {
          console.log(error)
          showMessage('error', error)
          agentInstallError = true
        } finally {
          data.loading = false
        }
      } else {
        if (!data.agentData.isAgent) {
          finishServer('2')
        } else {
          await addAgentHandler()
        }
      }
    })
  } catch (error) {
    showMessage('error', t('components.AddHost.unFill'))
  }
}
const close = () => {
  agentInstallError = false
  data.show = false
  data.oldPwd = ''
  data.serverData.password = ''
  data.serverData.username = ''
  backToIndex()
  nextTick(() => {
    formServerRef.value?.clearValidate()
    formServerRef.value?.resetFields()
    formAgentRef.value?.clearValidate()
    formAgentRef.value?.resetFields()
  })
}

const tagsChange = (val: any) => {
  data.serverData.tags = val.filter((item: string) => {
    return item.trim() !== ''
  })
}

const passwordFocus = () => {
  if (data.serverData.password === data.emptyPwd) {
    data.serverData.password = ''
  }
}
const formPwdRef = ref()
const passwordBlur = () => {
  if (!data.serverData.password && data.oldPwd) {
    formPwdRef.value.invisible = true
    data.serverData.password = data.emptyPwd
  }
}

const handleTestHost = () => {
  formServerRef.value?.validate().then(async result => {
    if (result) {
      const {privateIp, publicIp, port, remark, username, hostId, password} = data.serverData
      data.testLoading = true
      let encryptPwd
      if (hostId && password === data.emptyPwd) {
        encryptPwd = data.oldPwd
      } else {
        encryptPwd = await encryptPassword(password)
      }
      const param = {}
      Object.assign(param, {
        privateIp,
        publicIp,
        port,
        password: encryptPwd,
        isRemember: true,
        remark,
        username
      })

      hostPing(toRaw(param)).then((res: KeyValue) => {
        if (Number(res.code) === 200) {
          data.status = hostStatusEnum.success
        } else {
          data.status = hostStatusEnum.fail
        }
      }).catch(() => {
        data.status = hostStatusEnum.fail
      }).finally(() => {
        data.testLoading = false
      })
    }
  }) .catch((error) => {
    console.log(error)
  })
}

const getAllTag = () => {
  data.tagsLoading = true
  hostTagListAll().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      data.tagsList = []
      res.data.forEach((item: KeyValue) => {
        const temp = {
          label: item.name,
          value: item.name
        }
        data.tagsList.push(temp)
      })
    }
  }).finally(() => {
    data.tagsLoading = false
  })
}

const isAdd = ref(true)
const open = () => {
  data.show = true
  getAllTag()
  data.status = hostStatusEnum.unTest
  data.loading = false
  isAdd.value = true
  data.title = t('components.AddHost.5mphy3snz5k0')
  Object.assign(data.serverData, {
    hostId: '',
    privateIp: '',
    publicIp: '',
    password: '',
    isRemember: true,
    port: 22,
    remark: ''
  })
  Object.assign(data.agentData, {
    agentId: '',
    agentName: '',
    agentIp: '',
    agentPort: 10054,
    installPath: '',
    installUser: '',
    installUserId: '',
    privateIp: '',
    publicIp: ''
  })
}

const handleBlur = () => {
  if (!data.serverData.name) {
    data.serverData.name = data.serverData.publicIp
  }
}

const route = useRoute()
const router = useRouter()
const backToIndex = () => {
  bus.$emit('opengauss-close-tab', route)
  router.push({
    name: 'ResourcePhysical'
  })
}

defineExpose({
  open
})
onMounted(() => {
  open()
})

</script>

<style lang="scss" scoped>

.input-width {
  width: 440px;
}

.textarea-width {
  width: 708px;
  height: 73px;
}

.home-container {
  position: relative;
  height: calc(100vh - 123px);
  padding-bottom: 24px;
  background: var(--o-bg-color-light);
  .el-container {
    height: 100%;
  }
  .el-main {
    padding: 16px 24px 20px;
    .card-header {
      .el-text {
        color: var(--o-text-color-primary);
      }
    }
  }

  .box-card {
    background-color: var(--o-bg-color-base);
    border: 1px solid var(--o-border-color-base);
  }

  .main-con {
    height: calc(100vh - 123px);
    padding-bottom: 40px;
    overflow-y: auto;
  }

  .submit-con {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 10;
    height: 45px;
    padding-left: 20px;
    padding-right: 20px;
    padding-top: 10px;

    background: var(--color-bg-2);
    border-top: 1px solid #e4e7ed;

    .btn-group {
      height: 100%;
      display: flex;
      justify-content: flex-end;
      align-items: center;
      gap: 20px;
    }

    .btn-item {
      margin-left: 10px;
      min-width: 80px;
    }
  }
}

.inner-class :deep(.el-input__inner) {
  text-align: left !important;
  padding-right: 30px;
}

</style>
