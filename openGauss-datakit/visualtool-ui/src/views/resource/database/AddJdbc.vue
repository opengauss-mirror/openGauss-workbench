<template>
  <a-modal :mask-closable="false" :esc-to-close="false" :visible="data.show" :title="data.title" :unmount-on-close="true"
    :ok-loading="data.loading" :modal-style="{ width: '650px' }" @cancel="close">
    <template #footer>
      <div class="flex-between">
        <div class="flex-row">
          <div class="label-color mr" v-if="data.form.status === jdbcStatusEnum.unTest">{{ $t('database.AddJdbc.else1') }}
          </div>
          <a-tag v-if="data.form.status === jdbcStatusEnum.success" color="green">{{ $t('database.AddJdbc.5oxhkhiks5k0')
          }}</a-tag>
          <a-tag v-if="data.form.status === jdbcStatusEnum.fail" color="red">{{ $t('database.AddJdbc.5oxhkhimwfg0')
          }}</a-tag>
        </div>
        <div>
          <a-button class="mr" @click="close">{{ $t('database.AddJdbc.5oxhkhimwy00') }}</a-button>
          <a-button :loading="data.testLoading" class="mr" @click="handleTestHost()">{{
            $t('database.AddJdbc.5oxhkhimx5c0') }}</a-button>
          <a-button :loading="data.loading" type="primary" @click="submit">{{ $t('database.AddJdbc.5oxhkhimxbg0')
          }}</a-button>
        </div>
      </div>
    </template>
    <a-form :model="data.form" ref="formRef" auto-label-width :rules="formRules">
      <a-row :gutter="16">
        <a-col :span="19">
          <a-form-item v-if="!data.form.isCustomName" :label="$t('database.AddJdbc.5oxhkhimxho0')"
            validate-trigger="blur">
            <a-input v-model="clusterName" :placeholder="$t('database.AddJdbc.customNamePlaceholder')" disabled></a-input>
          </a-form-item>
          <a-form-item v-else field="name" :label="$t('database.AddJdbc.5oxhkhimxho0')" validate-trigger="blur">
            <a-input v-model="data.form.name" :placeholder="$t('database.AddJdbc.5oxhkhimz480')"></a-input>
          </a-form-item>
        </a-col>
        <a-col :span="5">
          <a-form-item hide-label>
            <a-checkbox v-model="data.form.isCustomName" @change="handleCustomChange(data.form.isCustomName)">
              {{ $t('database.AddJdbc.isCustomName') }}
            </a-checkbox>
          </a-form-item>
        </a-col>
      </a-row>
      <a-form-item :label="$t('database.AddJdbc.5oxhkhimxto0')" validate-trigger="change">
        <a-select class="select-w" v-model="data.form.dbType" :placeholder="$t('database.AddJdbc.5oxhkhimxzw0')">
          <a-option v-for="item in data.dbTypes" :key="item.value" :value="item.value">{{
            item.label
          }}</a-option>
        </a-select>
      </a-form-item>
    </a-form>
    <a-tabs type="card-gutter" :editable="true" @tab-click="handleTabClick" @add="handleAdd" @delete="handleDelete"
      v-model:active-key="data.activeTab" show-add-button auto-switch>
      <a-tab-pane v-for="item of data.form.nodes" :key="item.id" :closable="data.form.nodes.length > 1">
        <template #title>
          <a-tooltip :content="$t('database.AddJdbc.5oxhkhimwfg0')" v-if="item.status === jdbcStatusEnum.fail">
            <icon-exclamation-circle-fill />
          </a-tooltip>
          {{ item.ip.trim() ? item.ip : $t('database.AddJdbc.5oxhkhimyio0') + item.tabName }}
        </template>
        <div class="jdbc-instance-c">
          <jdbc-instance :form-data="item" :host-list="data.hostList" :jdbc-type="data.form.dbType"
            :ref="(el: any) => setRefMap(el, item.id)"></jdbc-instance>
        </div>
      </a-tab-pane>
    </a-tabs>
  </a-modal>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { nextTick, reactive, ref, computed } from 'vue'
import { addJdbc, editJdbc, hostListAll } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
import JdbcInstance from './JdbcInstance.vue'
import { useI18n } from 'vue-i18n'
import { encryptPassword, decryptPassword } from "@/utils/jsencrypt";
import showMessage from '@/hooks/showMessage'
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
    dbType: 'MYSQL',
    nodes: [],
    status: jdbcStatusEnum.unTest
  },
  hostList: [],
  activeTab: '',
  dbTypes: [
    { label: 'MySQL', value: 'MYSQL' },
    { label: 'openGauss', value: 'OPENGAUSS' },
    { label: 'PostgreSQL', value: 'POSTGRESQL' }
  ]
})
const formRef = ref<null | FormInstance>(null)
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
      { required: true, 'validate-trigger': 'blur', message: t('database.AddJdbc.5oxhkhimz480') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('database.AddJdbc.5oxhkhimzcg0'))
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

const refObj = ref<KeyValue>({})
const setRefMap = (el: HTMLElement, key: string) => {
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

const submit = async() => {
  const methodArr = []
  for (let i = 0; i < refList.value.length; i++) {
    if (refList.value[i]) {
      methodArr.push(refList.value[i].formValidate())
    }
  }
  methodArr.push(formRef.value?.validate())
  Promise.all(methodArr).then(async (res) => {
    console.log('validRes', res)
    const validRes = res.filter((item: KeyValue) => {
      return item && item.res === false
    })
    console.log('validRes', validRes)
    if (validRes.length) {
      data.activeTab = validRes[0].id
      return
    }
    if (res[methodArr.length - 1]) {
      return
    }

    // save
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
    for (const item of param.nodes) {
      const temppassword = await encryptPassword(item.password)
      item.password = temppassword
    }
    if (data.form.clusterId) {
      editJdbc(data.form.clusterId, param).then((res: KeyValue) => {
        data.loading = false
        if (Number(res.code) === 200) {
          Message.success({content: `Modified success`})
          emits(`finish`)
        }
        close()
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
        console.log(error)
      }) .finally(() => {
        data.loading = false
      })
    }
  })
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
  console.log('show refList', refList.value)
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

const handleTabClick = (val: any) => {
  console.log('show handle tab click', val)

}

const handleAdd = () => {
  const id = new Date().getTime() + ''
  let port = 3306
  if (data.form.dbType === 'OPENGAUSS') {
    port = 5432
  }
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
  nextTick(() => {
    data.activeTab = id
  })
}

const handleDelete = (val: any) => {
  if (refObj.value[val]) {
    delete refObj.value[val]
  }
  data.form.nodes = data.form.nodes.filter((item: KeyValue) => {
    return item.id !== val
  })
  nextTick(() => {
    data.activeTab = data.form.nodes[0].id
  })
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
      dbType: 'MYSQL',
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
</style>
