<template>
  <el-dialog :close-on-click-modal="false" :close-on-press-escape="false" :destroy-on-close="true"
             :model-value="data.show" :title="data.title" :class="['openDesignDialog', 'super-large-dialog']"
             :ok-loading="data.loading" @close="close">
    <template #footer>
      <div class="flex-between">
        <div class="flex-row" v-if="data.form.nodes.length > 1">
          <div class="label-color mr" v-if="data.form.status === jdbcStatusEnum.unTest">
            {{ $t('database.AddJdbc.else1') }}
          </div>
          <el-tag v-if="data.form.status === jdbcStatusEnum.success" type="success">
            {{ $t('database.AddJdbc.5oxhkhiks5k0') }}
          </el-tag>
          <el-tag v-if="data.form.status === jdbcStatusEnum.fail" type="danger">
            {{ $t('database.AddJdbc.5oxhkhimwfg0') }}
          </el-tag>
        </div>
        <div>
          <el-button class="mr" @click="close">{{ $t('database.AddJdbc.5oxhkhimwy00') }}</el-button>
          <el-button :loading="data.testLoading" class="mr" @click="handleTestHost()" v-if="data.form.nodes.length > 1">
            {{ $t('database.AddJdbc.5oxhkhimx5c0') }}
          </el-button>
          <el-button :loading="data.loading" type="primary" @click="submit">
            {{ $t('database.AddJdbc.5oxhkhimxbg0') }}
          </el-button>
        </div>
      </div>
    </template>
    <el-form :model="data.form" ref="formRef" label-width="auto" :rules="formRules">
      <el-row :gutter="16">
        <el-col :span="18">
          <el-form-item v-if="!data.form.isCustomName" :label="$t('database.AddJdbc.5oxhkhimxho0')"
                        validate-trigger="blur">
            <el-input v-model="clusterName" :placeholder="$t('database.AddJdbc.customNamePlaceholder')" disabled></el-input>
          </el-form-item>
          <el-form-item v-else prop="name" :label="$t('database.AddJdbc.5oxhkhimxho0')" validate-trigger="blur">
            <el-input v-model="data.form.name" :placeholder="$t('database.AddJdbc.5oxhkhimz480')"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label-width="0">
            <el-checkbox v-model="data.form.isCustomName" @change="handleCustomChange(data.form.isCustomName)">
              {{ $t('database.AddJdbc.isCustomName') }}
            </el-checkbox>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item :label="$t('database.AddJdbc.5oxhkhimxto0')" validate-trigger="change" style="margin-bottom: 14px">
        <el-radio-group v-model="data.form.dbType" class="radio-group-w" @change="changeJDBCType">
          <el-radio-button v-for="item in data.dbTypes" :label="item.label" :value="item.value" class="radio-item">
            {{ item.label }}
          </el-radio-button>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <div v-if="data.form.dbType === JDBCType.Milvus || data.form.dbType === JDBCType.Elasticsearch">
      <div class="jdbc-instance-c">
        <jdbc-instance :form-data="data.form.nodes[0]" :host-list="data.hostList" :jdbc-type="data.form.dbType"
                       :ref="(el: any) => setRefMap(el, data.form.nodes[0].id)" />
      </div>
    </div>
    <div v-else>
      <el-tabs type="card" :editable="true" :closable="true" class="clean-add-icon-only"
               @tab-remove="handleDelete" @add="handleAdd" @edit="handleTabsEdit"
               v-model="data.activeTab"  :show-add-button="true"  :auto-switch="true">
        <template #add-icon>
          <el-icon class="custom-add-icon"><IconPlus /></el-icon>
        </template>
        <el-tab-pane v-for="item of data.form.nodes" :key="item.id" :name="item.id"
                     :closable="data.form.nodes.length > 1" class="bordered-tab-pane">
          <template #label>
            <span class="tab-label">
              <el-tooltip v-if="item.status === jdbcStatusEnum.fail" :content="$t('database.AddJdbc.5oxhkhimwfg0')" placement="top">
                <el-icon><WarningFilled /></el-icon>
              </el-tooltip>
            {{ item.ip.trim() ? item.ip : $t('database.AddJdbc.5oxhkhimyio0') + item.tabName }}
            </span>
          </template>
          <div class="jdbc-instance-c">
            <jdbc-instance :form-data="item" :host-list="data.hostList" :jdbc-type="data.form.dbType"
                           :ref="(el: any) => setRefMap(el, item.id)"></jdbc-instance>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

  </el-dialog>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { ElForm } from 'element-plus'
import { nextTick, reactive, ref, computed } from 'vue'
import { addJdbc, editJdbc, hostListAll } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
import JdbcInstance from './JdbcInstance.vue'
import { useI18n } from 'vue-i18n'
import { encryptPassword } from "@/utils/jsencrypt";
import showMessage from '@/hooks/showMessage'
import {JDBCType} from "@/types/jdbc";
import type { TabPaneName } from 'element-plus'
const { t } = useI18n()
enum jdbcStatusEnum {
  unTest = -1,
  success = 1,
  fail = 0
}

const data = reactive<KeyValue>({
  show: false,
  title: '',
  testLoading: false,
  loading: false,
  getHostLoading: false,
  form: {
    clusterId: '',
    name: '',
    isCustomName: false,
    dbType: JDBCType.MySQL,
    nodes: [],
    status: jdbcStatusEnum.unTest
  },
  hostList: [],
  activeTab: '',
  dbTypes: [
    { label: 'MySQL', value: JDBCType.MySQL },
    { label: 'openGauss', value: JDBCType.openGauss },
    { label: 'PostgreSQL', value: JDBCType.PostgreSQL },
    { label: 'Elasticsearch', value: JDBCType.Elasticsearch },
    { label: 'Milvus', value: JDBCType.Milvus }
  ]
})
const formRef = ref<null | InstanceType<typeof ElForm>>(null)
const handleCustomChange = (val: boolean) => {
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

const getNameByNode = (data: KeyValue) => {
  let result = ''
  if (data.nodes.length) {
    data.nodes.forEach((item: KeyValue, index: number) => {
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
      { required: true, message: t('database.AddJdbc.5oxhkhimz480'), trigger: 'blur' },
      {
        validator: (rule: any, value: any, callback: any) => {
          if (!value.trim()) {
            callback(new Error(t('database.AddJdbc.5oxhkhimzcg0')))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ]
  }
})

const refObj = ref<KeyValue>({})
const setRefMap = (el: HTMLElement, key: string) => {
  if (!refObj.value[key]) {
    refObj.value[key] = el
  }
}

const refList = computed(() => {
  const result = []
  const refs = Object.keys(refObj.value)
  const validNodeIds = data.form.nodes.map((node: KeyValue) => node.id)
  if (refs.length) {
    for (let key in refObj.value) {
      if (refObj.value[key] && validNodeIds.includes(key)) {
        result.push(refObj.value[key])
      }
    }
  }
  return result
})

const getHostList = () => {
  data.getHostLoading = true
  hostListAll().then((res: KeyValue) => {
    console.log('show hostLIst', res)
    data.hostList = []
    if (Number(res.code) === 200) {
      if (res.data.length) {
        res.data.forEach((item: KeyValue) => {
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

const submit = async () => {
  if (!formRef.value) return
  const methodArr = []
  for (let i = 0; i < refList.value.length; i++) {
    if (refList.value[i]) {
      methodArr.push(refList.value[i].formValidate())
    }
  }

  try {
    await formRef.value.validate()
    const res = await Promise.all(methodArr)
    const validRes = res.filter((item: KeyValue) => {
      return item && item.res === false
    })
    if (validRes.length) {
      data.activeTab = validRes[0].id
      return
    }
    data.loading = true

    let param: {
      clusterName: string,
      dbType: string,
      deployType: string,
      nodes: Array<KeyValue>
    } = {
      clusterName: data.form.name,
      dbType: data.form.dbType,
      deployType: data.form.nodes.length > 1 ? 'CLUSTER' : 'SINGLE_NODE',
      nodes: []
    }
    if (!data.form.isCustomName) {
      param.clusterName = clusterName.value
    }
    data.form.nodes.forEach((item: any) => {
      const newItem = { ...item }
      newItem.extendProps = JSON.stringify(newItem.props)
      param.nodes.push(newItem)
    })
    if(data.form.dbType !== JDBCType.Milvus && data.form.dbType !== JDBCType.Elasticsearch) {
      for (const item of param.nodes) {
        if ( item.password.length > 0 ) {
          const temppassword = await encryptPassword(item.password)
          item.password = temppassword
        }
      }
    }
    if (data.form.clusterId) {
      editJdbc(data.form.clusterId, param).then((res: KeyValue) => {
        data.loading = false
        if (Number(res.code) === 200) {
          Message.success({content: `Modified success`})
          emits(`finish`)
        }
        close()
      }).catch((error) => {
        data.form.status = jdbcStatusEnum.fail
        const errorMsg = error?.message || error?.msg || String(error)
        if (errorMsg.includes('jdbc failed to get connection') && data.form.nodes.length === 1) {
          data.form.nodes[0].status = jdbcStatusEnum.fail
        } else {
          data.form.nodes[0].status = jdbcStatusEnum.unTest
        }
        console.log(error)
      }).finally(() => {
        data.loading = false
      })
    } else {
      addJdbc(param).then((res: KeyValue) => {
        data.loading = false
        if (Number(res.code) === 200) {
          Message.success({content: `Create success`})
          emits(`finish`)
        }
        close()
      }).catch((error) => {
        data.form.status = jdbcStatusEnum.fail
        const errorMsg = error?.message || error?.msg || String(error)
        if (errorMsg.includes('jdbc failed to get connection') && data.form.nodes.length === 1) {
          data.form.nodes[0].status = jdbcStatusEnum.fail
        } else {
          data.form.nodes[0].status = jdbcStatusEnum.unTest
        }
        console.log(error)
      }).finally(() => {
        data.loading = false
      })
    }
  } catch (error) {
    console.log('Validation failed:', error)
    return
  }
}

const close = () => {
  nextTick(() => {
    formRef.value?.clearValidate()
    formRef.value?.resetFields()
    delRefObj()
  })
  data.show = false
}

const handleTestHost = () => {
  const methodArr = []
  for (let i = 0; i < refList.value.length; i++) {
    if (refList.value[i]) {
      methodArr.push(refList.value[i].formValidate())
    }
  }
  Promise.all(methodArr).then((res) => {
    const validRes = res.filter((item: KeyValue) => {
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
      const noPass = testRes.filter((item: KeyValue) => {
        return item.res === false
      })
      if (noPass.length) {
        data.activeTab = noPass[0].id
        data.form.status = jdbcStatusEnum.fail
        return
      }
      data.form.status = jdbcStatusEnum.success
    }).finally(() => {
      data.testLoading = false
    })
  })
}

const handleAdd = () => {
  const id = new Date().getTime() + ''
  let port = JDBCType.getDefaultPort(data.form.dbType)
  let username = ''
  let password = ''
  const firstNode = data.form.nodes[0]
  if (firstNode) {
    port = firstNode.port
    username = firstNode.username
    password = firstNode.password
  }
  data.form.nodes.push({
    id: id,
    tabName: data.form.nodes.length + 1,
    url: '',
    urlSuffix: '',
    ip: '',
    port: port,
    username: username,
    password: password,
    props: [{
      name: '',
      value: ''
    }],
    status: jdbcStatusEnum.unTest
  })
  data.activeTab = id
}

const handleDelete = (val: any) => {
  if (refObj.value[val]) {
    delete refObj.value[val]
  }
  data.form.nodes = data.form.nodes.filter((item: KeyValue) => {
    return item.id !== val
  })
  if (data.form.nodes.length > 0) {
    data.activeTab = data.form.nodes[0].id
  }
}

const handleTabsEdit = (
  targetName: TabPaneName | undefined,
  action: 'remove' | 'add'
) => {
  if (action === 'add') {
    handleAdd()
  } else if (action === 'remove') {
    handleDelete(targetName)
  }
}

const changeJDBCType = () => {
  delRefObj()
  data.form.nodes = []
  handleAdd()
}

const getProps = (url: string): KeyValue[] => {
  const result: KeyValue[] = []
  if (!url) {
    result.push({
      name: '',
      value: ''
    })
    return result
  }
  const urlSuffix = url.split('?')[1]
  if (!urlSuffix) {
    result.push({
      name: '',
      value: ''
    })
    return result
  }
  const extendPropsArr = urlSuffix.split('&')
  extendPropsArr.forEach((item: string) => {
    const itemArr = item.split('=')
    const temp = {
      name: itemArr[0],
      value: itemArr[1]
    }
    result.push(temp)
  })
  if (!result.length) {
    result.push({
      name: '',
      value: ''
    })
  }
  return result
}

const open = async (type: string, editData?: KeyValue) => {
  data.show = true
  data.loading = false
  data.testLoading = false
  getHostList()
  if (type === 'update' && data) {
    delRefObj()
    data.title = t('database.AddJdbc.5oxhkhimzmw0')
    if (editData) {
      Object.assign(data.form, {
        clusterId: editData.clusterId,
        name: editData.name,
        dbType: editData.dbType,
        nodes: []
      })
      let flag = true;
      for (const item of editData.nodes) {
        const temp = {
          id: item.clusterNodeId,
          url: item.url,
          ip: item.ip,
          port: Number(item.port),
          username: item.username,
          password: '',
          props: getProps(item.url),
          status: jdbcStatusEnum.unTest
        }
        if (!temp.password) {
          flag = false;
        }
        data.form.nodes.push(temp)
      }
      if (!flag) {
        showMessage('warning', t('database.AddJdbc.checkPassword'))
      }
      const nameByNode = getNameByNode(data.form)
      if (nameByNode === data.form.name) {
        data.form.isCustomName = false
      }
      nextTick(() => {
        data.activeTab = data.form.nodes[0].id
      })
    }
  } else {
    data.title = t('database.AddJdbc.5oxhkhimzww0')
    delRefObj()
    Object.assign(data.form, {
      clusterId: '',
      name: '',
      dbType: JDBCType.MySQL,
      nodes: [],
      status: jdbcStatusEnum.unTest
    })
    handleAdd()
  }
}

const delRefObj = () => {
  for (let key in refObj.value) {
    delete refObj.value[key]
  }
}

defineExpose({
  open
})

</script>
<style lang="less" scoped>
.jdbc-instance-c {
  padding: 15px;
}
.bordered-tab-pane {
  border: 1px solid var(--o-border-color-base);
  border-top: none;
  margin-top: -1px;
}

.custom-add-btn-tabs :deep(.el-tabs__new-tab) {
  background: transparent;
  border: 1px dashed var(--el-border-color);
  color: var(--el-text-color-secondary);
}

.custom-add-btn-tabs :deep(.el-tabs__new-tab:hover) {
  background: transparent;
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}

.clean-add-icon-only :deep(.el-tabs__new-tab) {
  border: none !important;
  background: transparent !important;
  box-shadow: none !important;
  margin-left: 8px;
}

.add-icon-clean {
  color: var(--o-color-info);
  transition: color 0.3s ease;
}

.tab-content-wrapper {
  padding: 16px;
}
</style>
