<template>
  <div class="step1-container">
    <div class="form-con">
      <div class="form-left">
        <a-card bordered style="width: 400px;">
          <template #title>
            <div class="card-title-con">
              <div class="card-title">源端</div>
              <div class="sql-sel-con">
                <a-input v-model="searchSourceKey" placeholder="输入名称搜索" allow-clear />
              </div>
            </div>
          </template>
          <template #extra>
            <a-link @click="handleAddSql">+自定义数据库</a-link>
          </template>
          <div class="sql-tree-con">
            <a-spin :loading="loading" style="display: block;">
              <a-tree :data="treeSourceData" v-model:selected-keys="selectedSourceKey" blockNode :check-strictly="true" :load-more="getSourceClusterDbsData" :default-expand-all="false" @select="sourceNodeSelect">
                <template #title="nodeData">
                  <!-- eslint-disable -->
                  <template v-if="index = getMatchSourceIndex(nodeData?.title), index < 0">
                    <a-popover v-if="nodeData?.level === 1">
                      <span>{{ nodeData?.title }}</span>
                      <template #content>
                        <p>Host Name: {{ nodeData?.name || '-' }}</p>
                        <p>Host IP: {{ nodeData?.ip }}</p>
                        <p>Port: {{ nodeData?.port }}</p>
                      </template>
                    </a-popover>
                    <span v-else>{{ nodeData?.title }}</span>
                  </template>
                  <span v-else>
                    {{ nodeData?.title?.substr(0, index) }}
                    <span style="color: var(--color-primary-light-4);">
                      {{ nodeData?.title?.substr(index, searchSourceKey.length) }}
                    </span>{{ nodeData?.title?.substr(index + searchSourceKey.length) }}
                  </span>
                </template>
              </a-tree>
              <a-empty v-if="!sourceTreeData.length" />
            </a-spin>
          </div>
        </a-card>
        <div v-if="selectSourceDB.sourceDBName" class="selected-db-con">
          <span class="selected-info">已选源端数据库：</span>
          <span class="selected-db">{{ selectSourceDB.sourceDBName }}</span>
        </div>
      </div>
      <div class="form-center">
        <icon-arrow-right size="40" />
      </div>
      <div class="form-right">
        <a-card bordered style="width: 400px;">
          <template #title>
            <div class="card-title-con">
              <div class="card-title">目的端</div>
              <div class="sql-sel-con">
                <a-input v-model="searchTargetKey" placeholder="输入名称搜索" allow-clear />
              </div>
            </div>
          </template>
          <template #extra>
            <a-link @click="handleAddSql">+自定义数据库</a-link>
          </template>
          <div class="sql-selected-con">
            <a-spin :loading="loading" style="display: block;">
              <a-tree :data="treeTargetData" v-model:selected-keys="selectedTargetKey" blockNode :check-strictly="true" :load-more="getTargetClusterDbsData" :default-expand-all="false">
                <template #title="nodeData">
                  <!-- eslint-disable -->
                  <template v-if="index = getMatchTargetIndex(nodeData?.title), index < 0">
                    <a-popover v-if="nodeData?.level === 1">
                      <span>{{ nodeData?.title }}</span>
                      <template #content>
                        <p>Host Name: {{ nodeData?.hostname || '-' }}</p>
                        <p>Host IP: {{ nodeData?.publicIp }}</p>
                        <p>Port: {{ nodeData?.hostPort }}</p>
                      </template>
                    </a-popover>
                    <div v-else class="add-sub-task">
                      <span>{{ nodeData?.title }}</span>
                      <a-button v-if="nodeData?.isLeaf && nodeData?.isSelect" class="add-sub-btn" type="primary" size="mini" @click="addSubTask(nodeData)">添加子任务</a-button>
                    </div>
                  </template>
                  <span v-else>
                    {{ nodeData?.title?.substr(0, index) }}
                    <span style="color: var(--color-primary-light-4);">
                      {{ nodeData?.title?.substr(index, searchTargetKey.length) }}
                    </span>{{ nodeData?.title?.substr(index + searchTargetKey.length) }}
                  </span>
                </template>
              </a-tree>
              <a-empty v-if="!targetTreeData.length" />
            </a-spin>
          </div>
        </a-card>
      </div>
    </div>
    <div class="table-con">
      <a-table :data="tableData" :bordered="false" :stripe="!currentTheme" :hoverable="!currentTheme" :pagination="false">
        <template #columns>
          <a-table-column title="源实例名" data-index="sourceNodeName" :width="200" ellipsis tooltip></a-table-column>
          <a-table-column title="源库名" data-index="sourceDBName" :width="200" ellipsis tooltip></a-table-column>
          <a-table-column title="目的实例名" data-index="targetNodeName" :width="200" ellipsis tooltip></a-table-column>
          <a-table-column title="目的库名" data-index="targetDBName" :width="200" ellipsis tooltip></a-table-column>
          <a-table-column data-index="mode" :width="130">
            <template #title>
              <div>
                <span>迁移过程模式</span>
                <a-popover position="top">
                  <icon-question-circle style="cursor: pointer;margin-left: 3px;" size="15" />
                  <template #content>
                    <p>离线模式：自动执行全量迁移，完成后自动结束，释放资源。</p>
                    <p>在线模式：自动执行全量迁移+增量迁移，用户手动启动反向迁移，需要用户操作结束迁移，释放资源。</p>
                  </template>
                </a-popover>
              </div>
            </template>
            <template #cell="{ record }">
              <a-select v-model="record.mode" placeholder="请选择">
                <a-option :value="1">离线模式</a-option>
                <a-option :value="2">在线模式</a-option>
              </a-select>
            </template>
          </a-table-column>
          <a-table-column title="操作" align="center" :width="100" fixed="right">
            <template #cell="{ rowIndex }">
              <a-popconfirm content="你确认删除此任务吗？" @ok="deleteSubTask(rowIndex)">
                <a-button
                  size="mini"
                  type="text"
                >
                  <template #icon>
                    <icon-delete />
                  </template>
                  <template #default>删除</template>
                </a-button>
              </a-popconfirm>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </div>

    <!-- add sql -->
    <add-sql v-model:open="addSqlVisible" />
  </div>
</template>

<script setup>
import { reactive, ref, computed, watch, onMounted, toRaw, h, compile } from 'vue'
import { Message } from '@arco-design/web-vue'
import AddSql from '../components/AddSql.vue'
import { clustersData, sourceClusterDbsData, targetClusterDbsData } from '@/api/task'
import useTheme from '@/hooks/theme'

const { currentTheme } = useTheme()

const props = defineProps({
  subTaskConfig: {
    type: Array,
    default: () => []
  }
})

const emits = defineEmits(['syncConfig'])

const loading = ref(true)
const sourceTreeData = ref([])
const targetTreeData = ref([])
const searchSourceKey = ref('')
const searchTargetKey = ref('')
const selectedSourceKey = ref([])
const selectedTargetKey = ref([])
const selectSourceDB = reactive({
  sourceInfo: '',
  sourceNodeName: '',
  sourceDBName: ''
})

const tableData = ref([])

const addSqlVisible = ref(false)

const treeSourceData = computed(() => {
  if (!searchSourceKey.value) return sourceTreeData.value
  return searchTreeData(searchSourceKey.value, sourceTreeData.value)
})

const treeTargetData = computed(() => {
  if (!searchTargetKey.value) return targetTreeData.value
  return searchTreeData(searchTargetKey.value, targetTreeData.value)
})

const searchTreeData = (keyword, searchData) => {
  const loop = (data) => {
    const result = []
    data.forEach(item => {
      if (item.title.toLowerCase().indexOf(keyword.toLowerCase()) > -1) {
        result.push({ ...item })
      } else if (item.children) {
        const filterData = loop(item.children)
        if (filterData.length) {
          result.push({
            ...item,
            children: filterData
          })
        }
      }
    })
    return result
  }

  return loop(searchData)
}

const getMatchSourceIndex = (title) => { // eslint-disable-line
  if (!searchSourceKey.value) return -1
  return title.toLowerCase().indexOf(searchSourceKey.value.toLowerCase())
}

const getMatchTargetIndex = (title) => { // eslint-disable-line
  if (!searchTargetKey.value) return -1
  return title.toLowerCase().indexOf(searchTargetKey.value.toLowerCase())
}

// selected source db
const sourceNodeSelect = (selectedKeys, data) => {
  if (data.node.isLeaf) {
    selectSourceDB.sourceInfo = data.node.parentInfo,
    selectSourceDB.sourceNodeName = data.node.parentName
    selectSourceDB.sourceDBName = data.node.title
  } else {
    selectedSourceKey.value = []
    selectSourceDB.sourceInfo = {}
    selectSourceDB.sourceNodeName = ''
    selectSourceDB.sourceDBName = ''
  }
}

const deepSourceTreeData = (data) => {
  return data.map((item) => {
    if (item?.nodes?.length > 0) {
      return {
        title: item.name || '默认',
        key: item.clusterId || item.clusterNodeId,
        icon: () => h(compile('<svg-icon icon-class="cluster" style="font-size: 16px;" />')),
        level: item.clusterId ? 0 : 1,
        children: deepSourceTreeData(item.nodes),
        ...item
      }
    } else {
      return {
        title: item.ip + ':' + item.port,
        key: item.clusterId || item.clusterNodeId,
        icon: () => h(compile(`<svg-icon icon-class="${item.clusterId ? 'cluster' : 'mysql'}" style="font-size: 22px;" />`)),
        level: item.clusterId ? 0 : 1,
        ...item
      }
    }
  })
}

const deepTargetTreeData = (data) => {
  return data.map((item) => {
    if (item?.clusterNodes?.length > 0) {
      return {
        title: item.clusterId || '默认',
        key: item.clusterId || item.nodeId,
        icon: () => h(compile('<svg-icon icon-class="cluster" style="font-size: 16px;" />')),
        level: item.clusterId ? 0 : 1,
        children: deepTargetTreeData(item.clusterNodes.map(child => ({ ...child, versionNum: item.versionNum }))),
        ...item
      }
    } else {
      return {
        title: item.publicIp + ':' + item.hostPort,
        key: item.clusterId || item.nodeId,
        icon: () => h(compile(`<svg-icon icon-class="${item.clusterId ? 'cluster' : 'opengaussdb'}" />`)),
        level: item.clusterId ? 0 : 1,
        ...item
      }
    }
  })
}

// get source db data
const getSourceClusterDbsData = (nodeData) => {
  return sourceClusterDbsData({
    url: nodeData.url,
    username: nodeData.username,
    password: nodeData.password
  }).then(res => {
    const data = res.data || []
    nodeData.children = data.map(item => {
      return {
        title: item,
        key: item,
        icon: () => h(compile('<svg-icon icon-class="database" style="font-size: 16px;" />')),
        parentName: nodeData.title,
        parentInfo: {
          host: nodeData.ip,
          password: nodeData.password,
          port: nodeData.port,
          username: nodeData.username,
          nodeId: nodeData.clusterNodeId
        },
        isLeaf: true
      }
    })
  })
}

// get target db data
const getTargetClusterDbsData = (nodeData) => {
  console.log(nodeData)
  return targetClusterDbsData({
    azAddress: nodeData.azAddress,
    azName: nodeData.azName,
    clusterRole: nodeData.clusterRole,
    dbName: nodeData.dbName,
    dbPort: nodeData.dbPort,
    dbUser: nodeData.dbUser,
    dbUserPassword: nodeData.dbUserPassword,
    hostId: nodeData.hostId,
    hostPort: nodeData.hostPort,
    hostname: nodeData.hostname,
    installUserName: nodeData.installUserName,
    nodeId: nodeData.nodeId,
    privateIp: nodeData.privateIp,
    publicIp: nodeData.publicIp,
    rootPassword: nodeData.rootPassword
  }).then(res => {
    const data = res.data || []
    nodeData.children = data.map(item => {
      return {
        title: item.dbName,
        key: item.dbName,
        icon: () => h(compile('<svg-icon icon-class="database" style="font-size: 16px;" />')),
        parentName: nodeData.title,
        parentInfo: {
          host: nodeData.publicIp,
          password: nodeData.dbUserPassword,
          port: nodeData.dbPort,
          username: nodeData.dbUser,
          versionNum: nodeData.versionNum,
          nodeId: nodeData.nodeId
        },
        isSelect: item.isSelect,
        isLeaf: true
      }
    })
  })
}

// get cluster data
const getClustersData = () => {
  loading.value = true
  clustersData().then(res => {
    loading.value = false
    const data = res.data
    const sourceClusters = deepSourceTreeData(data.sourceClusters)
    sourceTreeData.value = sourceClusters
    const targetClusters = deepTargetTreeData(data.targetClusters)
    targetTreeData.value = targetClusters
  }).catch(() => {
    loading.value = false
  })
}

watch(tableData, (val) => {
  emits('syncConfig', val)
}, { deep: true })

// add sub task data
const addSubTask = (targetDB) => {
  if (!selectSourceDB.sourceDBName) {
    Message.error('The source database cannot be empty')
    return
  }
  if (tableData.value.some(item => item.targetDBName === targetDB.title)) {
    Message.error('The destination database is selected')
    return
  }
  tableData.value.unshift({
    sourceNodeName: selectSourceDB.sourceNodeName,
    sourceNodeInfo: selectSourceDB.sourceInfo,
    sourceDBName: selectSourceDB.sourceDBName,
    targetNodeName: targetDB.parentName,
    targetNodeInfo: targetDB.parentInfo,
    targetDBName: targetDB.title,
    configType: 1,
    taskParamsObject: {
      basic: [],
      more: []
    }
  })
  selectSourceDB.sourceInfo = {}
  selectSourceDB.sourceNodeName = ''
  selectSourceDB.sourceDBName = ''
  selectedSourceKey.value = []
  selectedTargetKey.value = []
}

// remove sub task
const deleteSubTask = (idx) => {
  tableData.value.splice(idx, 1)
}

const handleAddSql = () => {
  addSqlVisible.value = true
}

// init
const init = (val) => {
  tableData.value = toRaw(val || props.subTaskConfig)
}

defineExpose({
  init
})

onMounted(() => {
  getClustersData()
  init()
})
</script>

<style lang="less" scoped>
.step1-container {
  .form-con {
    display: flex;
    justify-content: center;
    .form-left {
      position: relative;
      .selected-db-con {
        position: absolute;
        z-index: 100;
        left: 1px;
        bottom: 1px;
        height: 40px;
        width: calc(100% - 2px);
        padding: 0 16px;
        border-top: 1px solid var(--color-border-1);
        display: flex;
        align-items: center;
        background-color: var(--color-bg-2);
        .selected-info {
          color: var(--color-text-1);
        }
        .selected-db {
          color: rgb(var(--primary-6));
        }
      }
    }
    .form-center {
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      margin-left: 10px;
      margin-right: 10px;
    }
    .card-title-con {
      display: flex;
      align-items: center;
      .card-title {
        font-size: 16px;
        margin-right: 10px;
      }
    }
    .sql-tree-con {
      height: 350px;
      padding-bottom: 30px;
      overflow-y: auto;
    }
    .sql-selected-con {
      height: 350px;
      overflow-y: auto;
      .sql-group {
        margin-bottom: 15px;
        .sql-group-title {
          font-size: 14px;
          color: var(--color-text-1);
        }
        .sql-item-con {
          margin-top: 5px;
          .sql-item {
            font-size: 12px;
            padding: 5px 8px;
            border: 1px solid var(--color-border);
            border-radius: 2px;
            margin-bottom: 5px;
          }
        }
      }
      :deep(.arco-tree-node-title) {
        height: 24px;
        .arco-tree-node-title-text {
          flex: 1;
        }
        .add-sub-task {
          display: flex;
          justify-content: space-between;
          align-items: center;
          .add-sub-btn {
            display: none;
          }
        }
        &:hover {
          .add-sub-task {
            .add-sub-btn {
              display: block;
            }
          }
        }
      }
    }
  }
  .table-con {
    margin-top: 20px;
    padding: 0 20px 30px;
    .opt-con {
      display: flex;
      justify-content: flex-end;
      margin-bottom: 10px;
    }
  }
}
</style>
