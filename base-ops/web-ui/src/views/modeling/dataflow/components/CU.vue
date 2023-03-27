
<template>
  <a-modal
    class="cu-container"
    :visible="dData.show" :title="dData.title" :ok-loading="dData.loading"
    :ok-text="$t('modeling.components.CU.5m6g1w13gjo0')" :cancel-text="$t('modeling.components.CU.5m6g1w13gz40')"
    @ok="submit" @cancel="close" :modal-style="{ width: '750px' }"
  >
    <div class="cu-dialog">
      <a-form :model="dData.formData" ref="formRef" :label-col="{ style: { width: '90px' } }" :rules="dData.rules">
        <a-form-item field="name" :label="$t('modeling.components.CU.5m6g1w13h2w0')" validate-trigger="blur">
          <a-input :max-length="140" show-word-limit  v-model="dData.formData.name" :placeholder="$t('modeling.components.CU.5m6g1w13h7c0')"></a-input>
        </a-form-item>
        <a-form-item field="dataBase" :label="$t('modeling.components.CU.5m6g1w13ha80')" validate-trigger="change">
          <a-cascader
            path-mode
            v-model="dData.formData.dataBase"
            :options="sourceList"
            :placeholder="$t('modeling.components.CU.5m6g1w13hdo0')"
            :load-more="loadMore"
            :fallback="cascaderCallback"
          />
        </a-form-item>
        <a-form-item field="tags" :label="$t('modeling.components.CU.5m6g1w13hgc0')" validate-trigger="blur">
          <a-input :max-length="140" show-word-limit  v-model="dData.formData.tags" :placeholder="$t('modeling.components.CU.5m6g1w13hiw0')"></a-input>
        </a-form-item>
        <a-form-item field="remark" :label="$t('modeling.components.CU.5m6g1w13hyo0')" validate-trigger="blur">
          <a-textarea v-model="dData.formData.remark" :placeholder="$t('modeling.components.CU.5m6g1w13i180')" :max-length="500"></a-textarea>
        </a-form-item>
      </a-form>
    </div>
  </a-modal>
  <CUUser ref="userRef" @success="addUserSuccess" />
</template>
<script setup lang="ts">
import { nextTick, reactive, ref } from 'vue'
import { Modal as AModal, Form as AForm, FormItem as AFormItem, Input as AInput, Textarea as ATextarea, Message } from '@arco-design/web-vue'
import CUUser from './CUUser.vue'
import { dataFlowAdd, dataFlowEdit, dataSourceDbList, getSchemeByClusterNodeId } from '@/api/modeling'
import { KeyValue } from '@/api/modeling/request'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const emits = defineEmits([`finish`])
const roles = [
  { id: 1, name: t('modeling.components.CU.5m6g1w13i3g0') },
  { id: 2, name: t('modeling.components.CU.5m6g1w13i640') },
  { id: 3, name: t('modeling.components.CU.5m6g1w13i8k0') }
]
const dData = reactive({
  show: false, loading: false, title: t('modeling.components.CU.5m6g1w13iaw0'),
  formData: { name: '', users: [], dataBase: [], tags: '', clusterId: '', clusterNodeId: '', dbName: '', schema: '', user: '', remark: '', type: '' } as KeyValue,
  formModal: { name: '', dataBase: [], tags: '', clusterId: '', clusterNodeId: '', dbName: '', schema: '', users: [
    { id: 1, name: '', role: 1, type: 'creator' }
  ], remark: '', type: '' },
  rules: {}
})
const open = (type: string, data?: KeyValue) => {
  dData.show = true
  dData.rules = {
    name: [{ required: true, 'validate-trigger': 'blur', message: t('modeling.components.CU.5m6g1w13h7c0') }],
    id: [{ required: true, 'validate-trigger': 'blur', message: t('modeling.components.CU.5m6g1w13id80') }],
    dataBase: [{ required: true, 'validate-trigger': 'blur', message: t('modeling.components.CU.5m6g1w13hdo0') }],
    type: [{ required: true, 'validate-trigger': 'blur', message: t('modeling.components.CU.5m6g1w13ihs0') }],
    user: [{ required: true, 'validate-trigger': 'blur', message: t('modeling.components.CU.5m6g1w13ik00') }]
  }
  if (type === 'create') {
    dData.formData = JSON.parse(JSON.stringify(dData.formModal))
    dData.title = t('modeling.components.CU.5m6g1w13iaw0')
  } else if (type === 'update' && data) {
    let formData = JSON.parse(JSON.stringify(dData.formModal))
    for (let i in formData) {
      if (data[i]) formData[i] = data[i]
    }
    formData.dataBase = [formData.clusterId, formData.clusterNodeId, formData.dbName, formData.schema]
    data.manager && data.manager.split(',').forEach((item: string) => item && item !== '' && formData.users.push({ name: item, id: formData.users.length + 1, role: 1 }))
    data.developer && data.developer.split(',').forEach((item: string) => item && item !== '' && formData.users.push({ name: item, id: formData.users.length + 1, role: 2 }))
    data.visitor && data.visitor.split(',').forEach((item: string) => item && item !== '' && formData.users.push({ name: item, id: formData.users.length + 1, role: 3 }))
    formData.id = data.id
    dData.title = t('modeling.components.CU.5m6g1w13ing0')
    dData.formData = formData
  }
  getSourceList()
}
const formRef = ref<null | FormInstance>(null)
const close = () => {
  dData.show = false
  nextTick(() => {
    formRef.value?.clearValidate()
    formRef.value?.resetFields()
  })
}
const submit = () => {
  formRef.value?.validate().then(result => {
    if (!result) {
      dData.loading = true
      let sendData = { ...JSON.parse(JSON.stringify(dData.formData)), manager: '', developer: '', visitor: '' }
      sendData.users.forEach((item: KeyValue) => {
        if (Number(item.role) === 1) sendData.manager += sendData.manager ? `,${item.name}` : item.name
        else if (Number(item.role) === 2) sendData.developer += sendData.developer ? `,${item.name}` : item.name
        else if (Number(item.role) === 3) sendData.visitor += sendData.visitor ? `,${item.name}` : item.name
      })
      sendData.clusterId = sendData.dataBase[0]
      sendData.clusterNodeId = sendData.dataBase[1]
      sendData.dbName = sendData.dataBase[2]
      sendData.schema = sendData.dataBase[3]
      sendData.dataBase = 1
      if (sendData.id) {
        dataFlowEdit(sendData).then((res: KeyValue) => {
          dData.loading = false
          if (Number(res.code) === 200) {
            Message.success({ content: t('modeling.components.CU.submitok2') })
            emits(`finish`, 'update')
          }
          close()
        }).catch(() => { dData.loading = false })
      } else {
        dataFlowAdd(sendData).then((res: KeyValue) => {
          dData.loading = false
          if (Number(res.code) === 200) {
            window.$wujie?.props.methods.jump({
              name: `Static-pluginBase-opsModelingDataflowDetail`,
              query: { id: res.data }
            })
            Message.success({ content: t('modeling.components.CU.submitok1') })
            emits(`finish`, 'create')
          }
          close()
        }).catch(() => { dData.loading = false })
      }
    }
  }).catch(() => { dData.loading = false })
}
const userRef = ref<InstanceType<typeof CUUser>>()
const openUserDialog = () => {
  userRef.value?.open(roles, dData.formData.users)
}
const addUserSuccess = (userList: Array<KeyValue>) => {
  dData.formData.users = [...dData.formData.users, ...userList]
}
const deleteUser = (key: number) => {
  dData.formData.users.splice(key, 1)
}
const sourceList = ref<Array<KeyValue>>([])
const getSourceList = () => {
  dataSourceDbList().then((res: KeyValue) => {
    res.data.forEach((item: KeyValue) => {
      item.value = item.clusterId
      item.label = item.clusterId
      item.clusterNodes && item.clusterNodes.forEach((item2: KeyValue) => {
        item2.label = item2.azName !== null ? `${item2.azName}_${item2.publicIp}` : item2.publicIp;
        item2.value = item2.nodeId
        let children = [] as KeyValue[]
        item2.dbName = JSON.parse(item2.dbName)
        item2.dbName.forEach((item3: any) => {
          children.push({
            label: item3,
            value: item3,
            parentId: item2.nodeId
          })
        })
        item2.children = children
      })
      item.children = item.clusterNodes
    })
    sourceList.value = res.data
  })
}
const loadMore = (option: any, done: any) => {
  getSchemeByClusterNodeId(option.value, option.parentId).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      done(res.data.map((item: string) => ({ label: item, value: item, isLeaf: true })))
    } else {
      done([])
    }
  }).catch(() => { done([]) })
}
const cascaderCallback = (data: any) => {
  let arr = []
  let index1 = sourceList.value.findIndex((item: KeyValue) => item.value === data[0])
  if (index1 !== -1) {
    arr.push(sourceList.value[index1].label)
    let index2 = sourceList.value[index1].children.findIndex((item: KeyValue) => item.value = data[1])
    if (index2 === -1) return data.join(' / ')
    else {
      arr.push(sourceList.value[index1].children[index2].label)
      arr.push(data[2])
      arr.push(data[3])
      return arr.join(' / ')
    }
  } else return data.join(' / ')
}
defineExpose({ open })
</script>
<style scoped lang="less">
.cu-container {
  .d-select-user {
    border: 1px solid #002b36;
    padding: 5px 10px;
    box-sizing: border-box;
    width: 100%;
    .user-item {
      display: flex;
      align-items: center;
      height: 30px;
      line-height: 30px;
      box-sizing: border-box;
      margin-bottom: 10px;
      .user-role {
        margin-left: auto;
      }
      .delete {
        margin-left: 5px;
        cursor: pointer;
        width: 20px;
        height: 20px;
        display: flex;
        justify-content: center;
        align-items: center;
        &:hover {
          background-color: rgba(0, 0, 0, 0.1);
          color: red;
          border-radius: 50%;
        }
      }
    }
  }
}
</style>
