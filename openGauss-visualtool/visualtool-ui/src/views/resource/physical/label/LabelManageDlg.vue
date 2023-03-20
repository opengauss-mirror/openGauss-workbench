<template>
  <a-modal :mask-closable="false" :esc-to-close="false" :visible="data.show" :title="data.title" @cancel="close"
    :modal-style="{ width: '650px' }" :footer="false">
    <div class="flex-col-start">
      <div class="flex-between full-w">
        <div>
          <a-button class="mb" type="primary" @click="handleAdd()"><template #icon>
              <icon-plus />
            </template>创建</a-button>
        </div>
        <div>
          <a-form :model="filter" layout="inline">
            <a-form-item label="标签名称">
              <a-input v-model="filter.name" allow-clear placeholder="请输入标签名称" style="width: 180px;"></a-input>
            </a-form-item>
            <a-form-item>
              <a-button type="outline" @click="getHostTagPage()">
                <template #icon>
                  <icon-search />
                </template>
                <template #default>{{ $t('physical.index.5mphf11szdk0') }}</template>
              </a-button>
            </a-form-item>
          </a-form>
        </div>
      </div>
      <a-table class="full-w" :data="list.data" :columns="columns" :loading="list.loading" :pagination="list.page"
        :row-selection="list.rowSelection" @page-change="currentPage" @page-size-change="pageSizeChange" size="mini">
        <template #operation="{ record }">
          <div class="flex-row-start" v-if="record.username !== 'root'">
            <a-link class="mr" @click="handleEdit(record)">编辑</a-link>
            <a-popconfirm :content="$t('components.HostUserMng.5mpi1bru2700')" type="warning"
              :ok-text="$t('components.HostUserMng.5mpi1bru2lo0')"
              :cancel-text="$t('components.HostUserMng.5mpi1bru2s00')" @ok="handleDel(record.id)">
              <a-link status="danger">{{ $t('components.HostUserMng.5mpi1bru2yo0') }}</a-link>
            </a-popconfirm>
          </div>
        </template>
      </a-table>
    </div>
  </a-modal>
  <a-modal :mask-closable="false" :esc-to-close="false" :ok-loading="formData.loading" :visible="formData.show"
    :title="formData.title" :modal-style="{ width: '550px' }" @ok="handleAddOk" @cancel="handleAddClose">
    <a-form :model="formData.form" ref="formRef" auto-label-width :rules="formRules">
      <a-form-item field="name" label="标签名称">
        <a-input v-model="formData.form.name" allow-clear placeholder="请输入标签名称"></a-input>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { computed, reactive, ref, nextTick } from 'vue'
import { hostTagPage, hostTagUpdate, hostTagDel, hostTagAdd } from '@/api/ops' // eslint-disable-line
import { FormInstance } from '@arco-design/web-vue/es/form'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()

const data = reactive<KeyValue>({
  show: false,
  title: t('components.HostUserMng.5mpi1bru3dk0'),
  hostData: {}
})

const filter = reactive<KeyValue>({
  name: '',
  pageNum: 1,
  pageSize: 10
})

const list = reactive<KeyValue>({
  data: [],
  loading: false,
  page: {
    total: 0,
    'show-total': true,
    'show-jumper': true,
    'show-page-size': true
  }
})
const columns = computed(() => [
  { title: '标签名称', dataIndex: 'name', ellipsis: true, tooltip: true },
  { title: '关联主机数', dataIndex: 'relNum' },
  { title: '操作', slotName: 'operation' }
])

const formData = reactive({
  loading: false,
  show: false,
  title: '创建标签',
  form: {
    id: '',
    name: ''
  }
})

const formRules = computed(() => {
  return {
    name: [
      { required: true, message: '请输入标签名称' },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('database.JdbcInstance.5oxhtcbobtc0'))
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

const formRef = ref<null | FormInstance>(null)
const handleAddOk = () => {
  formRef.value?.validate().then(result => {
    if (!result) {
      formData.loading = true
      if (formData.form.id) {
        hostTagUpdate(formData.form.id, { name: formData.form.name }).then((res: KeyValue) => {
          if (Number(res.code) === 200) {
            formData.show = false
            getHostTagPage()
          }
        }).finally(() => {
          formData.loading = false
        })
      } else {
        hostTagAdd(formData.form).then((res: KeyValue) => {
          if (Number(res.code) === 200) {
            formData.show = false
            getHostTagPage()
          }
        }).finally(() => {
          formData.loading = false
        })
      }
    }
  })
}

const handleAddClose = () => {
  nextTick(() => {
    formData.show = false
    formRef.value?.clearValidate()
    formRef.value?.resetFields()
  })
}

const handleAdd = () => {
  formData.show = true
  formData.title = '创建标签'
  formData.form.id = ''
  formData.form.name = ''
}

const handleEdit = (record: KeyValue) => {
  formData.show = true
  formData.title = '编辑标签'
  formData.form.id = record.id
  formData.form.name = record.name
}

const handleDel = (id: string) => {
  list.loading = true
  hostTagDel(id).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      getHostTagPage()
    }
  }).finally(() => {
    list.loading = false
  })
}

const currentPage = (e: number) => {
  filter.pageNum = e
  getHostTagPage()
}

const pageSizeChange = (e: number) => {
  filter.pageSize = e
  getHostTagPage()
}

const emits = defineEmits([`finish`])

const close = () => {
  data.show = false
  filter.name = ''
  filter.pageNum = 1
  filter.pageSize = 10
  emits('finish')
}

const getHostTagPage = () => {
  list.loading = true
  hostTagPage(filter).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      list.data = res.rows
      list.page.total = res.total
    }
  }).finally(() => {
    list.loading = false
  })
}

const open = () => {
  data.show = true
  data.title = '标签管理'
  getHostTagPage()
}
defineExpose({
  open
})
</script>

<style lang="less" scoped></style>
