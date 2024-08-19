<template>
  <div class="step1-container">
    <div class="form-con">
      <div class="form-left" >
        <a-card bordered style="width: 400px;">
          <template #title>
            <div class="card-title-con">
              <div class="card-title">{{$t('step1.index.5q091ixiemk0')}}</div>
              <div class="sql-sel-con">
                <a-input v-model="searchSourceKey" :placeholder="$t('step1.index.5q091ixig500')" allow-clear />
              </div>
              <div class="refresh-con">
                <a-link @click="getSourceClustersData"><icon-refresh /></a-link>
              </div>
            </div>
          </template>
          <template #extra>
            <a-link @click="handleAddSql('MYSQL')">{{$t('step1.index.5q091ixigdc0')}}</a-link>
          </template>
          <div class="sql-tree-con" >
            <a-spin :loading="loadingSource">
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
                    <div v-else class="add-sub-task" style="width:278px;" @mouseover="showButtonSourceDB(nodeData)" @mouseleave="hideButtonSourceDB">
                      <span>{{ nodeData?.title }}</span>
                      <a-button type="primary" size="mini" @click="dataTblWin(nodeData)" v-if="checkBtnShow(nodeData)" class="add-sub-btn">{{$t('step1.index.5q091ixigdc1')}}</a-button>
                    </div>
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
          <span class="selected-info">{{$t('step1.index.5q091ixiggs0')}}</span>
          <span class="selected-db">{{ selectSourceDB.sourceDBName }}</span>
          <span class="selected-info" v-if="tblListShowflag.value > 0" >已选择{{ tblListShowflag.value }}个数据表</span>
        </div>
      </div>
      <div class="form-center">
        <img src="@/assets/images/right-arrow.png" width="50" alt="">
      </div>
      <div class="form-right">
        <a-card bordered style="width: 400px;">
          <template #title>
            <div class="card-title-con">
              <div class="card-title">{{$t('step1.index.5q091ixigjo0')}}</div>
              <div class="sql-sel-con">
                <a-input v-model="searchTargetKey" :placeholder="$t('step1.index.5q091ixig500')" allow-clear />
              </div>
              <div class="refresh-con">
                <a-link @click="getTargetClustersData"><icon-refresh /></a-link>
              </div>
            </div>
          </template>
          <template #extra>
            <a-link @click="handleAddSql('OPENGAUSS')">{{$t('step1.index.5q091ixigdc0')}}</a-link>
          </template>
          <div class="sql-selected-con">
            <a-spin :loading="loadingTarget" style="display: block;">
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
                      {{ nodeData?.title }}
                      <a-button v-if="nodeData?.isLeaf && nodeData?.isSelect" class="add-sub-btn" type="primary" size="mini" @click="addSubTask(nodeData)">{{$t('step1.index.5q091ixigog0')}}</a-button>
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
          <a-table-column :title="$t('step1.index.5q091ixigro0')" data-index="sourceNodeName" :width="200" ellipsis tooltip></a-table-column>
          <a-table-column :title="$t('step1.index.5q091ixigug0')" data-index="sourceDBName" :width="200" ellipsis tooltip></a-table-column>
          <a-table-column :title="$t('step1.index.5q091ixigro1')" data-index="seletedTblNum" :width="150" ellipsis tooltip>
            <template #cell="{ record }">
              <a-button v-if="record.sourceTables && record.sourceTables !== ''" @click="showTblList(record.sourceDBName, record.sourceTables)">{{ record.seletedTblNum }}</a-button>
              <p v-else>{{ $t('step1.index.5q091ixigro3') }}</p>
            </template>
          </a-table-column>
          <a-table-column :title="$t('step1.index.5q091ixigy80')" data-index="targetNodeName" :width="200" ellipsis tooltip></a-table-column>
          <a-table-column :title="$t('step1.index.5q091ixih280')" data-index="targetDBName" :width="200" ellipsis tooltip></a-table-column>
          <a-table-column data-index="mode" :width="130">
            <template #title>
              <div>
                <span>{{$t('step1.index.5q091ixih580')}}</span>
                <a-popover position="top">
                  <icon-question-circle style="cursor: pointer;margin-left: 3px;" size="15" />
                  <template #content>
                    <p>{{$t('step1.index.5q091ixih8g0')}}</p>
                    <p>{{$t('step1.index.5q091ixihtg0')}}</p>
                  </template>
                </a-popover>
              </div>
            </template>
            <template #cell="{ record }">
              <a-select v-model="record.mode" :placeholder="$t('step1.index.5q091ixii200')">
                <a-option :value="1">{{$t('step1.index.5q091ixii5w0')}}</a-option>
                <a-option :value="2">{{$t('step1.index.5q091ixii8o0')}}</a-option>
              </a-select>
            </template>
          </a-table-column>
          <a-table-column data-index="isAdjustKernelParam" :width="200" >
            <template #title>
              <div>
                <span style="margin-left: 60px">{{$t('step1.index.5q091ixih1g0')}}</span>
                <a-popover position="top">
                  <icon-question-circle style="cursor: pointer;margin-left: 3px;" size="15" />
                  <template #content>
                    <p>{{$t('step1.index.5q091ixih2g0')}}</p>
                  </template>
                </a-popover>
              </div>
            </template>
            <template #cell="{ record }">
              <input style="position: center; margin-left: 100px" v-model="record.isAdjustKernelParam" class="checkbox" type = "checkbox" :disabled="isDisable(record.isSystemAdmin)"/>
            </template>
          </a-table-column>
          <a-table-column :title="$t('step1.index.5q091ixiibk0')" align="center" :width="100" fixed="right">
            <template #cell="{ rowIndex }">
              <a-popconfirm :content="$t('step1.index.5q091ixiieo0')" @ok="deleteSubTask(rowIndex)">
                <a-button
                  size="mini"
                  type="text"
                >
                  <template #icon>
                    <icon-delete />
                  </template>
                  <template #default>{{$t('step1.index.5q091ixiihc0')}}</template>
                </a-button>
              </a-popconfirm>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </div>
    <div>
      <!-- add sql -->
      <add-jdbc ref="addJdbcRef" @finish="finishAddJdbc" />
      <dataTblModal v-if="dataTblModalRef" @close="dataTblWinClose" :seleDBMsg="seleDBMsg" @data-selected="handleTableSeleted"> </dataTblModal>
      <dataTblList v-if="dataTblListRef" @close="showTblListClose" :seleDBMsgaft="seleDBMsgaft" > </dataTblList>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, watch, onMounted, toRaw, h, compile } from 'vue'
import { Message } from '@arco-design/web-vue'
import AddJdbc from '../components/AddJdbc.vue'
import { sourceClusters, targetClusters, sourceClusterDbsData, targetClusterDbsData } from '@/api/task'
import useTheme from '@/hooks/theme'
import { useI18n } from 'vue-i18n'
import dataTblModal from './dataTableModal.vue'
import dataTblList from './dataTableList.vue'

const { t } = useI18n()

const { currentTheme } = useTheme()

const props = defineProps({
  subTaskConfig: {
    type: Array,
    default: () => []
  }
})

const emits = defineEmits(['syncConfig'])

const loadingSource = ref(true)
const loadingTarget = ref(true)
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
const seleDBMsg = reactive({
  dbName: '',
  url: '',
  username: '',
  password: '',
  seletedTbl: ''
})

const selectedData = ref([''])
const dataTblModalRef = ref (false)
const selectedTblColumn = ref (false)
const tblListShowflag = ref (0)
const showSourceDBbtn = ref(null)
const showButtonSourceDB = (nodeData) => {
  if (nodeData.title) {
    showSourceDBbtn.value = nodeData.title
  }
}

const checkBtnShow = (nodeData) => {
  if (showSourceDBbtn.value && nodeData && nodeData.title && nodeData.level !== 0 && nodeData.level !== 1) {
    if (showSourceDBbtn.value === nodeData.title) {
      return true
    }
  }
  return false
}

const hideButtonSourceDB = () => {
  showSourceDBbtn.value = -1
}

const selecTblbf = ref('')
const dataTblWin = async (nodeData) => {
  tblListShowflag.value = 0
  seleDBMsg.dbName = nodeData?.title
  if (selectedData.value.length > 0 && selecTblbf && selecTblbf.value && selecTblbf.value === seleDBMsg.dbName) {
    if (selectedData.value[0] === '') {
      seleDBMsg.seletedTbl = '全部'
    } else {
      seleDBMsg.seletedTbl = selectedData.value
    }
  } else if (selectedData.value.length === 0 && selecTblbf && selecTblbf.value && selecTblbf.value === seleDBMsg.dbName) {
    seleDBMsg.seletedTbl = '全部'
  } else {
    selecTblbf.value = ''
    seleDBMsg.seletedTbl = ''
    seleDBMsg.seletedTbl = ''
  }
  if (seleDBMsg.url) {
    dataTblModalRef.value = true
  }
}

const dataTblWinClose = () => {
  dataTblModalRef.value = false
  selectedTblColumn.value = !(selectedData.value.length === 0 && selectedTblColumn.value === false)
}

const handleTableSeleted = (data) => {
  selectedData.value = data.selectedValue
  selecTblbf.value = data.selecTbl
  tblListShowflag.value = selectedData.value.length
  if (tblListShowflag.value > 0 && selectedData.value[0] !== '') {
    selectedTblColumn.value = true
  } else {
    tblListShowflag.value = 0
  }
}

const getTblOpt = async (nodeData) => {
  seleDBMsg.url = nodeData.url
  seleDBMsg.username = nodeData.username
  seleDBMsg.password = nodeData.password
}

const comTbllist = () => {
  let retString = ''
  if (selectedData.value && Array.isArray(selectedData.value) && selectedData.value.length > 0) {
    for (let part of selectedData.value) {
      if (part === '') {
        retString = ''
      } else {
        if (retString.length > 1) {
          retString = retString + ',' + seleDBMsg.dbName + '.' + part
        } else {
          retString = seleDBMsg.dbName + '.' + part
        }
      }
    }
  }
  return retString
}

const comTblLen = () => {
  let retString = ''
  if (selectedData.value && Array.isArray(selectedData.value) && selectedData.value.length > 0) {
    if (selectedData.value[0] === '') {
      retString=t('step1.index.5q091ixigro3')
    } else {
      retString = t('step1.index.5q091ixigro2') + selectedData.value.length
    }
  }
  if (retString === '') {
    retString = t('step1.index.5q091ixigro3')
  }
  return retString
}

const dataTblListRef = ref (false)
const seleDBMsgaft = reactive({
  dbName: '',
  TblList: ''
})

const showTblList = (dbName, TblList) => {
  seleDBMsgaft.dbName = dbName
  seleDBMsgaft.TblList = TblList
  dataTblListRef.value = true
}
const showTblListClose = () => {
  dataTblListRef.value = false
}

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
        title: item.name || t('step1.index.5q091ixiikk0'),
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
        title: item.clusterId || t('step1.index.5q091ixiikk0'),
        key: item.clusterId || item.nodeId,
        icon: () => h(compile('<svg-icon icon-class="cluster" style="font-size: 16px;" />')),
        level: item.clusterId ? 0 : 1,
        children: deepTargetTreeData(item.clusterNodes.map(child => ({ ...child, versionNum: item.versionNum }))),
        ...item
      }
    } else {
      return {
        title: item.publicIp + ':' + item.dbPort,
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
  getTblOpt(nodeData)
  return sourceClusterDbsData({
    url: nodeData.url,
    username: nodeData.username,
    password: nodeData.password
  }).then(res => {
    const data = res.data || []
    nodeData.children = data.map(item => {
      return {
        title: item,
        key: nodeData.ip + item + '',
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
    const data = res.data.filter(item => item.dbName !== 'template0' && item.dbName !== 'template1')
    nodeData.children = data.map(item => {
      return {
        title: item.dbName,
        key: nodeData.publicIp + item.dbName + '',
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
        isLeaf: true,
        isSystemAdmin: nodeData.isSystemAdmin
      }
    })
  })
}

// get source clusters
const getSourceClustersData = () => {
  loadingSource.value = true
  sourceClusters().then(res => {
    loadingSource.value = false
    const data = res.data
    const sourceClusters = deepSourceTreeData(data.sourceClusters)
    sourceTreeData.value = sourceClusters
  }).catch(() => {
    loadingSource.value = false
  })
}

// get target clusters
const getTargetClustersData = () => {
  loadingTarget.value = true
  targetClusters().then(res => {
    loadingTarget.value = false
    const data = res.data
    const targetClusters = deepTargetTreeData(data.targetClusters)
    targetTreeData.value = targetClusters
  }).catch(() => {
    loadingTarget.value = false
  })
}

const finishAddJdbc = (type) => {
  if (type === 'MYSQL') {
    getSourceClustersData()
  } else {
    getTargetClustersData()
  }
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
  if (tableData.value.some(item => (item.targetDBName === targetDB.title && item.targetNodeName === targetDB.parentName))) {
    Message.error('The destination database is selected')
    return
  }
  tableData.value.unshift({
    sourceNodeName: selectSourceDB.sourceNodeName,
    sourceNodeInfo: selectSourceDB.sourceInfo,
    sourceDBName: selectSourceDB.sourceDBName,
    seletedTblNum: comTblLen(),
    sourceTables: comTbllist(),
    targetNodeName: targetDB.parentName,
    targetNodeInfo: targetDB.parentInfo,
    targetDBName: targetDB.title,
    configType: 1,
    isAdjustKernelParam: false,
    isSystemAdmin: targetDB.isSystemAdmin,
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
  selectedData.value = ''
  tblListShowflag.value = 0
  selecTblbf.value = ''
}

const isDisable = (val) => {
  if (val === true) {
    return false
  } else {
    return true
  }
}

// remove sub task
const deleteSubTask = (idx) => {
  tableData.value.splice(idx, 1)
  selecTblbf.value = ''
  if (selectedTblColumn.value) {
    let temptblListShow = false
    for (let db of tableData.value) {
      if (db. selectedTblList !== '全部') {
        temptblListShow = temptblListShow || true
      } else {
        temptblListShow = temptblListShow || false
      }
    }
    selectedTblColumn.value = temptblListShow
  }
}

const addJdbcRef = ref(null)

const handleAddSql = (dbType) => {
  addJdbcRef.value?.open(dbType)
}

// init
const init = (val) => {
  tableData.value = toRaw(val || props.subTaskConfig)
}

defineExpose({
  init
})

onMounted(() => {
  getSourceClustersData()
  getTargetClustersData()
  init()
})

</script>

<style lang="less" scoped>

.right-aligned {
  position: absolute;
  right: 0;
  top: 0;
}

.step1-container {
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
  .form-con {
    display: flex;
    justify-content: center;

    .form-left {
      position: relative;

      .selected-db-con {
        position: absolute;
        z-index: 100;
        left: 1px;
        bottom: -20px;
        height: 40px;
        width: calc(100% - 2px);
        padding: 0 16px;
        border-top: 1px solid var(--color-border-1);
        border-bottom: 1px solid var(--color-border-1);
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
      margin-left: 60px;
      margin-right: 60px;

      .arrow {
        color: var(--color-text-3);
      }
    }

    .card-title-con {
      display: flex;
      align-items: center;

      .card-title {
        font-size: 16px;
        margin-right: 10px;
      }

      .refresh-con {
        margin-left: 5px;
        margin-top: 3px;
        cursor: pointer;
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
          color:red;
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
