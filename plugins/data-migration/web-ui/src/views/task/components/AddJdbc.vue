<template>
  <a-modal
    :mask-closable="false"
    :esc-to-close="false"
    :visible="data.show"
    :title="data.title"
    :unmount-on-close="true"
    :ok-loading="data.loading"
    :modal-style="{ width: '650px' }"
    @cancel="close"
  >
    <template #footer>
      <div class="flex-between">
        <div class="flex-row">
          <div class="label-color mr" v-if="data.form.status === -1">{{$t('components.AddJdbc.5q0a7i439cg0')}}</div>
          <a-tag v-if="data.form.status === 1" color="green">{{$t('components.AddJdbc.5q0a7i43aeg0')}}</a-tag>
          <a-tag v-if="data.form.status === 0" color="red">{{$t('components.AddJdbc.5q0a7i43ajk0')}}</a-tag>
        </div>
        <div>
          <a-button class="mr" @click="close">{{$t('components.AddJdbc.5q0a7i43amo0')}}</a-button>
          <a-button :loading="data.testLoading" class="mr" @click="handleTestHost()">{{$t('components.AddJdbc.5q0a7i43ap40')}}</a-button>
          <a-button :loading="data.loading" type="primary" @click="submit">{{$t('components.AddJdbc.5q0a7i43as00')}}</a-button>
        </div>
      </div>
    </template>
    <a-form :model="data.form" ref="formRef" auto-label-width :rules="formRules">
      <a-row :gutter="16">
        <a-col :span="19">
          <a-form-item v-if="!data.form.isCustomName" :label="$t('components.AddJdbc.5q0a7i43aw00')"
            validate-trigger="blur">
            <a-input v-model="clusterName" :placeholder="$t('components.AddJdbc.5q0a7i43bkk0')" disabled></a-input>
          </a-form-item>
          <a-form-item v-else field="name" :label="$t('components.AddJdbc.5q0a7i43aw00')" validate-trigger="blur">
            <a-input v-model="data.form.name" :placeholder="$t('components.AddJdbc.5q0a7i43boo0')"></a-input>
          </a-form-item>
        </a-col>
        <a-col :span="5">
          <a-form-item hide-label>
            <a-checkbox v-model="data.form.isCustomName" @change="handleCustomChange(data.form.isCustomName)">
              {{$t('components.AddJdbc.5q0a7i43br80')}}
            </a-checkbox>
          </a-form-item>
        </a-col>
      </a-row>
      <a-form-item :label="$t('components.AddJdbc.5q0a7i43bto0')" validate-trigger="change">
        <a-select class="select-w" v-model="data.form.dbType" disabled :placeholder="$t('components.AddJdbc.5q0a7i43bwk0')">
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
          <a-tooltip :content="$t('components.AddJdbc.5q0a7i43ajk0')" v-if="item.status === 0">
            <icon-exclamation-circle-fill />
          </a-tooltip>
          {{ item.ip.trim() ? item.ip : $t('components.AddJdbc.5q0a7i43bzk0') + item.tabName }}
        </template>
        <div class="jdbc-instance-c">
          <jdbc-instance :form-data="item" :host-list="data.hostList" :jdbc-type="data.form.dbType" :ref="(el) => setRefMap(el, item.id)"></jdbc-instance>
        </div>
      </a-tab-pane>
    </a-tabs>
  </a-modal>
</template>

<script setup>
import { nextTick, reactive, ref, computed } from 'vue'
import { addJdbc, hostListAll } from '@/api/task'
import { Message } from '@arco-design/web-vue'
import JdbcInstance from './JdbcInstance.vue'
import { useI18n } from 'vue-i18n'
import {encryptPassword} from "@/utils/jsencrypt";

const { t } = useI18n()

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
    { label: 'MySQL', value: 'MYSQL' },
    { label: 'openGauss', value: 'OPENGAUSS' }
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
      { required: true, 'validate-trigger': 'blur', message: t('components.AddJdbc.5q0a7i43c280') },
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
    console.log('show hostLIst', res)
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
    const validRes = res.filter((item) => {
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
      item.extendProps = JSON.stringify(item.props)
      param.nodes.push(item)
    })
    for (const item of param.nodes) {
      const temppassword = await encryptPassword(item.password)
      item.password = temppassword
    }
    addJdbc(param).then((res) => {
      data.loading = false
      if (Number(res.code) === 200) {
        Message.success({content: `Create success`})
        emits(`finish`, data.form.dbType)
      }
      close()
    }).finally(() => {
      data.loading = false
    })
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

const handleTabClick = (val) => {
  console.log('show handle tab click', val)

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
  nextTick(() => {
    data.activeTab = id
  })
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

const open = (dbType) => {
  delRefObj()
  data.show = true
  data.loading = false
  data.testLoading = false
  getHostList()
  data.title = t('components.AddJdbc.5q0a7i43f3c0')
  Object.assign(data.form, {
    clusterId: '',
    name: '',
    dbType,
    nodes: [],
    status: -1
  })
  handleAdd()
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
.flex-between {
  display: flex;
  justify-content: space-between;
}
.flex-row {
  display: flex;
  align-items: center;
}
.label-color {
  color: var(--color-text-2)
}
.mr {
  margin-right: 20px;
}
.jdbc-instance-c {
  padding: 15px;
}
</style>
