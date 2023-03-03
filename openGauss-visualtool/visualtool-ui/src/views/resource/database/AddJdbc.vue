<template>
  <a-modal :mask-closable="false" :esc-to-close="false" :visible="data.show" :title="data.title" :unmount-on-close="true"
    :ok-loading="data.loading" :modal-style="{ width: '650px' }" @cancel="close">
    <template #footer>
      <div class="flex-between">
        <div class="flex-row">
          <div class="label-color mr" v-if="data.form.status === jdbcStatusEnum.unTest">待检测
          </div>
          <a-tag v-if="data.form.status === jdbcStatusEnum.success" color="green">可用</a-tag>
          <a-tag v-if="data.form.status === jdbcStatusEnum.fail" color="red">不可用</a-tag>
        </div>
        <div>
          <a-button class="mr" @click="close">取消</a-button>
          <a-button :loading="data.testLoading" class="mr" @click="handleTestHost()">测试连通性</a-button>
          <a-button :loading="data.loading" type="primary" @click="submit">确定</a-button>
        </div>
      </div>
    </template>
    <a-form :model="data.form" ref="formRef" auto-label-width :rules="formRules">
      <a-form-item field="name" label="集群名称" validate-trigger="blur">
        <a-input v-model="data.form.name" placeholder="请输入实例名称"></a-input>
      </a-form-item>
      <a-form-item label="数据库类型" validate-trigger="change">
        <a-select class="select-w" v-model="data.form.dbType" placeholder="请选择数据库类型">
          <a-option v-for="item in data.dbTypes" :key="item.value" :value="item.value">{{
            item.label
          }}</a-option>
        </a-select>
      </a-form-item>
    </a-form>
    <a-tabs type="card-gutter" :editable="true" @add="handleAdd" @delete="handleDelete" v-model="data.activeTab"
      show-add-button auto-switch>
      <a-tab-pane v-for="item of data.form.nodes" :key="item.id" :closable="data.form.nodes.length > 1">
        <template #title>
          <a-tooltip content="不可用" v-if="item.status === jdbcStatusEnum.fail">
            <icon-exclamation-circle-fill />
          </a-tooltip>
          {{ item.ip.trim() ? item.ip : '实例' + item.tabName }}
        </template>
        <div class="jdbc-instance-c">
          <jdbc-instance :form-data="item" :ref="(el: any) => setRefMap(el, item.id)"></jdbc-instance>
        </div>
      </a-tab-pane>
    </a-tabs>
  </a-modal>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { nextTick, reactive, ref, computed } from 'vue'
import { addJdbc, editJdbc } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
import JdbcInstance from './JdbcInstance.vue'
// import { useI18n } from 'vue-i18n'
// import { encryptPassword } from '@/utils/jsencrypt'
// const { t } = useI18n()
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
  form: {
    clusterId: '',
    name: '',
    dbType: 'mysql',
    nodes: [],
    status: jdbcStatusEnum.unTest
  },
  activeTab: '',
  dbTypes: [
    { label: 'MYSQL', value: 'mysql' }
  ]
})

const formRules = computed(() => {
  return {
    name: [
      { required: true, 'validate-trigger': 'blur', message: '请输入集群名称' },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb('不能为纯空格')
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

const emits = defineEmits([`finish`])
const formRef = ref<null | FormInstance>(null)
const submit = () => {
  const methodArr = []
  for (let i = 0; i < refList.value.length; i++) {
    if (refList.value[i]) {
      methodArr.push(refList.value[i].formValidate())
    }
  }
  methodArr.push(formRef.value?.validate())
  Promise.all(methodArr).then((res) => {
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

    const param: {
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
    data.form.nodes.forEach((item: any) => {
      item.extendProps = JSON.stringify(item.props)
      param.nodes.push(item)
    })

    if (data.form.clusterId) {
      editJdbc(data.form.clusterId, param).then((res: KeyValue) => {
        data.loading = false
        if (Number(res.code) === 200) {
          Message.success({ content: `Modified success` })
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
          Message.success({ content: `Create success` })
          emits(`finish`)
        }
        close()
      }).finally(() => {
        data.loading = false
      })
    }
  })
}
const close = () => {
  data.show = false
  nextTick(() => {
    formRef.value?.clearValidate()
    formRef.value?.resetFields()
  })
  delRefObj()
}

const handleTestHost = () => {
  console.log('show refList', refList.value)
  data.testLoading = true
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
  data.form.nodes.push({
    id: id,
    tabName: data.form.nodes.length + 1,
    url: '',
    urlSuffix: '',
    ip: '',
    port: 3306,
    username: '',
    password: '',
    props: [{
      name: '',
      value: ''
    }],
    status: jdbcStatusEnum.unTest
  })
  console.log('show id', id, refObj.value);

  data.activeTab = id
}

const handleDelete = (val: any) => {
  if (refObj.value[val]) {
    delete refObj.value[val]
  }
  data.form.nodes = data.form.nodes.filter((item: KeyValue) => {
    return item.id !== val
  })
  data.activeTab = data.form.nodes[0].id
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

const open = (type: string, editData?: KeyValue) => {
  data.show = true
  data.loading = false
  if (type === 'update' && data) {
    data.title = '修改数据源'
    console.log('update jdbc', editData)
    if (editData) {
      Object.assign(data.form, {
        clusterId: editData.clusterId,
        name: editData.name,
        nodes: []
      })
      editData.nodes.forEach((item: KeyValue) => {
        const temp = {
          id: item.clusterNodeId,
          url: item.url,
          ip: item.ip,
          port: Number(item.port),
          username: item.username,
          password: item.password,
          props: getProps(item.url),
          status: jdbcStatusEnum.unTest
        }
        data.form.nodes.push(temp)
      })
      data.activeTab = data.form.nodes[0].id
    }
  } else {
    data.title = '新增数据源'
    Object.assign(data.form, {
      clusterId: '',
      name: '',
      dbType: 'mysql',
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