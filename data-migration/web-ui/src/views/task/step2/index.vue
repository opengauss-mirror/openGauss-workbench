<template>
  <div class="step2-container">
    <div class="warn-con">
      <a-alert>
        {{$t('step2.index.5q092waasqk0')}}
        <template #action>
          <a-button size="small" type="primary" @click="handleParamsConfig(1)">{{$t('step2.index.5q092waatio0')}}</a-button>
        </template>
      </a-alert>
    </div>
    <div class="search-con">
      <a-form :model="form" layout="inline">
        <a-form-item field="sourcedbKey" style="margin-left: -17px;">
          <a-input v-model.trim="form.sourcedbKey" allow-clear :placeholder="$t('step2.index.5q092waatnk0')" style="width: 180px;"></a-input>
        </a-form-item>
        <a-form-item field="targetdbKey" style="margin-left: -17px;">
          <a-input v-model.trim="form.targetdbKey" allow-clear :placeholder="$t('step2.index.5q092waatqo0')" style="width: 180px;"></a-input>
        </a-form-item>
        <a-form-item field="mode" style="margin-left: -17px;">
          <a-select v-model="form.mode" :placeholder="$t('step2.index.5q092waau3o0')" allow-clear style="width: 160px;">
            <a-option :value="1">{{$t('step2.index.5q092waau8c0')}}</a-option>
            <a-option :value="2">{{$t('step2.index.5q092waaubo0')}}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="configType" style="margin-left: -17px;">
          <a-select v-model="form.configType" :placeholder="$t('step2.index.5q092waaueg0')" allow-clear style="width: 160px;">
            <a-option :value="1">{{$t('step2.index.5q092waauhk0')}}</a-option>
            <a-option :value="2">{{$t('step2.index.5q092waaukc0')}}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item style="margin-left: -17px;">
          <a-button type="outline" @click="getFilterData">
            <template #icon>
              <icon-search />
            </template>
            <template #default>{{$t('step2.index.5q092waaun40')}}</template>
          </a-button>
          <a-button style="margin-left: 10px;" @click="resetQuery">
            <template #icon>
              <icon-sync />
            </template>
            <template #default>{{$t('step2.index.5q092waaw3w0')}}</template>
          </a-button>
        </a-form-item>
      </a-form>
    </div>
    <div class="table-con">
      <a-table :data="tableData" :bordered="false" :stripe="!currentTheme" :hoverable="!currentTheme" :pagination="pagination" @page-change="pageChange">
        <template #columns>
          <a-table-column :title="$t('step2.index.5q092waawag0')" data-index="sourceNodeName"></a-table-column>
          <a-table-column :title="$t('step2.index.5q092waawdo0')" data-index="sourceDBName"></a-table-column>
          <a-table-column :title="$t('step2.index.5q092waawh80')" data-index="targetNodeName"></a-table-column>
          <a-table-column :title="$t('step2.index.5q092waawk00')" data-index="targetDBName"></a-table-column>
          <a-table-column :title="$t('step2.index.5q092wab7280')" data-index="mode">
            <template #cell="{ record }">
              {{ record.mode === 1 ? $t('step2.index.5q092waau8c0') : $t('step2.index.5q092waaubo0') }}
            </template>
          </a-table-column>
          <a-table-column :title="$t('step2.index.5q092wab7fo0')" align="center" :width="160" fixed="right">
            <template #cell="{ record }">
              <a-button
                size="mini"
                type="text"
                @click="handleParamsConfig(2, record)"
              >
                <template #icon>
                  <icon-edit />
                </template>
                <template #default>{{$t('step2.index.5q092wab7k80')}}</template>
              </a-button>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </div>

    <!-- params config -->
    <params-config v-model:open="paramsConfigVisible" :mode="configMode" :global-params="props.globalParams" :count-use-default="countUseDefault" :task-info="subTaskInfo" @syncGlobalParams="syncGlobalParams" @syncTaskParams="syncTaskParams" />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, toRaw } from 'vue'
import ParamsConfig from './components/ParamsConfig.vue'
import useTheme from '@/hooks/theme'

const { currentTheme } = useTheme()

const props = defineProps({
  subTaskConfig: {
    type: Array,
    default: () => []
  },
  globalParams: Object
})

const emits = defineEmits(['syncConfig', 'syncGlobalParams'])

const form = reactive({
  sourcedbKey: undefined,
  targetdbKey: undefined,
  mode: undefined,
  configType: undefined
})

const pagination = reactive({
  total: 0,
  current: 1,
  pageSize: 10
})
const tableData = ref([])

const paramsConfigVisible = ref(false)
const configMode = ref(1)
const countUseDefault = ref(0)
const subTaskInfo = ref({})

const syncGlobalParams = params => {
  emits('syncGlobalParams', params)
}

const syncTaskParams = params => {
  subTaskInfo.value = {
    ...subTaskInfo.value,
    configType: params.basic.length || params.more.length ? 2 : 1,
    taskParamsObject: params
  }
  const originData = toRaw(props.subTaskConfig)
  const idx = originData.findIndex(item => item.sourceDBName === subTaskInfo.value.sourceDBName && item.targetDBName === subTaskInfo.value.targetDBName)
  originData.splice(idx, 1, toRaw(subTaskInfo.value))
  emits('syncConfig', toRaw(originData))
  getFilterData()
}

const handleParamsConfig = (mode, row) => {
  paramsConfigVisible.value = true
  configMode.value = mode
  subTaskInfo.value = row
  const originFilterData = toRaw(props.subTaskConfig).filter(item => !item.taskParamsObject.basic.length && !item.taskParamsObject.more.length) || []
  countUseDefault.value = originFilterData.length
}

const pageChange = current => {
  pagination.current = current
}

// data filter
const getFilterData = () => {
  pagination.current = 1
  const originData = toRaw(props.subTaskConfig)
  tableData.value = originData.filter(item => {
    let flag = true
    if (form.sourcedbKey && !~item.sourceNodeName.indexOf(form.sourcedbKey) && !~item.sourceDBName.indexOf(form.sourcedbKey)) {
      flag = false
    }
    if (form.targetdbKey && !~item.targetNodeName.indexOf(form.targetdbKey) && !~item.targetDBName.indexOf(form.targetdbKey)) {
      flag = false
    }
    if (form.mode && item.mode !== form.mode) {
      flag = false
    }
    if (form.configType && item.configType !== form.configType) {
      flag = false
    }
    return flag
  })
  pagination.total = tableData.value.length
}

const resetQuery = () => {
  form.sourcedbKey = undefined
  form.targetdbKey = undefined
  form.mode = undefined
  form.configType = undefined
  getFilterData()
}

onMounted(() => {
  tableData.value = toRaw(props.subTaskConfig)
  pagination.total = tableData.value.length
})
</script>

<style lang="less" scoped>
.step2-container {
  .warn-con {
    width: 70%;
    margin: 0 auto;
  }
  .search-con {
    margin-top: 20px;
    padding: 0 20px;
  }
  .table-con {
    margin-top: 20px;
    padding: 0 20px 30px;
  }
}
</style>
