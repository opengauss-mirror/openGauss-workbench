
<template>
  <a-tabs
    class="d-a-tabs"
    v-model:activeKey="iData.activeKey" centered size="mini" :tabBarGutter="40"
    :tabBarStyle="{ color: '#fff' }"
  >
    <a-tab-pane key="1" :title="$t('modeling.special.DataSource.5m7adso1d000')">
      <div class="tab-content d-a-form">
        <a-form-item :label="$t('modeling.special.DataSource.5m7adso1dv00')" :labelCol="{ span: 6, offset: 0 }" labelAlign="left" :colon="false">
          <a-cascader
            path-mode
            v-model="config.source"
            :options="sourceList"
            :placeholder="$t('modeling.special.DataSource.5m7adso1e040')"
            :load-more="loadMore"
            :fallback="cascaderCallback"
            @change="(value: any) => selectChange(value, 'source')"
          />
        </a-form-item>
        <a-row >
          <a-col :span="3"></a-col>
          <a-col :span="18">
            <div class="d-tips">
              <icon-exclamation-circle-fill />
              {{$t('modeling.special.DataSource.5m7adso1e580')}}
            </div>
          </a-col>
        </a-row>
      </div>
    </a-tab-pane>
  </a-tabs>
  <a-modal
    :visible="warning.show"
    :ok-loading="warning.loading"
    :closable="false"
    simple
    :ok-text="$t('modeling.special.DataSource.5m7adso1e7s0')"
    :cancel-text="$t('modeling.special.DataSource.5m7adso1eag0')"
    @ok="warningOk"
    @cancel="warningClose"
  >
    <div class="warning">
      <div class="title"><icon-exclamation-circle-fill color="#ff7d00" />{{$t('modeling.special.DataSource.5m7adso1eeo0')}}</div>
      <div class="content">{{$t('modeling.special.DataSource.5m7adso1ehg0')}}</div>
    </div>
  </a-modal>
</template>
<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import {
  Tabs as ATabs, TabPane as ATabPane, FormItem as AFormItem
} from '@arco-design/web-vue'
import { Cell } from '@antv/x6'
import { useDataFlowStore } from '@/store/modules/modeling/data-flow'
import { KeyValue } from '@/api/modeling/request'
import { dataSourceDbList, getSchemeByClusterNodeId } from '@/api/modeling'
const dFStore = useDataFlowStore()
const useDatabase = computed(() => dFStore.getUseDatabase)
let cell: Cell | null = null
const init = (pCell: Cell) => {
  cell = pCell
  let { data } = pCell
  config.clusterId = data.clusterId ? data.clusterId : ''
  config.clusterNodeId = data.clusterNodeId ? data.clusterNodeId : ''
  config.schema = data.schema ? data.schema : ''
  config.dbName = data.dbName ? data.dbName : ''
  config.source = data.source ? data.source : []
  warning.oldValue = data.source ? data.source : ''
}
const iData = reactive({
  activeKey: `1`
})
const warning = reactive({
  show: false, loading: false, oldValue: ''
})
const warningOpen = () => warning.show = true
const warningClose = (isFunc: boolean) => {
  warning.show = false
  if (!isFunc) config.source = warning.oldValue
}
const warningOk = () => {
  warning.loading = true
  dFStore.clearUse()
  dFStore.setDatabaseInfo(config.source[2], config.source[1], config.source[3]).then(() => {
    warning.loading = false
    warning.oldValue = JSON.parse(JSON.stringify(config.source))
    warningClose(true)
    saveData('source', config.source)
    saveData('clusterId', config.source[0])
    saveData('clusterNodeId', config.source[1])
    saveData('dbName', config.source[2])
    saveData('schema', config.source[3])
  }).catch(() => {
    dFStore.clearUse()
    warning.loading = false
    config.source = ''
    warning.oldValue = JSON.parse(JSON.stringify(config.source))
    warningClose(true)
    saveData('source', config.source)
    saveData('clusterId', config.source[0])
    saveData('clusterNodeId', config.source[1])
    saveData('dbName', config.source[2])
    saveData('schema', config.source[3])
  })
}
const selectChange = (value: any, prop: any) => {
  if (prop === 'source') {
    if ((useDatabase.value !== `${value[1]}|,|${value[2]}|,|${value[3]}`) || !config.clusterId) {
      if (value && value.length > 0) {
        warningOpen()
      }
    }
  }
}
/** configuration parameters */
const config: KeyValue = reactive({
  source: [], clusterId: '', clusterNodeId: '', schema: '', dbName: ''
})
/** When filling in the content, save it in the data of the node itself */
const saveData = (key: string, value: any) => {
  if (cell) {
    let cellData = cell.getData()
    cell?.setData({ ...cellData, [key]: JSON.parse(JSON.stringify(value)) }, { overwrite: true })
  }
}
/** Data source list */
const sourceList = ref<KeyValue[]>([])
const getListData = () => {
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
getListData()
const loadMore = (option: any, done: any) => {
  getSchemeByClusterNodeId(option.value, option.parentId).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      done(res.data.map((item: string) => ({ label: item, value: item, isLeaf: true })))
    } else {
      done()
    }
  }).catch(() => { done() })
}
const cascaderCallback = (data: any) => {
  let arr = []
  let index1 = sourceList.value.findIndex((item: KeyValue) => item.value === data[0])
  if (index1 !== -1) {
    arr.push(sourceList.value[index1].label)
    let index2 = sourceList.value[index1].children.findIndex((item: KeyValue) => item.value = data[1])
    if (index2 === -1) {
      return data.join(' / ')
    } else {
      arr.push(sourceList.value[index1].children[index2].label)
      arr.push(data[2])
      arr.push(data[3])
      return arr.join(' / ')
    }
  } else {
    return data.join(' / ')
  }
}
defineExpose({ init })
</script>
<style scoped lang="less">
  .tab-content {
    padding: 0 10px;
    .d-title-1 {
      font-size: 14px;
      margin-bottom: 20px;
    }
  }
  .warning {
    .title {
      text-align: center;
      font-size: 20px;
      color: #ff7d00;
      margin-bottom: 10px;
      > svg {
        margin-right: 10px;
      }
    }
    .content {
      font-size: 14px;
    }
  }
  .d-tips {
    color: var(--color-neutral-6);
  }
  .arco-tabs {
    overflow: visible;
  }
  .d-a-tabs {
    :deep(.arco-tabs-content) {
      overflow: visible !important;
      .arco-tabs-content-item  {
        overflow: visible;
      }
    }
  }
</style>
