<template>
  <div class="step2-container">
    <div class="warn-con">
      <a-alert>
        提示：每个迁移过程都需要有参数配置，DataKit已经为每一个迁移过程（即子任务）配置了默认参数。
        <template #action>
          <a-button size="small" type="primary" @click="handleParamsConfig(1)">默认参数配置(全局配置)</a-button>
        </template>
      </a-alert>
      <a-alert style="margin-top: 10px;">提示：您也可以在下表中修改某个迁移过程（即子任务）的参数配置，来确保迁移过程顺利执行。</a-alert>
    </div>
    <div class="search-con">
      <a-form :model="form" layout="inline">
        <a-form-item field="sourcedbKey">
          <a-input v-model.trim="form.sourcedbKey" allow-clear placeholder="请输入源实例名/库名" @change="getFilterData"></a-input>
        </a-form-item>
        <a-form-item field="targetdbKey">
          <a-input v-model.trim="form.targetdbKey" allow-clear placeholder="请输入目的实例名/库名" @change="getFilterData"></a-input>
        </a-form-item>
        <a-form-item field="mode">
          <a-select v-model="form.mode" placeholder="请选择迁移模式" allow-clear @change="getFilterData">
            <a-option :value="1">离线模式</a-option>
            <a-option :value="2">在线模式</a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="configType">
          <a-select v-model="form.configType" placeholder="请选择配置类型" allow-clear @change="getFilterData">
            <a-option :value="1">默认配置</a-option>
            <a-option :value="2">个性化配置</a-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="outline" @click="getFilterData">
            <template #icon>
              <icon-search />
            </template>
            <template #default>查询</template>
          </a-button>
          <a-button style="margin-left: 10px;" @click="resetQuery">
            <template #icon>
              <icon-sync />
            </template>
            <template #default>重置</template>
          </a-button>
        </a-form-item>
      </a-form>
    </div>
    <div class="table-con">
      <a-table :data="tableData" :bordered="false" stripe :pagination="pagination" @page-change="pageChange">
        <template #columns>
          <a-table-column title="源实例名" data-index="sourceNodeName"></a-table-column>
          <a-table-column title="源IP和端口">
            <template #cell="{ record }">
              {{ record.sourceNodeInfo.host + ':' + record.sourceNodeInfo.port }}
            </template>
          </a-table-column>
          <a-table-column title="源库名" data-index="sourceDBName"></a-table-column>
          <a-table-column title="目的实例名" data-index="targetNodeName"></a-table-column>
          <a-table-column title="目的IP和端口">
            <template #cell="{ record }">
              {{ record.targetNodeInfo.host + ':' + record.targetNodeInfo.port }}
            </template>
          </a-table-column>
          <a-table-column title="目的库名" data-index="targetDBName"></a-table-column>
          <a-table-column title="迁移过程模式" data-index="mode">
            <template #cell="{ record }">
              {{ record.mode === 1 ? "离线模式" : "在线模式" }}
            </template>
          </a-table-column>
          <a-table-column title="操作" align="center" :width="160" fixed="right">
            <template #cell="{ record }">
              <a-button
                size="mini"
                type="text"
                @click="handleParamsConfig(2, record)"
              >
                <template #icon>
                  <icon-edit />
                </template>
                <template #default>编辑配置参数</template>
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
    taskParamsObject: params
  }
  const idx = tableData.value.findIndex(item => item.sourceDBName === subTaskInfo.value.sourceDBName && item.targetDBName === subTaskInfo.value.targetDBName)
  tableData.value.splice(idx, 1, toRaw(subTaskInfo.value))
  emits('syncConfig', toRaw(tableData.value))
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
