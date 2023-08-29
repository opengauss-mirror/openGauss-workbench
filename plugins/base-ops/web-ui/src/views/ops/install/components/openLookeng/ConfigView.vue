<template>
  <div>
    <div class="flex-between">
      <p class="label-color">{{ $t('components.openLooKeng.5mpiji1qpcc37') }}</p>
      <a-button type="primary" :loading="loading" @click="generateYaml">{{
          $t('components.openLooKeng.5mpiji1qpcc51')
        }}
      </a-button>
    </div>
    <Codemirror
      v-model:value="code"
      :options="cmOptions"
      border
      ref="cmRef"
      height="500px"
      width="100%"
      @ready="onReady"
    >
    </Codemirror>
  </div>
</template>
<script lang="ts" setup>
import { inject, onMounted, onUnmounted, ref } from 'vue'
import 'codemirror/mode/yaml/yaml.js'
import type { CmComponentRef } from 'codemirror-editor-vue3'
import Codemirror from 'codemirror-editor-vue3'
import type { Editor, EditorConfiguration } from 'codemirror'
import 'codemirror/theme/monokai.css'
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

const cmRef = ref<CmComponentRef>()
const cmOptions: EditorConfiguration = {
  mode: 'text/x-yaml',
  theme: 'monokai',
  lineNumbers: true,
  lineWrapping: true
}

const onReady = (cm: Editor) => {
  cm.focus()
}

onMounted(() => {
  setTimeout(() => {
    cmRef.value?.refresh()
  }, 1000)
  setTimeout(() => {
    cmRef.value?.cminstance.isClean()
  }, 3000)
  generateYaml()
})

onUnmounted(() => {
  cmRef.value?.destroy()
})

defineExpose({
  beforeConfirm
})
</script>
<style lang="less">
.codemirror-container {
  font-size: 16px !important;
}
</style>
