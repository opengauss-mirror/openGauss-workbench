<template>
  <a-modal :mask-closable="false" :esc-to-close="false" :visible="data.show" :title="data.title"
    :ok-loading="data.loading" :modal-style="{ width: '650px' }" @cancel="close">
    <template #footer>
      <div class="flex-between">
        <div class="flex-row">
          <div class="label-color mr" v-if="data.formData.status === jdbcStatusEnum.unTest">待检测
          </div>
          <a-tag v-if="data.formData.status === jdbcStatusEnum.success" color="green">可用</a-tag>
          <a-tag v-if="data.formData.status === jdbcStatusEnum.fail" color="red">不可用</a-tag>
        </div>
        <div>
          <a-button class="mr" @click="close">取消</a-button>
          <a-button :loading="data.testLoading" class="mr" @click="handleTestHost(0)">测试连通性</a-button>
          <a-button :loading="data.loading" type="primary" @click="submit">确定</a-button>
        </div>
      </div>

    </template>
    <a-form :model="data.formData" ref="formRef" auto-label-width :rules="data.rules">
      <a-form-item field="name" label="实例名称" validate-trigger="blur" :rules="[{ required: true, message: '请输入实例名称' }]">
        <a-input v-model="data.formData.name" placeholder="请输入实例名称"></a-input>
      </a-form-item>
      <a-form-item label="数据库类型" validate-trigger="change">
        <a-select class="select-w" v-model="data.formData.dbType" placeholder="请选择数据库类型">
          <a-option v-for="item in data.dbTypes" :key="item.value" :value="item.value">{{
            item.label
          }}</a-option>
        </a-select>
      </a-form-item>
      <a-form-item field="ip" label="IP地址" validate-trigger="blur" :rules="[{ required: true, message: '请输入IP地址' }]">
        <a-input v-model="data.formData.ip" placeholder="请输入IP地址" />
      </a-form-item>
      <a-form-item field="port" label="端口" validate-trigger="blur" :rules="[{ required: true, message: '请输入端口' }]">
        <a-input-number v-model="data.formData.port" placeholder="请输入端口" />
      </a-form-item>
      <a-form-item field="username" label="用户名" validate-trigger="blur"
        :rules="[{ required: true, message: '请输入用户名' }]">
        <a-input v-model="data.formData.username" placeholder="请输入用户名" />
      </a-form-item>
      <a-form-item field="password" label="密码" validate-trigger="blur" :rules="[{ required: true, message: '请输入密码' }]">
        <a-input-password v-model="data.formData.password" placeholder="请输入密码" allow-clear />
      </a-form-item>
      <a-form-item field="url" label="连接地址" validate-trigger="blur">
        <div class="flex-row" style="width: 100%;">
          <a-input class="mr-s" disabled v-model="url"></a-input>
          <a-input v-model="data.formData.urlSuffix" placeholder="请输入扩展属性" />
        </div>
      </a-form-item>
      <!-- <a-form-item label="说明">
        <a-textarea v-model="data.formData.remark" placeholder="请输入说明"></a-textarea>
      </a-form-item> -->
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { nextTick, reactive, ref, computed } from 'vue'
import { addJdbc, editJdbc, jdbcNodePing } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
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
  formData: {
    name: '',
    url: '',
    urlSuffix: '',
    dbType: 'mysql',
    ip: '',
    port: 3306,
    username: '',
    password: '',
    remark: '',
    status: jdbcStatusEnum.unTest
  },
  rules: {
    name: [{ required: true, 'validate-trigger': 'blur', message: '请输入集群名称' }]
  },
  dbTypes: [
    { label: 'MYSQL', value: 'mysql' }
  ]
})

const emits = defineEmits([`finish`])
const formRef = ref<null | FormInstance>(null)
const submit = () => {
  formRef.value?.validate().then(async result => {
    if (!result) {
      data.loading = true
      let urlStr = url.value
      if (data.formData.urlSuffix) {
        urlStr = urlStr + '?' + data.formData.urlSuffix
      }
      const param = {
        clusterName: data.formData.name,
        deployType: 'SINGLE_NODE',
        nodes: [{
          url: urlStr,
          username: data.formData.username,
          password: data.formData.password
        }],
        remark: data.formData.remark
      }
      // const param = Object.assign({}, data.formData)
      // for (let i = 0; i < param.nodes.length; i++) {
      //   const instanceData = param.nodes[i]
      //   const encryptPwd = await encryptPassword(instanceData.password)
      //   instanceData.password = encryptPwd
      // }
      console.log('show save param', param)
      if (data.formData.clusterId) {
        editJdbc(data.formData.clusterId, param).then((res: KeyValue) => {
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
    }
  }).catch()
}
const close = () => {
  data.show = false
  nextTick(() => {
    formRef.value?.clearValidate()
    formRef.value?.resetFields()
  })
}

const handleTestHost = (index = 0) => {
  console.log('show index', index)
  formRef.value?.validate().then(async result => {
    if (!result) {
      data.testLoading = true
      // const instanceData = data.formData.nodes[index]
      // const encryptPwd = await encryptPassword(instanceData.password)
      // instanceData.password = encryptPwd
      // console.log('show jdbc param', instanceData)
      const param = {
        username: data.formData.username,
        password: data.formData.password,
        url: url.value
      }
      jdbcNodePing(param).then((res: KeyValue) => {
        if (Number(res.code) === 200 && res.data) {
          // ok
          data.formData.status = jdbcStatusEnum.success
        } else {
          data.formData.status = jdbcStatusEnum.fail
        }
      }).catch(() => {
        data.formData.status = jdbcStatusEnum.fail
      }).finally(() => {
        data.testLoading = false
      })
    }
  })
}

const url = computed(() => {
  if (data.formData.dbType === 'mysql') {
    return `jdbc:mysql://${data.formData.ip ? data.formData.ip : '{IP}'}:${data.formData.port ? data.formData.port : '{port}'}`
  }
  return 'jdbc:{dbType}://{IP}:{port}'
})

const open = (type: string, editData?: KeyValue) => {
  data.show = true
  data.loading = false
  if (type === 'update' && data) {
    data.title = '修改数据源'
    console.log('update jdbc', editData)
    if (editData) {
      let urlSuffixTemp
      if (editData.nodes[0].url) {
        const urlTemp = editData.nodes[0].url
        urlSuffixTemp = urlTemp.split('?')[1]
      }
      Object.assign(data.formData, {
        clusterId: editData.clusterId,
        name: editData.name,
        ip: editData.nodes[0].ip,
        port: Number(editData.nodes[0].port),
        urlSuffix: urlSuffixTemp,
        username: editData.nodes[0].username,
        password: '',
        remark: editData.remark
      })
    }
  } else {
    data.title = '新增数据源'
    Object.assign(data.formData, {
      // nodes: [{
      //   name: '',
      //   url: '',
      //   username: '',
      //   password: '',
      //   status: hostStatusEnum.unTest
      // }],
      clusterId: '',
      name: '',
      url: '',
      dbType: 'mysql',
      ip: '',
      port: 3306,
      urlSuffix: '',
      username: '',
      password: '',
      status: jdbcStatusEnum.unTest,
      remark: ''
    })
  }
}

defineExpose({
  open
})

</script>
