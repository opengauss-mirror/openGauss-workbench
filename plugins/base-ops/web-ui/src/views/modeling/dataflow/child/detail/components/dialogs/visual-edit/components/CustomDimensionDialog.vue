
<template>
  <a-modal
    class="custom-dimension-dialog-container"
    :visible="visible"
    :title="$t('modeling.components.CustomDimensionDialog.5m7iksonqnk0')"
    :ok-text="$t('modeling.components.CustomDimensionDialog.5m7iksonr9s0')"
    @cancel="close"
    width="1280px"
    :footer="false"
  >
    <a-spin :loading="loading" style="width: 100%; height: 100%;">
      <div class="cddc-content">
        <div class="menu">
          <div class="vm-header">{{$t('modeling.components.CustomDimensionDialog.5m7iksonrew0')}}</div>
          <div class="vm-content">
            <a-dropdown trigger="contextMenu" v-for="(menuItem, menuKey) in list" :key="`menuItem${menuKey}`">
              <a-button
                class="mb-s"
                :type="menuKey === configKey ? 'outline' : 'dashed'"
                @click="configChange(menuItem, menuKey)"
              >
                {{ menuItem.name ? menuItem.name : $t('modeling.components.CustomDimensionDialog.5m7iksonri40') }}
              </a-button>
              <template #content>
                <a-doption @click="deleteListItem(menuItem)">{{$t('modeling.components.CustomDimensionDialog.5m7iksonrkg0')}}</a-doption>
              </template>
            </a-dropdown>
          </div>
          <div class="vm-footer">
            <a-button type="primary" @click="configChange({}, '')"><template #icon><icon-plus /></template>{{$t('modeling.components.CustomDimensionDialog.5m7iksonros0')}}</a-button>
          </div>
        </div>
        <div class="cddc-form">
          <a-row class="mb" align="center">
            <a-col :span="3">{{$t('modeling.components.CustomDimensionDialog.5m7iksonrrg0')}}</a-col>
            <a-col :span="8"><a-input :max-length="140" show-word-limit  v-model="config.name" :placeholder="$t('modeling.components.CustomDimensionDialog.5m7iksonru00')" /></a-col>
          </a-row>
          <a-row class="mb" align="center">
            <a-col :span="3">{{$t('modeling.components.CustomDimensionDialog.5m7iksonrwc0')}}</a-col>
            <a-col :span="8">
              <a-select v-model="config.field" :placeholder="$t('modeling.components.CustomDimensionDialog.5m7iksonryw0')" @change="fieldSelectChange">
                <overflow-tooltip :text="item.label" v-for="(item, key) in stringOption" :key="key" :content="item.label">
                  <a-option :value="item.value"><icon-tag class="mr-s" v-if="item.isCustom" />{{ item.label }}</a-option>
                </overflow-tooltip>
              </a-select>
            </a-col>
          </a-row>
          <a-row align="center" class="mb-s">
            <a-col :span="3">{{$t('modeling.components.CustomDimensionDialog.5m7iksons100')}}</a-col>
            <a-col :span="3"><a-button type="primary" @click="addRow">{{$t('modeling.components.CustomDimensionDialog.5m7iksons3g0')}}</a-button></a-col>
          </a-row>
          <div class="table mb">
            <a-table
              :data="config.categories"
              :columns="columns"
              :scroll="{ y: 500 }"
              :pagination="false"
              @change="handleChange"
              :draggable="{ type: 'handle', width: 40 }"
            >
              <template #name="{ rowIndex }">
                <a-input :max-length="140" v-model="config.categories[rowIndex].name" :placeholder="$t('modeling.components.CustomDimensionDialog.5m7iksons5w0')"></a-input>
              </template>
              <template #type="{ rowIndex }">
                <a-select v-model="config.categories[rowIndex].type" :placeholder="$t('modeling.components.CustomDimensionDialog.5m7iksons8g0')">
                  <a-option :value="1">{{$t('modeling.components.CustomDimensionDialog.5m7iksonsak0')}}</a-option>
                  <a-option :value="2">{{$t('modeling.components.CustomDimensionDialog.5m7iksonscs0')}}</a-option>
                </a-select>
              </template>
              <template #value="{ rowIndex }">
                <a-select
                  v-model="config.categories[rowIndex].value"
                  :placeholder="$t('modeling.components.CustomDimensionDialog.5m7iksonsf40')"
                  multiple
                  allow-clear
                  @dropdown-reach-bottom="rowFieldCurrentPage()"
                >
                  <overflow-tooltip :text="item.label" v-for="(item, key) in valueOption" :key="key" :content="item.label">
                    <a-option :value="item.value">{{ item.label }}</a-option>
                  </overflow-tooltip>
                </a-select>
              </template>
              <template #operate="{ rowIndex }">
                <a-button type="primary" status="danger" size="mini" @click="deleteRow(rowIndex)">{{$t('modeling.components.CustomDimensionDialog.5m7iksonrkg0')}}</a-button>
              </template>
            </a-table>
          </div>
          <div class="cf-footer">
            <a-button type="primary" class="mlauto" @click="saveConfig">{{$t('modeling.components.CustomDimensionDialog.5m7iksonshg0')}}</a-button>
          </div>
        </div>
      </div>
    </a-spin>
  </a-modal>
</template>
<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { reactive, ref } from 'vue'
import OverflowTooltip from '@/views/modeling/components/OverflowTooltip.vue'
import { Message } from '@arco-design/web-vue'
import { useRoute } from 'vue-router'
import { getQueryDataByKey } from '../hooks/options'
import { modelingVCAdd, modelingVCDelete, modelingVCEdit, modelingVCGetListByOperatorId } from '@/api/modeling'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const route = useRoute()
defineProps<{ stringOption: KeyValue[] }>()
const emits = defineEmits([`setCustomList`])
const visible = ref<boolean>(false)
const cellId = ref<string>('')
const loading = ref<boolean>(false)
const open = (id: string) => {
  cellId.value = id
  visible.value = true
  getListData().then(() => {
    if (list.value.length > 0) initConfig(list.value[0], 0)
    else initConfig({}, '')
  })
}
const close = () => {
  visible.value = false
}
const list = ref<KeyValue[]>([])
const configKey = ref<string | number>('')
const getListData = () => new Promise((resolve) => {
  loading.value = true
  modelingVCGetListByOperatorId(cellId.value).then((res: KeyValue) => {
    loading.value = false
    list.value = (res && res.data && Array.isArray(res.data)) ? res.data : []
    emits('setCustomList', (res && res.data && Array.isArray(res.data)) ? res.data : [])
    resolve(true)
  }).catch(() => { loading.value = false })
})
const deleteListItem = (item: KeyValue) => {
  loading.value = true
  modelingVCDelete(item.id).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({ content: t('modeling.components.CustomDimensionDialog.5m7iksonsk00') })
      getListData().then(() => {
        if (list.value.length > 0) initConfig(list.value[0], 0)
        else initConfig({}, '')
      })
    } else loading.value = false
  }).catch(() => { loading.value = false })
}
const configChange = (data: KeyValue, key: number | string) => {
  initConfig(data, key)
}
const saveConfig = () => {
  let sendData = {
    dataFlowId: window.$wujie?.props.data.id,
    operatorId: cellId.value,
    name: config.name,
    field: config.field,
    categories: config.categories
  } as KeyValue
  sendData.categories.forEach((item: KeyValue, key: number) => {
    item.sortId = key
  })
  loading.value = true
  if (config.id) {
    sendData.id = config.id
    modelingVCEdit(sendData).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        Message.success({ content: t('modeling.components.CustomDimensionDialog.5m7iksonsmc0') })
        getListData().then(() => {
          let key = list.value.findIndex((item: KeyValue) => item.id === config.id)
          initConfig(list.value[key], key)
        })
      } else loading.value = false
    }).catch(() => { loading.value = false })
  } else {
    modelingVCAdd(sendData).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        Message.success({ content: t('modeling.components.CustomDimensionDialog.5m7iksonsmc0') })
        getListData().then(() => {
          initConfig(list.value[list.value.length - 1], list.value.length - 1)
        })
      } else loading.value = false
    }).catch(() => { loading.value = false })
  }
}
const columns = [
  { title: t('modeling.components.CustomDimensionDialog.5m7iksonspg0'), dataIndex: 'name', slotName: 'name', width: 180 },
  { title: t('modeling.components.CustomDimensionDialog.5m7iksonsro0'), dataIndex: 'type', slotName: 'type', width: 180 },
  { title: t('modeling.components.CustomDimensionDialog.5m7iksonsu00'), dataIndex: 'value', slotName: 'value' },
  { title: t('modeling.components.CustomDimensionDialog.5m7iksonsvs0'), slotName: 'operate', width: 100 }
]
const valueOption = ref<KeyValue[]>([])
const valueOptionPageInfo = { page: 1, size: 10 }
const config = reactive({
  name: '', field: '', id: '', categories: [] as KeyValue[]
})
const initConfig = (data: KeyValue, key: string | number) => {
  configKey.value = key
  config.name = data.name ? data.name : ''
  config.field = data.field ? data.field : ''
  config.id = data.id ? data.id : ''
  config.categories = data.categoriesJson ? JSON.parse(data.categoriesJson) : []
  if (data.field) rowFieldCurrentPage(1)
}
const deleteRow = (key: number) => {
  config.categories.splice(key, 1)
}
const addRow = () => {
  config.categories.push({ name: '', type: '', value: [] })
}
const fieldSelectChange = () => {
  config.categories.forEach((item: KeyValue) => item.value = [])
  rowFieldCurrentPage(1)
}
const rowFieldCurrentPage = (type?: number) => {
  if (type && type === 1) valueOptionPageInfo.page = 1
  else valueOptionPageInfo.page++
  if (valueOptionPageInfo.page === 1) {
    valueOption.value = getQueryDataByKey(config.field as string, valueOptionPageInfo.page, valueOptionPageInfo.size)
  } else {
    valueOption.value = [
      ...valueOption.value,
      ...getQueryDataByKey(config.field as string, valueOptionPageInfo.page, valueOptionPageInfo.size)
    ]
  }
}
const handleChange = (_data: KeyValue[]) => {
  config.categories = _data
}
defineExpose({ open })
</script>
<style scoped lang="less">
  .custom-dimension-dialog-container {
    .cddc-content {
      display: flex;
      height: 768px;
      .menu {
        width: 160px;
        height: 768px;
        text-align: center;
        transition: all .3s;
        overflow: hidden;
        border: 1px solid var(--color-neutral-4);
        display: flex;
        flex-direction: column;
        &:hover {
          .vm-toggle {
            opacity: 1;
          }
        }
        .vm-header {
          font-weight: bold;
          text-align: start;
          padding: 10px;
          margin-bottom: 10px;
          border-bottom: 1px solid var(--color-neutral-4);
        }
        .vm-content {
          width: 100%;
          flex: 1;
          box-sizing: border-box;
          padding: 10px;
          overflow: auto;
          .visual-menu-item {
            font-size: 14px;
          }
          > * {
            width: 100%;
          }
        }
        .vm-footer {
          margin-top: auto;
          width: 100%;
          button {
            width: 100%;
          }
        }
      }
      .cddc-form {
        flex: 1;
        height: 100%;
        box-sizing: border-box;
        padding: 0 20px;
        .table {
          height: 560px;
        }
        .cf-footer {
          display: flex;
          .mlauto {
            margin-left: auto;
          }
        }
      }
    }
  }
</style>
