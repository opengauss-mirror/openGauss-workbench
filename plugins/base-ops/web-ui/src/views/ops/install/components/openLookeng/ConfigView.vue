<template>
  <div>
    <div class="flex-between">
      <p class="label-color">{{ $t('components.openLooKeng.5mpiji1qpcc37') }}</p>
      <a-button type="primary" :loading="loading" @click="generateYaml">{{
          $t('components.openLooKeng.5mpiji1qpcc51')
        }}
      </a-button>
    </div>
    <codemirror
      v-model="code"
      :extensions="extensions"
      :style="{ height: '500px', border: '1px solid #ddd' }"
      @ready="onReady"
      :autofocus="true"
      :indent-with-tab="true"
      :tab-size="2"
    />
  </div>
</template>
<script lang="ts" setup>
import { inject, onMounted, onUnmounted, ref } from 'vue'
import { Codemirror } from 'vue-codemirror'
import { yaml } from '@codemirror/lang-yaml'
import { oneDark } from '@codemirror/theme-one-dark'
import { lineNumbers } from '@codemirror/view'
import { EditorView } from '@codemirror/view'
import { useOpsStore } from '@/store'
import { dataSourceDbList } from '@/api/modeling'
import { KeyValue } from '@/types/global'
import { OpenLookengInstallConfig, ShardingDsConfig } from '@/types/ops/install'
import { generateRuleYaml } from '@/api/ops'
import { Message } from '@arco-design/web-vue'

const beforeConfirm = async (): Promise<boolean> => {
  installStore.setOpenLookengConfig({ ruleYaml: code.value } as OpenLookengInstallConfig)
  return true
}

const installStore = useOpsStore()
const loadingFunc = inject<any>('loading')
const code = ref('')
const loading = ref(false)
const editorView = ref<EditorView>()

const extensions = [
  yaml(),
  lineNumbers(),
  oneDark,
  EditorView.lineWrapping
]

const onReady = (payload: { view: EditorView; container: HTMLDivElement }) => {
  editorView.value = payload.view
  payload.view.focus()
}

const generateYaml = async () => {
  loadingFunc.toLoading()
  loading.value = true
  const reqData = await buildReqData()
  if (reqData) {
    generateRuleYaml(reqData).then(res => {
      code.value = res.data
    }).finally(() => {
      loadingFunc.cancelLoading()
      loading.value = false
    })
  } else {
    loadingFunc.cancelLoading()
    loading.value = false
  }
}

const buildReqData = async () => {
  const ds = installStore.openLookengInstallConfig.dsConfig
  const res = await dataSourceDbList()
  const dbList: Array<ShardingDsConfig> = []
  if (ds.length <= 0) {
    Message.error('No datasource is selected, please select datasource and try again')
    return
  }
  if (res.data.length <= 0) {
    Message.error('No openGauss cluster is installed, please install a openGauss cluster and try again')
    return
  }
  ds.map((arr: Array<string>) => {
    const clusterId = arr[0]
    const nodeId = arr[1]
    const databaseName = arr[2]
    const cluster = res.data.find((item: KeyValue) => item.clusterId === clusterId)
    if (cluster) {
      const node = cluster.clusterNodes.find((item: KeyValue) => item.nodeId === nodeId)
      if (node && node.dbName.indexOf(databaseName) >= 0) {
        dbList.push({
          dbName: databaseName,
          port: node.dbPort,
          host: node.publicIp,
          username: node.dbUser,
          password: node.dbUserPassword
        } as ShardingDsConfig)
      }
    }
  })
  return {
    dsConfig: dbList,
    tableName: installStore.openLookengInstallConfig.tableName,
    columns: installStore.openLookengInstallConfig.columns
  }
}

onMounted(() => {
  generateYaml()
})

onUnmounted(() => {
  if (editorView.value) {
    editorView.value.destroy()
  }
})

defineExpose({
  beforeConfirm
})
</script>

<style lang="less">
.cm-editor {
  font-size: 16px !important;
  height: 500px;

  &.cm-focused {
    outline: none;
  }
}
</style>
