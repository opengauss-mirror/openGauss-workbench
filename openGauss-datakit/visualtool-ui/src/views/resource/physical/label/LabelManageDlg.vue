<template>
  <el-dialog v-model="data.show" :title="data.title" @before-close="close" class="w650 labelManageDia" :footer="false" :z-index="960">
    <div class="flex-col-start labelManageDiaBody">
      <div class="topArea">
        <div class="btnArea">
          <el-button class="mb" type="primary" @click="handleAdd()">
            {{ $t('label.LabelManageDlg.addLabel') }}
          </el-button>
          <el-popconfirm :title="$t('components.HostUserMng.5mpi1bru2700')" :visible="showDelPopover"
            @confirm="delSelected" @cancel="delCancel">
            <template #reference>
              <el-button class="multiDel" @click="deleteSelectedUserId">{{ t('physical.index.5mphf11rr590') }}</el-button>
            </template>
          </el-popconfirm>
        </div>
        <div class="searchArea">
          <el-input ref="inputRef" v-model="filter.name" class="o-style-search" :maxlength="100"
            :placeholder="t('label.LabelManageDlg.tagNamePlaceholder')" :prefix-icon="IconSearch"
            clearable @keyup.enter.native="clickSearch" @clear="getHostTagPage"></el-input>
        </div>
      </div>
      <el-table :data="list.data" class="tableArea" @selection-change="handleSelectionChange">
        <template #empty>
          <div class="o-table-empty mt24">
            <el-icon class="o-empty-icon">
              <IconEmpty />
            </el-icon>
            <div class="o-empty-label">
              {{ $t('physical.index.noData') }}
            </div>
          </div>
        </template>
         <el-table-column type="selection" width="36"></el-table-column>
        <el-table-column :label="t('label.LabelManageDlg.5pbjv7b0wu80')" prop="name"
          show-overflow-tooltip></el-table-column>
        <el-table-column :label="t('label.LabelManageDlg.5pbjv7b0xgo0')" prop="relNum"></el-table-column>
        <el-table-column :label="t('label.LabelManageDlg.5pbjv7b0y0w0')">
          <template #default="{ row }">
            <div class="flex-row-start" v-if="row.username !== 'root'">
              <el-button text class="mr" @click="handleEdit(row)">{{ $t('label.LabelManageDlg.5pbjv7b0xak0')
              }}</el-button>
              <el-popconfirm :title="$t('components.HostUserMng.5mpi1bru2700')"
                :ok-text="$t('components.HostUserMng.5mpi1bru2lo0')"
                :cancel-text="$t('components.HostUserMng.5mpi1bru2s00')" @confirm="handleDel(row.id)">
                <template #reference>
                  <el-button text status="danger">{{ $t('components.HostUserMng.5mpi1bru2yo0') }}</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button class="o-dlg-btn" type="primary" size="small" @click="close">{{ t('physical.index.5mphf11t05c0')
        }}</el-button>
        <el-button class="o-dlg-btn" size="small" @click="close">{{ t('physical.index.5mphf11t0bc0') }}</el-button>
      </span>
    </template>
  </el-dialog>
  <el-dialog v-model="formData.show" class="w550 inputLableDiaForm" :z-index="980">
    <template #title>
      <div v-if="formData.form.id">{{ t('label.LabelManageDlg.5pbjv7b0yk40') }}</div>
      <div v-else>{{ t('label.LabelManageDlg.5pbjv7b0yd00') }}</div>
    </template>
    <el-form :model="formData.form" ref="formRef" auto-label-width :rules="formRules">
      <el-form-item field="name" :label="$t('label.LabelManageDlg.5pbjv7b0wu80')">
        <el-input v-model.trim="formData.form.name" allow-clear
          :placeholder="$t('label.LabelManageDlg.5pbjv7b0x3w0')"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button class="o-dlg-btn" type="primary" size="small" @click="handleAddOk">{{
          t('physical.index.5mphf11t05c0') }}</el-button>
        <el-button class="o-dlg-btn" size="small" @click="handleAddClose">{{ t('physical.index.5mphf11t0bc0')
        }}</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { computed, reactive, ref, nextTick } from 'vue'
import { hostTagPage, hostTagUpdate, hostTagDel, hostTagAdd } from '@/api/ops' // eslint-disable-line
import { FormInstance } from '@arco-design/web-vue/es/form'
import { useI18n } from 'vue-i18n'
import { IconEmpty, IconSearch } from '@computing/opendesign-icons'
import { searchType } from '@/types/searchType'
import showMessage from '@/hooks/showMessage'
const { t } = useI18n()
const tagName = ref('');
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


const labelOptions = computed(() => {
  return {
    name: {
      label: t('label.LabelManageDlg.5pbjv7b0wu80') || '13',
      value: 'name',
      placeholder: t('label.LabelManageDlg.5pbjv7b0x3w0'),
      selectType: searchType.INPUT
    }
  }
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
  { title: t('label.LabelManageDlg.5pbjv7b0wu80'), dataIndex: 'name', ellipsis: true, tooltip: true },
  { title: t('label.LabelManageDlg.5pbjv7b0xgo0'), dataIndex: 'relNum' },
  { title: t('label.LabelManageDlg.5pbjv7b0y0w0'), slotName: 'operation' }
])

const formData = reactive({
  loading: false,
  show: false,
  title: t('label.LabelManageDlg.5pbjv7b0yd00'),
  form: {
    id: '',
    name: ''
  }
})

const selectedIdList = ref<KeyValue[]>([])

const showDelPopover = ref(false)

const delSelected = async () => {
  showDelPopover.value = false
  // 遍历删除选中的标签
  for (const selectedId of selectedIdList.value) {
    await handleDel(selectedId)
  }
  nextTick(() => {
    getHostTagPage()
    selectedIdList.value = []
  })
}

const delCancel = () => {
  showDelPopover.value = false
}

const handleSelectionChange = async (rows: KeyValue[]) => {
  // rows为当前选中的用户，遍历删除即可
  selectedIdList.value = rows.map(row => row.id)
}

const deleteSelectedUserId = () => {
  if (selectedIdList.value.length === 0) {
    showMessage('warning', t('label.LabelManageDlg.delWarning'))
    showDelPopover.value = false
  } else {
    showDelPopover.value = true
  }
}

const clickSearch = () => {
  // 调用接口搜索
  getHostTagPage();
}

const formRules = computed(() => {
  return {
    name: [
      { required: true, message: t('label.LabelManageDlg.5pbjv7b0x3w0') },
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
  if (!formData.form.name) {
    return;
  }
  formRef.value?.validate(valid => {
    if (valid) {
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
  formData.title = t('label.LabelManageDlg.5pbjv7b0yd00')
  formData.form.id = ''
  formData.form.name = ''
}

const handleEdit = (record: KeyValue) => {
  formData.show = true
  formData.title = t('label.LabelManageDlg.5pbjv7b0yk40')
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
  data.title = t('label.LabelManageDlg.5pbjv7b0yso0')
  getHostTagPage()
}
defineExpose({
  open
})
</script>

<style lang="less">
.labelManageDia {
  .el-dialog__body {
    max-height: 560px;

    .topArea {
      width: 100%;
      margin-bottom: 16px;

      .btnArea {
        display: flex;
        gap: 8px;
        height: 32px;
        margin-bottom: 16px;

        .el-button {
          height: 100%;
        }
      }

      .labelSearchForm {
        display: flex;
        gap: 8px;

        .el-form-item {
          margin-bottom: 12px;
        }
      }
    }

    .el-table {
      min-height: 132px;
      max-height: 354px;
      display: flex;
      overflow: auto;
    }

    .inputLableDiaForm {
      .el-dialog__header {
        height: 54px;
      }
    }
  }
  .o-style-search.el-input {
    .el-input__suffix {
      &::after {
        margin-top: 8px;
      }
    }
  }
}
</style>
