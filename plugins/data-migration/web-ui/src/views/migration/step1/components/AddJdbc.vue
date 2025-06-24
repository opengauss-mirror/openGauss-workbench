<template>
  <el-drawer v-model="visible" :before-close="handleClose" :width="'calc(50vw)'" class="custom-drawer" show-close="true">
    <template #title>
      <div class="title-con">
        <h4 class="params-title">{{ data.title }}</h4>
      </div>
    </template>
    <div class="config-con" id="mainForm">
      <div class="basic-config-con">
        <div class="container">
          <el-form :model="data.form" ref="formRef" auto-label-width :rules="formRules">
            <el-row :gutter="16">
              <el-col :span="18">
                <el-form-item v-if="!data.form.isCustomName" :label="$t('components.AddJdbc.5q0a7i43aw00')"
                              validate-trigger="blur">
                  <el-input v-model="clusterName" :placeholder="$t('components.AddJdbc.5q0a7i43bkk0')"
                            disabled></el-input>
                </el-form-item>
                <el-form-item v-else prop="name" :label="$t('components.AddJdbc.5q0a7i43aw00')"
                              validate-trigger="blur">
                  <el-input v-model="data.form.name" :placeholder="$t('components.AddJdbc.5q0a7i43boo0')"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item hide-label>
                  <el-checkbox v-model="data.form.isCustomName" @change="handleCustomChange(data.form.isCustomName)">
                    {{ $t('components.AddJdbc.5q0a7i43br80') }}
                  </el-checkbox>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item :label="$t('components.AddJdbc.5q0a7i43bto0')" validate-trigger="change">
              <el-radio-group v-model="data.form.dbType">
                <el-radio-button value="MYSQL">MySQL</el-radio-button>
                <el-radio-button value="POSTGRESQL" disabled>PostgreSQL</el-radio-button>
                <el-radio-button value="OPENGAUSS" disabled>openGauss</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-form>
          <div>
            <el-card v-for="(node, index) in data.form.nodes" :key="node.id" class="gray-card">
              <template #header>
                <div class="custom-card-header">
                  <h3 style=" margin-top: 0px; margin-bottom: 10px;">{{ $t('components.AddJdbc.5q0a7i43bzk0') }}
                    {{ index + 1 }}</h3>
                  <el-button
                    v-if="node.tabName > 1"
                    class="close-icon"
                    @click="handleDelete(node.id)"
                    type="text">
                    <el-icon class="close-icon">
                      <Close/>
                    </el-icon>
                  </el-button>
                </div>
              </template>
              <div class="card-content">
                <div class="jdbc-instance-c">
                  <jdbc-instance
                    :ref="(el) => setRefMap(el, node.id)"
                    :form-data="node"
                    :host-list="data.hostList" :jdbc-type="data.form.dbType" />
                </div>
              </div>
            </el-card>
          </div>
          <el-row>
            <el-button @click="handleAdd" type="text" class="add-button">
              <el-icon><CirclePlus/></el-icon>
              {{ $t('components.AddJdbc.5q0a7i43bzk0') }}
            </el-button>
          </el-row>
        </div>
      </div>
    </div>
    <template #footer>
      <div class="footer-con">
        <el-button class="mr" @click="close">{{ $t('components.AddJdbc.5q0a7i43amo0') }}</el-button>
        <el-button :loading="data.testLoading" class="mr" @click="handleTestHost">
          {{ $t('components.AddJdbc.5q0a7i43ap40') }}
        </el-button>
        <el-button :loading="data.loading" type="primary" @click="handleSubmit">{{ $t('components.AddJdbc.5q0a7i43as00') }}
        </el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import {ref, reactive, computed, nextTick} from 'vue'
import {useI18n} from 'vue-i18n'
import {hostListAll, addJdbc} from '@/api/task'
import JdbcInstance from "./JdbcInstance.vue"
import {CirclePlus, Close} from '@element-plus/icons-vue'
import {ElDrawer} from 'element-plus'
import {encryptPassword} from "@/utils/jsencrypt"
import showMessage from "@/utils/showMessage"

const {t} = useI18n()
const visible = ref(false)

const data = reactive({
  show: false,
  title: '',
  testLoading: false,
  loading: false,
  getHostLoading: false,
  form: {
    clusterId: '',
    name: '',
    isCustomName: false,
    dbType: 'MYSQL',
    nodes: [],
    status: -1
  },
  hostList: [],
  activeTab: '',
  dbTypes: [
    {label: 'MySQL', value: 'MYSQL'},
    {label: 'openGauss', value: 'OPENGAUSS'}
  ]
})

const formRef = ref(null)
const handleCustomChange = (val) => {
  formRef.value?.clearValidate()
  if (val) {
    data.form.name = clusterName.value
  }
}

const clusterName = computed(() => {
  let result = ''
  if (!data.form.isCustomName) {
    result = getNameByNode(data.form)
  } else {
    result = clusterName.value
  }
  return result
})

const getNameByNode = (data) => {
  let result = ''
  if (data.nodes.length) {
    data.nodes.forEach((item, index) => {
      if (index < 2 && item.ip && item.port) {
        result += `${item.ip}(${item.port})-`
      }
    })
    if (result) {
      result += data.nodes.length
    }
  }
  return result
}

const formRules = computed(() => {
  return {
    name: [
      {required: data.form.isCustomName, 'validate-trigger': 'blur', message: t('components.AddJdbc.5q0a7i43c280')},
      {
        validator: (value, cb) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('components.AddJdbc.5q0a7i43c4s0'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ]
  }
})

const refObj = ref({})
const setRefMap = (el, key) => {
  if (!refObj.value[key]) {
    refObj.value[key] = el
  }
}

const refList = computed(() => {
  const result = []
  const refs = Object.keys(refObj.value)
  if (refs.length) {
    for (let key in refObj.value) {
      if (refObj.value[key]) {
        result.push(refObj.value[key])
      }
    }
  }
  return result
})

const getHostList = () => {
  data.getHostLoading = true
  hostListAll().then((res) => {
    data.hostList = []
    if (Number(res.code) === 200) {
      if (res.data.length) {
        res.data.forEach((item) => {
          data.hostList.push({
            label: item.publicIp,
            value: item.publicIp
          })
        })
      }
    }
  }).finally(() => {
    data.getHostLoading = false
  })
}

const emits = defineEmits([`finish`])
const handleSubmit = (event) => {
  event.preventDefault()
  submit()
}

const submit = () => {
  const methodArr = []
  for (let i = 0; i < refList.value.length; i++) {
    if (refList.value[i]) {
      methodArr.push(refList.value[i].formValidate())
    }
  }
  methodArr.push(formRef.value?.validate())
  Promise.all(methodArr).then(res => {
    const validRes = res.filter((item) => {
      return item && item.res === false
    })
    if (validRes.length > 0) {
      data.activeTab = validRes[0].id
      return Promise.reject(new Error('表单验证失败'))
    }
    formRef.value.validate()
    data.loading = true
    const param = {
      clusterName: data.form.name,
      dbType: data.form.dbType,
      deployType: data.form.nodes.length > 1 ? 'CLUSTER' : 'SINGLE_NODE',
      nodes: []
    }
    if (!data.form.isCustomName) {
      param.clusterName = clusterName.value
    }
    data.form.nodes.forEach((item) => {
      const newItem = {...item}
      newItem.extendProps = JSON.stringify(newItem.props)
      param.nodes.push(newItem)
    })
    return Promise.all(
      param.nodes.map(item =>
        encryptPassword(item.password).then(encrypted => {
          item.password = encrypted
        })
      )
    ).then(() => param)
  }).then(param => {
    return addJdbc(param)
  }).then(res => {
    if (Number(res.code) === 200) {
      showMessage('success', 'Create success')
      emits('finish', data.form.dbType)
      handleClose()
    }
  }).catch(error => {
    console.error(error)
  }).finally(() => {
    data.loading = false
  })
}

const handleClose = (done) => {
  formRef.value?.clearValidate()
  formRef.value?.resetFields()
  visible.value = false
  data.form.nodes = []
  done()
}

const close = () => {
  visible.value = false
}

const handleTestHost = () => {
  const methodArr = []
  for (let i = 0; i < refList.value.length; i++) {
    if (refList.value[i]) {
      methodArr.push(refList.value[i].formValidate())
    }
  }
  Promise.all(methodArr).then((res) => {
    const validRes = res.filter((item) => {
      return item.res === false
    })
    if (validRes.length) {
      data.activeTab = validRes[0].id
      return
    }
    data.testLoading = true
    const methodTestArr = []
    for (let i = 0; i < refList.value.length; i++) {
      if (refList.value[i]) {
        methodTestArr.push(refList.value[i].handelTest())
      }
    }
    Promise.all(methodTestArr).then((testRes) => {
      const noPass = testRes.filter((item) => {
        return item.res === false
      })
      if (noPass.length) {
        data.activeTab = noPass[0].id
        data.form.status = 0
        return
      }
      data.form.status = 1
    }).finally(() => {
      data.testLoading = false
    })
  })
}

const handleAdd = () => {
  const id = new Date().getTime() + ''
  data.form.nodes.push({
    id: id,
    tabName: data.form.nodes.length + 1,
    url: '',
    urlSuffix: '',
    ip: '',
    port: data.form.dbType === 'MYSQL' ? 3306 : 5432,
    username: '',
    password: '',
    props: [{
      name: '',
      value: ''
    }],
    status: -1
  })
  visible.value = true
  data.activeTab = id
}

const handleDelete = (val) => {
  if (refObj.value[val]) {
    delete refObj.value[val]
  }
  data.form.nodes = data.form.nodes.filter((item) => {
    return item.id !== val
  })
  nextTick(() => {
    data.activeTab = data.form.nodes[0].id
  })
}

const open = async (type) => {
  data.show = true
  data.loading = false
  data.testLoading = false
  refObj.value = {}
  getHostList()
  data.title = t('components.AddJdbc.5q0a7i43f3c0')
  Object.assign(data.form, {
    clusterId: '',
    name: '',
    dbType: type,
    nodes: [],
    status: -1
  })
  handleAdd()
  formRef.value?.clearValidate()
  formRef.value?.resetFields()
}

defineExpose({
  open
})

</script>

<style lang="less" scoped>
@import '@/assets/style/openGlobal.less';

.title-con {
  width: 540px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .params-info {
    font-size: 14px;
    font-weight: normal;
  }
}

.config-con {
  .diy-info-con {
    margin-bottom: 10px;
  }

  .basic-config-con {
    .basic-title {
      padding-left: 13px;
      margin-bottom: 10px;
    }

    :deep(.row-changed) {
      .arco-table-td {
        background: var(--color-neutral-3);
      }
    }
  }

  .super-config-con {
    margin-top: 10px;

    :deep(.arco-collapse-item-content) {
      padding-left: 0;
      padding-right: 0;
      background-color: transparent;

      .arco-collapse-item-content-box {
        padding: 0;
      }
    }

    :deep(.row-changed) {
      .arco-table-td {
        background: var(--color-neutral-3);
      }
    }
  }
}

:deep(.arco-form-item) {
  margin-bottom: 0;
}

:deep(.arco-col-5) {
  flex: none;
  width: auto;
}

.single-line-text {
  width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

:deep(.el-input-number .el-input__inner) {
  text-align: left;
}

.table-container {
  transition: height 0.3s ease;
}

.gray-card {
  background-color: rgb(var(--gray-2));
  margin-bottom: 16px;
}

:deep(.card-content) {
  text-align: left;
}

.custom-drawer {
  width: 50% !important;
  margin-left: calc(100% - 100vw);
}

.compact-divider {
  margin: 0 !important;
}

.custom-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
</style>
