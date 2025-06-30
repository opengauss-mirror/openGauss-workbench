<template>
  <div class="common-layout">
    <div class="background-main" v-if="taskBasicInfo.subTaskData && taskBasicInfo.subTaskData.length > 0">
      <el-card class="backgroundcard" style="margin-bottom: 20px;">
        <el-form :model="taskBasicInfo" :rules="taskBasicRules" validateTrigger="onBlur" labelAlign="left"
                 label-width="300px" ref="taskNameFormRef">
          <h3>{{ $t('step1.index.taskConfig') }}</h3>
          <el-form-item :label="t('step1.index.taskName')" prop="taskName">
            <el-input v-model="taskBasicInfo.taskName" class="select-width" :placeholder="t('step1.index.taskNamePlace')"/>
          </el-form-item>
        </el-form>
      </el-card>
      <el-card class="backgroundcard">
        <el-container>
          <el-aside class="tab-aside">
            <h3>{{ $t('step1.index.subTaskConfig') }}</h3>
            <el-tabs v-model:activeName="editableTabsValue" type="card" tab-position="left" @tab-click="handleTabClick"
              class="tab-width, my-tabs" style="--o-tabs-item-max-width: 280px; width: 280px">
              <el-tab-pane v-for="(item, index) in editableTabs" :key="item.name" :label="item.title" :name="item.name">
                <template #label>
                  <div class="tab-label-container"
                       @mouseenter="!popoverVisible && (hoverTabName = item.name)"
                       @mouseleave="!popoverVisible && (hoverTabName = null)">
                    <div style="display: flex; align-items: center; gap: 4px;">
                        <span class="status-dot"
                              :class="{ 'error-dot': item.isValid === false, 'success-dot': item.isValid === true, 'info-dot': item.isValid === null}" />
                      <span>{{ item.title }}</span>
                    </div>
                    <div>
                      <el-popconfirm
                        v-if="hoverTabName === item.name && Number(item.name) >= 1"
                        :title="t('step1.index.deleMsg')"
                        @confirm="removeTab(item.name)"
                        @show="popoverVisible = true"
                        @hide="handlePopoverHide"
                      >
                        <template #reference>
                          <el-icon class="delete-icon"><delete/></el-icon>
                        </template>
                      </el-popconfirm>
                    </div>
                  </div>
                </template>
              </el-tab-pane>
            </el-tabs>
          </el-aside>
          <el-main>
            <el-form :model="taskBasicInfo.subTaskData[curTableTabs]"
                     :ref="(el: any) => taskDataFormRef[curTableTabs] = el"
                     :rules="taskBasicRules" validateTrigger="onBlur"
                     labelAlign="left"
                     label-width="300px"
                     class="page-input-size"
            >
              <h4> {{ $t('step1.index.sourceData') }} </h4>
              <el-form-item :label="t('step1.index.sqlType')" label-position="left" prop="sourceDbType">
                <el-radio-group v-model="taskBasicInfo.subTaskData[curTableTabs].sourceDbType"
                                @change="changeSourceType('select')">
                  <el-radio-button value="MYSQL">MYSQL</el-radio-button>
                  <el-radio-button value="POSTGRESQL">POSTGRESQL</el-radio-button>
                  <el-radio-button value="openGauss" disabled>openGauss</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item :label="t('step1.index.sourceIpPort')" label-position="left" prop="sourceIpPort">
                <el-tree-select
                  v-model="taskBasicInfo.subTaskData[curTableTabs].sourceIpPort"
                  :data="sourceClusterfilterOption"

                  :filter-method="filterSourceMethod"
                  filterable
                  highlight-current
                  @change="getSourceClusterDB('select')"
                  :placeholder="t('step1.index.pleaseSelect')"
                  class="tree-selection"
                />
                <div class="refresh-con">
                  <el-icon>
                    <IconRefresh @click="getSourceClustersData"/>
                  </el-icon>
                  <el-link @click="handleAddSql('source')" type="primary">
                    {{ $t('step1.index.newsource') }}
                  </el-link>
                </div>
                <el-text type="default"> {{ sourceVersionNum }} </el-text>
              </el-form-item>
              <el-form-item :label="t('step1.index.sourceDB')" label-position="left" prop="sourceDBName">
                <el-select v-model="taskBasicInfo.subTaskData[curTableTabs].sourceDBName"
                           :placeholder="t('step1.index.pleaseSelect')" filterable
                           class="select-width"
                           :teleported="false"
                           :rules="[{ required: true, message: t('transcribe.create.required'), trigger: ['blur', 'change'] }]"
                           @change="changeSourceDb">
                  <el-option v-for="option in sourceDBOptions" :key="option.key" :label="option.value"
                             :value="option.value"/>
                </el-select>
              </el-form-item>
              <el-form-item :label="t('step1.index.sourceSchema')" label-position="left" prop="sourceSchema"
                            v-if="taskBasicInfo.subTaskData[curTableTabs].sourceDbType.toUpperCase() === 'POSTGRESQL'">
                <el-select v-model="taskBasicInfo.subTaskData[curTableTabs].sourceSchema"
                           :placeholder="t('step1.index.pleaseSelect')" filterable
                           multiple collapse-tags collapse-tags-tooltip :max-collapse-tags="3"
                           class="select-width"
                           :teleported="false">
                  <el-option v-for="option in sourceSchemaOptions" :key="option.key" :label="option.value"
                             :value="option.value"/>
                </el-select>
              </el-form-item>
              <el-form-item :label="t('step1.index.sourceTable')" label-position="left" prop="sourceTable"
                            v-if="taskBasicInfo.subTaskData[curTableTabs].sourceDbType === 'MYSQL'">
                <el-radio-group v-model="taskBasicInfo.subTaskData[curTableTabs].isSelectAlltables">
                  <el-radio-button value=true @click="changeSeleTbl(true)">{{$t('step1.index.allTable') }}</el-radio-button>
                  <el-radio-button value=false @click="changeSeleTbl(false)">{{$t('step1.index.selectedTable') }}</el-radio-button>
                </el-radio-group>
                <div v-if="taskBasicInfo.subTaskData[curTableTabs].isSelectAlltables === false">
                  {{ $t("step1.index.selectTblNum", {num: taskBasicInfo.subTaskData[curTableTabs].seletedTbl.length}) }}
                </div>
              </el-form-item>
              <h4>{{ $t('step1.index.targetData') }}</h4>
              <el-form-item :label="t('step1.index.targetIpPort')" label-position="left" prop="targetIpPort">
                <el-tree-select
                  v-model="taskBasicInfo.subTaskData[curTableTabs].targetIpPort"
                  :data="targetClusterfilterOption"
                  :load="loadTargetNode"
                  :props="{
                    value: 'value',
                    label: 'label',
                    children: 'children',
                    isLeaf: 'isLeaf'
                  }"
                  lazy
                  :filter-method="filterTargetMethod"
                  filterable
                  @change="getTargetClusterDB('select')"
                  :placeholder="t('step1.index.pleaseSelect')"
                />
                <div class="refresh-con">
                  <el-icon>
                    <IconRefresh @click="getTargetClustersData"/>
                  </el-icon>
                  <el-link @click="handleAddSql('OPENGAUSS')" type="primary">{{ $t('step1.index.newsource') }}</el-link>
                </div>
              </el-form-item>
              <el-form-item :label="t('transcribe.create.targetdb')" prop="targetDBName" label-position="left"
                            :rules="[{ required: true, message: t('transcribe.create.required'), trigger: ['blur', 'change'] }]">
                <el-select v-model="taskBasicInfo.subTaskData[curTableTabs].targetDBName"
                           :placeholder="t('transcribe.create.sourcedb')" filterable class="select-width"
                           :teleported="false"
                           :rules="[{ required: true, message: t('transcribe.create.required'), trigger: ['blur', 'change'] }]">
                  <el-option v-for="option in targetDBOptions" :key="option.key" :label="option.key"
                             :disabled="option.select === false"
                             :value="option.value"/>
                </el-select>
                <el-tooltip class="item" effect="light"
                            :content="t('step1.index.dbTypeContent')"
                            placement="right"
                            :teleported="false">
                  <i class="el-icon icon">
                    <el-icon>
                      <IconHelpCircle/>
                    </el-icon>
                  </i>
                </el-tooltip>
              </el-form-item>
              <h4>{{ $t('step1.index.migrationSet') }}</h4>
              <el-form-item :label="t('step1.index.migrationMode')" label-position="left" prop="mode">
                <el-radio-group v-model="taskBasicInfo.subTaskData[curTableTabs].mode">
                  <el-radio-button value=2>{{ $t('step1.index.online') }}</el-radio-button>
                  <el-radio-button value=1>{{ $t('step1.index.offline') }}</el-radio-button>
                </el-radio-group>
                <el-tooltip class="item" effect="light"
                            :content="t('step1.index.onlineMsg') + t('step1.index.offlineMsg')"
                            placement="right"
                            :teleported="false">
                  <i class="el-icon icon">
                    <el-icon>
                      <IconHelpCircle/>
                    </el-icon>
                  </i>
                </el-tooltip>
              </el-form-item>
              <el-form-item :label="t('step1.index.adjust')" label-position="left">
                <el-switch v-model="taskBasicInfo.subTaskData[curTableTabs].isAdjustKernelParam"
                           :disabled="!taskBasicInfo.subTaskData[curTableTabs].isSystemAdmin"/>
                <el-tooltip class="item" effect="light" :content="t('step1.index.adjustMsg')"
                            placement="right"
                            :teleported="false">
                  <i class="el-icon icon">
                    <el-icon>
                      <IconHelpCircle/>
                    </el-icon>
                  </i>
                </el-tooltip>
              </el-form-item>
              <el-form-item :label="t('step1.index.migrationConfig')" label-position="left"
                            prop="isDefaultRecordConfig">
                <el-radio-group v-model="taskBasicInfo.subTaskData[curTableTabs].isDefaultConfig">
                  <el-radio-button value=true @click="defaultParamsConfig('customized')">
                    {{ $t('step1.index.defaultConfig') }}
                  </el-radio-button>
                  <el-radio-button value=false @click="handleParamsConfig('customized')">
                    {{ $t('step1.index.customConfig') }}
                  </el-radio-button>
                </el-radio-group>
                <div v-if="taskBasicInfo.subTaskData[curTableTabs].isDefaultConfig === true">
                  <span>{{ $t('step1.index.defaultMsg') }}</span>
                </div>
                <div v-else>
                  <span>{{ $t('step1.index.customMsg') }}<el-link
                    type="primary" @click="handleParamsConfig('customized')">{{
                      $t('step1.index.checkCustomParam')
                    }}</el-link>
                    <el-link type="error" @click="defaultParamsConfig('customized')">
                      {{ $t('step1.index.reset') }}
                    </el-link>
                  </span>
                </div>
              </el-form-item>
            </el-form>
            <div style="text-align: left; padding: 10px 0;" v-if="curTableTabs > 1">
              <el-button type="primary" @click="saveSubTask()">{{ $t('step1.index.save') }}</el-button>
              <el-button @click="resetSubTask">{{ $t('step1.index.reset') }}</el-button>
              <el-button v-if="curTableTabs > 0" @click="removeTab(curTableTabs.toString())">{{
                  $t('step1.index.delete')
                }}
              </el-button>
            </div>
          </el-main>
        </el-container>
      </el-card>
    </div>
    <div>
      <params-config v-model:open="paramsConfigVisible" :mode="configMode"
                     :global-params="taskBasicInfo.globalParamsObject" :task-info="subTaskInfo"
                     :default-data="defaultBasicData" @syncGlobalParams="syncGlobalParams"
                     @syncTaskParams="syncTaskParams"/>
      <add-jdbc2 ref="addJdbc2Ref" @finish="finishAddJdbc"/>
      <add-host ref="addHostRef" @finish="labelClose"/>
      <dataTblModal v-if="dataTblModalRef" @close="dataTblWinClose" :seleDBMsg="seleDBMsg"
                    @data-selected="handleTableSeleted">
      </dataTblModal>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, nextTick, onMounted, ref, toRaw, unref, watch} from "vue"
import {checkTargetclusterMaster, sourceClusterDbsType, sourceClusterSchema, sourceClustersType, targetClusterDbsData,
  targetClusters,} from "@/api/playback"
import ParamsConfig from './components/ParamsConfig.vue'
import AddJdbc2 from './components/AddJdbc.vue'
import {IconHelpCircle} from "@computing/opendesign-icons"
import showMessage from "@/utils/showMessage"
import {useI18n} from 'vue-i18n'
import {KeyValue} from "@/types/global";
import {isAdmin} from "@/api/task";
import dayjs from "dayjs";
import {ElTable, ElDialog} from 'element-plus'
import type {Node} from 'element-plus/es/components/tree/src/tree.type'
import dataTblModal from "./components/dataTableModal.vue";
import {Delete, CirclePlus, QuestionFilled} from '@element-plus/icons-vue';
import type {FormInstance} from 'element-plus'
import {encryptPassword, initPublicKey} from "@/utils/jsencrypt"

const {t} = useI18n()

const editableTabsValue = ref('0')
const curTableTabs = computed(() => {
  return Number(editableTabsValue.value)
})
const editableTabs = ref([])

const addTaskTab = () => {
  const editableTabsLen = editableTabs.value.length - 1
  const newTabIndex = Number(editableTabs.value[editableTabsLen].name) + 1
  const newTabName = newTabIndex.toString()
  const newTabTitle = newTabIndex + 1
  const tempTapName = t('step1.index.subTask', {num:newTabTitle.toString()})
  editableTabs.value.push({
    title: tempTapName,
    name: newTabName,
    isValid: null,
  })
  editableTabsValue.value = ((taskBasicInfo.value.subTaskData.length) - 1).toString()
  const newSubTask: subTaskList = initSubTask(newTabName)
  taskBasicInfo.value.subTaskData.push(newSubTask)
}

const hoverTabName = ref(null);
const popoverVisible = ref(false);

const handlePopoverHide = () => {
  popoverVisible.value = false;
  setTimeout(() => {
    if (!popoverVisible.value) {
      hoverTabName.value = null
    }
  }, 100);
}

const removeTab = (tabname: string) => {
  const tabIndex = editableTabs.value.findIndex(tab => tab.name === tabname)
  const dataIndex = taskBasicInfo.value.subTaskData.findIndex(data => data.curretTab === Number(tabname))
  if (tabIndex != -1 && dataIndex != -1) {
    taskBasicInfo.value.subTaskData.splice(dataIndex, 1)
    editableTabs.value.splice(tabIndex, 1)
    if (editableTabsValue.value === tabIndex.toString()) {
      editableTabsValue.value = '0'
    }
  }
}

const handleTabClick = (tab: any) => {
  if (tab.props.name !== curTableTabs.value) {
    editableTabsValue.value = tab.props.name
    preSourceDb.value = taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDB
  }
}

const addHostRef = ref()

const finishAddJdbc = (type: string) => {
  if (type.toUpperCase() === 'MYSQL' || type.toUpperCase() === 'POSTGRESQL') {
    getSourceClustersData()
  } else {
    getTargetClustersData()
  }
}

const configMode = ref(1)
const paramsConfigVisible = ref(false)
const subTaskInfo = ref({})
const handleParamsConfig = async (type: string) => {
  subTaskInfo.value = {...taskBasicInfo.value.subTaskData[curTableTabs.value]}
  type !== 'default' ? configMode.value = 2 : configMode.value = 1
  if (type === 'customized') {
    paramsConfigVisible.value = true
  }
}

const syncGlobalParams = (params: any) => {
  taskBasicInfo.value.globalParamsObject.basic = params.basic
  taskBasicInfo.value.globalParamsObject.more = params.more
}

const syncTaskParams = (params: any) => {
  subTaskInfo.value = {
    ...subTaskInfo.value,
    configType: params.basic.length || params.more.length ? 2 : 1,
    taskParamsObject: params
  }
  const originData = toRaw(taskBasicInfo.value.subTaskData[curTableTabs.value].taskParamsObject)
  const result = {basic: [], more: []};
  ['basic', 'more'].forEach(key => {
    const originMap = new Map(originData[key].map(item => [item.id, item]));
    const paramsMap = new Map(params[key].map(item => [item.id, item]));
    const defaultMap = key === 'basic'
      ? new Map(defaultBasicData.value.map(item => [item.id, item]))
      : new Map();
    const allIds = new Set([
      ...originMap.keys(),
      ...paramsMap.keys(),
      ...defaultMap.keys()
    ]);
    allIds.forEach(id => {
      const paramsItem = paramsMap.get(id);
      const originItem = originMap.get(id);
      const defaultItem = defaultMap.get(id);
      if (key === 'basic' && paramsItem && defaultItem && (paramsItem.paramValue === defaultItem.paramValue)) {
      } else if (key === 'basic' && !paramsItem && defaultItem && originItem && (originItem.paramValue === defaultItem.paramValue)) {
      } else {
        if (paramsItem) {
          result[key].push({...paramsItem});
        } else if (originItem) {
          result[key].push({...originItem});
        }
      }

    });
  });
  taskBasicInfo.value.subTaskData[curTableTabs.value].taskParamsObject = {...result}
}

const defaultParamsConfig = (type: string) => {
  if (type === 'default') {
    taskBasicInfo.value.globalParamsObject.basic.length = 0
    taskBasicInfo.value.globalParamsObject.more.length = 0
  } else {
    taskBasicInfo.value.subTaskData[curTableTabs.value].taskParamsObject.basic.length = 0
    taskBasicInfo.value.subTaskData[curTableTabs.value].taskParamsObject.more.length = 1
  }
}

const addJdbc2Ref = ref(null)
const handleAddSql = (dbType: string) => {
  if (dbType === 'source') {
    const type = taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDbType.toUpperCase()
    addJdbc2Ref.value?.open(type)
  } else {
    addJdbc2Ref.value?.open(dbType)
  }
}

interface subTaskList {
  sourceNodeName: string,
  sourceNodeInfo: {
    port: number,
    host: string,
    password: string,
    username: string,
    nodeId: string
  },
  sourceDBName: string,
  sourceSchema: string[],
  seletedTbl: string[],
  sourceTables: string,
  targetNodeName: string,
  targetNodeInfo: {
    port: number,
    host: string,
    password: string,
    username: string,
    nodeId: string,
    versionNum: string
  },
  targetDBName: string,
  configType: number,
  isAdjustKernelParam: boolean,
  isSystemAdmin: boolean,
  taskParamsObject: {
    basic: {
      paramKey: string;
      paramValue: string;
      paramDesc: string;
    }[],
    more: {
      paramKey: string;
      paramValue: string;
      paramDesc: string;
    }[],
  },
  id: string,

  curretTab: number,
  subTaskName: string,
  sourceDbType: string,
  sourceIpPort: string,
  targetIpPort: string,
  sourceIp: string,
  targetIp: string,
  sourcePort: number,
  targetPort: number,
  selectHost: string
  mode: number,
  isDefaultConfig: boolean,
  isSelectAlltables: boolean
}

interface migrationTaskList {
  taskId: number
  taskName: string
  subTaskData: subTaskList[]
  selectedHosts: string[]
  globalParamsObject: {
    basic: {
      paramKey: string;
      paramValue: string;
      paramDesc: string;
    }[],
    more: {
      paramKey: string;
      paramValue: string;
      paramDesc: string;
    }[],
  }
}

const taskBasicInfo = ref<migrationTaskList>({
  taskId: 0,
  taskName: '',
  subTaskData: [],
  selectedHosts: [],
  globalParamsObject: {
    basic: [],
    more: []
  }
})

const taskBasicRules = computed(() => {
  return {
    taskName: [
      {required: true, trigger: ['blur', 'change'], message: t('transcribe.create.withouttaskname')},
      {max: 255, message: t('transcribe.create.tasknamemsg'), trigger: ['blur', 'change']}
    ],
    taskType: [
      {required: true, trigger: ['blur', 'change'], message: t('transcribe.create.withoutrecordingtype')}
    ],
    sourceIpPort: [
      {required: true, trigger: ['blur', 'change'], message: t('transcribe.create.withoutsourceip')}
    ],
    sourceDBName: [
      {required: true, message: t('transcribe.create.required'), trigger: ['blur', 'change']},
    ],
    sourceSchema: [
      {required: taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDbType.toUpperCase() === 'POSTGRESQL',
        message: t('transcribe.create.required'), trigger: ['blur', 'change']},
    ],
    targetIpPort: [
      {required: true, trigger: ['blur', 'change'], message: t('transcribe.create.withouytargetip')},
    ],
    targetDbName: [
      {required: true, message: t('transcribe.create.required'), trigger: ['blur', 'change']},
    ],
    mode: [
      {required: true, message: t('step1.index.onlineContent'), trigger: ['blur', 'change']},
    ],
  }
})

const changeSourceType = (type?: string) => {
  if (type === 'select') {
    taskBasicInfo.value.subTaskData[curTableTabs.value].sourceIpPort = ''
    taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDBName = ''
    taskBasicInfo.value.subTaskData[curTableTabs.value].seletedTbl = []
    taskBasicInfo.value.subTaskData[curTableTabs.value].sourceSchema = []
    taskBasicInfo.value.subTaskData[curTableTabs.value].isDefaultConfig = true
    defaultParamsConfig('customized')
    preSourceDb.value = ''
  }
  const formRef = taskDataFormRef.value[curTableTabs.value]
  if (formRef) {
    formRef.clearValidate()
  }
  getSourceClustersData()
}

const taskNameFormRef = ref<InstanceType<typeof ElTable> | null>(null)
const taskDataFormRef = ref<(FormInstance | null)[]>([])

interface TreeNode {
  value: string | number
  label: string
  disabled?: boolean
  isLeaf?: boolean
  children?: TreeNode[]
}

const sourceClusterOption = ref<TreeNode[]>([])
const sourceClusterfilterOption = ref<TreeNode[]>([])
const sourceClusterInfo = ref<{ [key: string]: string[] }>({})
const getSourceClustersData = () => {
  const tempDbType = taskBasicInfo.value?.subTaskData?.[curTableTabs.value]?.sourceDbType ?? 'MYSQL';
  sourceClustersType(tempDbType).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      sourceClusterInfo.value = {}
      sourceClusterOption.value = res.data.sourceClusters
        .filter((item: any) => item.dbType.toLowerCase() === taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDbType.toLowerCase())
        .map((item: any) => ({
          value: item.name,
          label: item.name,
          children: item.nodes?.map((nodes: any) => ({
            value: `${nodes.ip}:${nodes.port}`,
            label: `${nodes.ip}:${nodes.port}`,
          }))
        }))
      res.data.sourceClusters.forEach((item: any) => {
        Object.values(item.nodes).forEach((node: any) => {
          const tempname = `${node.ip}:${node.port}`
          const clusterInfo = [
            node.username,
            node.password,
            node.clusterNodeId,
          ]
          if (item.versionNum) {
            clusterInfo.push(item.versionNum)
          }
          sourceClusterInfo.value[tempname] = clusterInfo
        })
      })
      filterSourceMethod('')
    }

  }).catch(error => {
    console.error('加载第一层失败1:', error)
    sourceClusterOption.value = []
  }).finally(() => {
    if (taskBasicInfo.value.subTaskData[curTableTabs.value].sourceIpPort !== '') {
      getSourceClusterDB("init")
    }
  })
}

const sourceVersionNum = computed(() => {
  const ip = taskBasicInfo.value.subTaskData[curTableTabs.value].sourceIpPort
  const clusterInfo = {...sourceClusterInfo.value[ip]} || []
  if (clusterInfo && clusterInfo[3]) {
    const sqlType = taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDbType
    return t('step1.index.dbVersionInfo', {sqlType: sqlType, versionNum: clusterInfo[3]}) + t('step1.index.dbVersionSuc')
  }
  return ''
})


const filterSourceMethod = (value: any) => {
  sourceClusterfilterOption.value = [...sourceClusterOption.value].filter((item) => item.label.includes(value))
}

const targetClusterOption = ref<TreeNode[]>([])
const targetClusterfilterOption = ref<TreeNode[]>([])


const targetClusterInfo = ref<{ [key: string]: string[] }>({})
const tempTargetCluster = ref<{ [key: string]: string[] }>({})

const filterTargetMethod = (value: string) => {
  targetClusterfilterOption.value = [...targetClusterOption.value]
    .filter(item => item.label.includes(value))
    .map(item => ({
      ...item,
      isLeaf: false,
      children: item.children
        ? item.children.filter(child => child.label.includes(value))
          .map(child => ({
            ...child,
            isLeaf: true
          }))
        : []
    }))
}

const getTargetClustersData = () => {
  targetClusters().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      targetClusterOption.value = []
      targetClusterInfo.value = {}
      tempTargetCluster.value = {}
      targetClusterOption.value = res.data.targetClusters.map((item: any) => ({
        value: item.clusterId,
        label: item.clusterId,
        isLeaf: false,
        children: item.clusterNodes?.map((clusterNodes: any) => ({
          value: `${clusterNodes.publicIp}:${clusterNodes.dbPort}`,
          label: `${clusterNodes.publicIp}:${clusterNodes.dbPort}`,
          isLeaf: true,
          disabled: false
        }))
      }))
      res.data.targetClusters.forEach((item: any) => {
        Object.values(item.clusterNodes).forEach((node: any) => {
          const tempname = `${node.publicIp}:${node.dbPort}`;
          const {isSystemAdmin, ...nodeWithoutAdmin} = node;
          targetClusterInfo.value[tempname] = [
            node.dbUser,
            node.dbUserPassword,
            node.nodeId,
            item.versionNum,
          ]
          tempTargetCluster.value[node.publicIp] = nodeWithoutAdmin;
        });
      });
      filterTargetMethod('')
    }

  }).catch(error => {
    console.error('加载第一层失败2:', error)
    targetClusterOption.value = []
  }).finally(() => {
    if (taskBasicInfo.value.subTaskData[curTableTabs.value].targetIpPort !== '') {
      getTargetClusterDB("init")
    }
  })
}

const loadTargetNode = (node: Node, resolve: (data: TreeNode[]) => void) => {
  if (node.level === 0) {
    resolve(targetClusterOption.value)
    return
  }
  else if (node.level === 1) {
    const disabledMap = new Map()
    const tempClusterName = node.data.label
    checkTargetclusterMaster(tempClusterName) .then((res) => {
      if(Number(res.code) === 200) {
        Object.entries(res.data).forEach(([key, value]) => {
          disabledMap.set(key, value)
        })
        const processedChildren = (node.data.children || []).map((child: any) => ({
          ...child,
          disabled: !disabledMap.get(String(child.value))
        }))
        resolve(processedChildren)
      }
    }).catch((error) => {
      console.log(error)
    })
  } else {
    resolve([])
    return
  }
}

const preSourceDb = ref<string>('')

const changeSourceDb = () => {
  if (preSourceDb.value !== '' && preSourceDb.value !== taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDBName) {
    taskBasicInfo.value.subTaskData[curTableTabs.value].seletedTbl.length = 0
    taskBasicInfo.value.subTaskData[curTableTabs.value].isSelectAlltables = true
  }
  preSourceDb.value = taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDBName
  if(taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDbType.toUpperCase() === "POSTGRESQL") {
    getSourceSchema()
    if(taskBasicInfo.value.subTaskData[curTableTabs.value].sourceSchema.length !== 0) {
      taskBasicInfo.value.subTaskData[curTableTabs.value].sourceSchema.length = 0
    }
    const formRef = taskDataFormRef.value[curTableTabs.value]
    if (formRef) {
      formRef.clearValidate()
    }
  }
}

const sourceDBOptions = ref<{ [key: string]: string }[]>([])
const updateNodeInfo = (subTask: any, ip: string, port: number, clusterInfo: any[], type: string) => {
  if (type === 'source') {
    subTask.sourceIp = ip;
    subTask.sourcePort = port;
    subTask.sourceNodeInfo = {
      host: ip,
      port: port,
      username: clusterInfo[0],
      password: clusterInfo[1],
      nodeId: clusterInfo[2]
    };
    return true
  } else {
    subTask.targetIp = ip
    subTask.targetPort = port
    subTask.targetNodeInfo = {
      host: ip,
      port: port,
      username: clusterInfo[0],
      password: clusterInfo[1],
      nodeId: clusterInfo[2],
      versionNum: clusterInfo[3]
    }
    return true
  }
}

const sourceSchemaOptions = ref<{ [key: string]: string }[]>([])
const getSourceSchema = async (type?: string) => {
  sourceSchemaOptions.value = []
  const subTask = taskBasicInfo.value.subTaskData[curTableTabs.value]
  const {sourceIpPort} = subTask
  const [ip, port] = await parseIpPort(sourceIpPort)
  if (!ip || !port) return
  const clusterInfo = {...sourceClusterInfo.value[sourceIpPort]} || []
  await updateNodeInfo(subTask, ip, port, clusterInfo, 'source')
  clusterInfo[1] = await encryptPassword(clusterInfo[1])
  const formData = createFormData(subTask, clusterInfo)
  formData.append('dbName', taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDBName);
  sourceClusterSchema(formData, taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDbType).then((res: any) => {
    if (Number(res.code) === 200) {
      sourceSchemaOptions.value = res.data.map((db: any) => ({key: db, value: db}))
    }
  }).catch((error) => {
    console.error('获取源schema失败:', error)
  })
}

const parseIpPort = (ipPort: string) => {
  const lastColonIndex = ipPort.lastIndexOf(':');
  if (lastColonIndex === -1) return [null, null];
  return [
    ipPort.slice(0, lastColonIndex),
    Number(ipPort.slice(lastColonIndex + 1))
  ]
}

const createFormData = (subTask: any, clusterInfo: any[]) => {
  const formData = new FormData();
  const tempUrl = taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDbType === 'MYSQL' ?
    `jdbc:${subTask.sourceDbType.toLowerCase()}://${subTask.sourceIp}:${subTask.sourcePort}` :
    `jdbc:${subTask.sourceDbType.toLowerCase()}://${subTask.sourceIp}:${subTask.sourcePort}/postgres`
  formData.append('url', tempUrl)
  formData.append('username', clusterInfo[0])
  formData.append('password', clusterInfo[1])
  return formData;
};

const updateDatabaseOptions = (databases: string[], type: string) => {
  if (type === 'source') {
    sourceDBOptions.value = databases.map((db: any) => ({key: db, value: db}))
  } else {
    let type = ''
    if (taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDbType === 'MYSQL') {
      type = 'B'
    } else {
      type='PG'
    }
    targetDBOptions.value = databases.map((db: any) => ({key: db.dbName + '(' +  db.datcompatibility.toUpperCase() + ')',
      value: db.dbName, select: db.isSelect && (db.datcompatibility.toUpperCase() === type)}))
  }
}

const getSourceClusterDB = async (type?: string) => {
  if (taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDBName !== '' && type !== 'init') {
    taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDBName = ''
  }
  sourceDBOptions.value = []
  const subTask = taskBasicInfo.value.subTaskData[curTableTabs.value]
  const {sourceIpPort} = subTask
  const [ip, port] = await parseIpPort(sourceIpPort)
  if (!ip || !port) return
  const clusterInfo = {...sourceClusterInfo.value[sourceIpPort]} || []
  await updateNodeInfo(subTask, ip, port, clusterInfo, 'source')
  clusterInfo[1] = await encryptPassword(clusterInfo[1])
  const formData = createFormData(subTask, clusterInfo)
  sourceClusterDbsType(formData, taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDbType).then((res: any) => {
    if (Number(res.code) === 200) {
      updateDatabaseOptions(res.data, 'source')
    }
  }).catch((error) => {
    console.error('获取源数据库失败:', error)
  })
}

const targetDBOptions = ref<{ [key: string]: string }[]>([])

const getTargetClusterDB = async (type?: string) => {
  if (taskBasicInfo.value.subTaskData[curTableTabs.value].targetDBName !== '' && type !== 'init') {
    taskBasicInfo.value.subTaskData[curTableTabs.value].targetDBName = ''
  }
  targetDBOptions.value = []
  const subTask = taskBasicInfo.value.subTaskData[curTableTabs.value];
  const {targetIpPort} = subTask;
  const [ip, port] = parseIpPort(targetIpPort)
  if (!ip || !port) return;
  const clusterInfo = {...targetClusterInfo.value[targetIpPort]} || [];
  updateNodeInfo(subTask, ip, port, clusterInfo, 'target')
  const tempTarget = {...tempTargetCluster.value[subTask.targetIp]}
  tempTarget.dbUserPassword = await encryptPassword(tempTarget.dbUserPassword)
  targetClusterDbsData(tempTarget).then((res: any) => {
    if (Number(res.code) === 200) {
      updateDatabaseOptions(res.data, 'target')
    }
    checkTargetdbAdmin()
  }).catch((error) => {
    console.error('获取目标数据库失败:', error);
  })
}

const checkTargetdbAdmin = async () => {
  const encryptPwd = await encryptPassword(taskBasicInfo.value.subTaskData[curTableTabs.value].targetNodeInfo.password)
  isAdmin({
    dbPort: taskBasicInfo.value.subTaskData[curTableTabs.value].targetNodeInfo.port,
    dbUser: taskBasicInfo.value.subTaskData[curTableTabs.value].targetNodeInfo.username,
    dbUserPassword: encryptPwd,
    publicIp: taskBasicInfo.value.subTaskData[curTableTabs.value].targetNodeInfo.host
  }).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      taskBasicInfo.value.subTaskData[curTableTabs.value].isSystemAdmin = res.data
    }
  }).catch((error) => {
    console.log(error)
  })
}

const labelClose = () => {
  getSourceClusterDB('select')
}

const emits = defineEmits([
  "update:modelValue"
])

const validateSubTask = async (index: number) => {
  try {
    await taskDataFormRef.value[index]?.validate()
    return true
  } catch (error) {
    return false
  }
}
const saveSubTask = async () => {
  let validRes = true
  if (taskNameFormRef.value) {
    await taskNameFormRef.value.validate((valid: boolean) => {
      validRes = validRes && valid
    })
  } else {
    showMessage('error', '当前页面有未填写项')
    return false
  }
  const isValid = await validateSubTask(curTableTabs.value)
  if (isValid && validRes) {
    const currentTabNum = curTableTabs.value + 1
    showMessage('success', t('step1.index.saveSubtaskSuc', {num: currentTabNum}))
    editableTabs.value[curTableTabs.value].isValid = true
    subtaskValidFlag.value = true
    return true
  } else {
    const currentTabNum = curTableTabs.value + 1
    showMessage('error', t('step1.index.validErrorMsg', {num: currentTabNum}))
    editableTabs.value[curTableTabs.value].isValid = false
    return false
  }
}

const saveAllTask = async () => {
  try {
    let validRes = taskNameFormRef.value ? await taskNameFormRef.value.validate().then(() => true).catch(() => false) : false
    const results = await Promise.all(
      taskDataFormRef.value.map((form, index) =>
        form?.validate().then(() => true).catch(() => false)
      )
    )
    if (results.some(valid => !valid)) {
      showMessage('error', t('step1.index.validErrorMsg', {num: 'curTableTabs.value + 1'}))
      return false
    }
    if (!validRes) {
      showMessage('error', t('step1.index.taskNameMsg'))
      return false
    }
    showMessage('success', t('step1.index.saveSucMsg'))
    emits("update:modelValue", taskBasicInfo.value)
    return true
  } catch (error) {
    console.error('保存失败:', error)
    showMessage('error', t('step1.index.saveErrMsg'))
    return false
  }
}
defineExpose({
  saveAllTask,
})



const changeSeleTbl = (value: boolean) => {
  if (taskBasicInfo.value.subTaskData[curTableTabs.value].sourceIpPort === '' ||
    taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDBName === '') {
    taskBasicInfo.value.subTaskData[curTableTabs.value].isSelectAlltables = true
    showMessage('error', t('step1.index.seleTblMsg'))
  } else {
    taskBasicInfo.value.subTaskData[curTableTabs.value].isSelectAlltables = value
    if (taskBasicInfo.value.subTaskData[curTableTabs.value].isSelectAlltables === true) {
      taskBasicInfo.value.subTaskData[curTableTabs.value].seletedTbl.length = 0
    } else {
      dataTblWin()
    }
  }
}
const seleDBMsg = ref({
  username: '',
  url: '',
  password: '',
  dbName: '',
  seletedTbl: []
})
const dataTblModalRef = ref<boolean>(false)
const dataTblWin = async () => {
  seleDBMsg.value.username = taskBasicInfo.value.subTaskData[curTableTabs.value].sourceNodeInfo.username
  seleDBMsg.value.url = `jdbc:${taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDbType.toLowerCase()}://${taskBasicInfo.value.subTaskData[curTableTabs.value].sourceIp}:${taskBasicInfo.value.subTaskData[curTableTabs.value].sourcePort}`
  seleDBMsg.value.password = taskBasicInfo.value.subTaskData[curTableTabs.value].sourceNodeInfo.password
  seleDBMsg.value.dbName = taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDBName
  seleDBMsg.value.seletedTbl = taskBasicInfo.value.subTaskData[curTableTabs.value].seletedTbl
  dataTblModalRef.value = true
}

const dataTblWinClose = () => {
  dataTblModalRef.value = false
  if(taskBasicInfo.value.subTaskData[curTableTabs.value].seletedTbl.length === 0) {
    taskBasicInfo.value.subTaskData[curTableTabs.value].isSelectAlltables = true
    taskBasicInfo.value.subTaskData[curTableTabs.value].seletedTbl = []
    taskBasicInfo.value.subTaskData[curTableTabs.value].isSelectAlltables === true
    showMessage('info', t('step1.index.selectTblContent'))
  }
}

const handleTableSeleted = (selectedTblCurrent: any) => {
  taskBasicInfo.value.subTaskData[curTableTabs.value].seletedTbl = selectedTblCurrent
  if (selectedTblCurrent.length === 0) {
    taskBasicInfo.value.subTaskData[curTableTabs.value].isSelectAlltables = true
    taskBasicInfo.value.subTaskData[curTableTabs.value].seletedTbl = []
    taskBasicInfo.value.subTaskData[curTableTabs.value].isSelectAlltables === true
    showMessage('info', t('step1.index.selectTblContent'))
  } else {
    taskBasicInfo.value.subTaskData[curTableTabs.value].seletedTbl = selectedTblCurrent
  }
}

const resetSubTask = () => {
  taskBasicInfo.value.subTaskData[curTableTabs.value] = initSubTask(editableTabsValue.value)
  const formRef = taskDataFormRef.value[curTableTabs.value]
  if (formRef) {
    formRef.clearValidate()
  }
}

const initSubTask = (currentTab: string) => {
  const newSubTask: subTaskList = {
    sourceNodeName: '',
    sourceNodeInfo: {
      host: '',
      password: '',
      port: 0,
      username: '',
      nodeId: '',
    },
    sourceDBName: '',
    seletedTbl: [],
    sourceTables: '',
    targetNodeName: '',
    targetNodeInfo: {
      host: '',
      password: '',
      port: 0,
      username: '',
      nodeId: '',
      versionNum: '',
    },
    targetDBName: '',
    configType: 1,
    isAdjustKernelParam: false,
    isSystemAdmin: false,
    taskParamsObject: {
      basic: [],
      more: [{paramKey: "rules.enable", paramValue: "true", paramDesc: "规则过滤，true代表开启，false代表关闭"}],
    },
    id: '',
    curretTab: Number(currentTab),
    subTaskName: `Task_${dayjs().format('YYYYMMDDHHmm')}_${Math.random().toString(36).substring(2, 8)}` + currentTab,
    sourceDbType: 'MYSQL',
    sourceIpPort: '',
    targetIpPort: '',
    sourceIp: '',
    targetIp: '',
    sourcePort: 0,
    targetPort: 0,
    selectHost: '',
    mode: 2,
    isDefaultConfig: true,
    isSelectAlltables: true
  }
  return newSubTask
}

const inittaskBasicInfo = async () => {
  targetDBOptions.value = []
  sourceDBOptions.value = []
  dataTblModalRef.value = false
  editableTabsValue.value = '0'
  taskBasicInfo.value.subTaskData = []
  taskBasicInfo.value.globalParamsObject.basic = []
  const newSubTask: subTaskList = await initSubTask(editableTabsValue.value)
  taskBasicInfo.value.subTaskData.push(newSubTask)
  editableTabs.value.push({
    title: t('step1.index.subTask', {num: '1'}),
    name: '0',
    isValid: null,
  })
}

watch(
  () => {
    const data = taskBasicInfo.value.subTaskData[curTableTabs.value]
    return data ? { ...data } : null
  },
  (newData, oldData) => {
    if (!newData || JSON.stringify(oldData) === JSON.stringify(newData)) return
    if (editableTabs.value[curTableTabs.value]?.isValid === true && subtaskValidFlag.value && oldData !== null) {
      editableTabs.value[curTableTabs.value].isValid = null
    }
  }, { deep: false })

const props = defineProps<{
  modelValue: migrationTaskList,
  defaultBasicData: any
}>()

const subtaskValidFlag = ref(false)
const defaultBasicData = ref()
const init = () => {
  subtaskValidFlag.value = false
  editableTabs.value = []
  taskBasicInfo.value = {...props.modelValue}
  defaultBasicData.value = toRaw(props.defaultBasicData)
  if (taskBasicInfo.value.subTaskData.length === 0) {
    inittaskBasicInfo()
  } else {
    editableTabsValue.value = '0'
    taskBasicInfo.value.subTaskData.forEach((item: subTaskList) => {
      const tabIndex = editableTabs.value.findIndex(tab => tab.name === item.curretTab)
      if (tabIndex === -1) {
        const newTabName = item.curretTab.toString()
        const newTabTitle = item.curretTab + 1
        const tempTapName = t('step1.index.subTask', {num: newTabTitle.toString()})
        editableTabs.value.push({
          title: tempTapName,
          name: newTabName,
          isValid: true,
        })
      }
    })
  }
  subtaskValidFlag.value = true
  preSourceDb.value = taskBasicInfo.value.subTaskData[curTableTabs.value] && taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDB !== '' ?
    taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDB : ''
}

const initialized = ref();
onMounted(() => {
  if (!initialized.value) {
    initialized.value = true
    getSourceClustersData()
    getTargetClustersData()
    init()
  }
})

</script>

<style scoped lang="less">
@import '@/assets/style/openGlobal.less';

.background-main {
  background-color: var(--o-bg-color-light);
  position: absolute;
  width: -webkit-fill-available;

  .h3 {
    color: var(--o-text-color-primary)
  }

  .h4 {
    color: var(--o-text-color-primary)
  }
}

.backgroundform {
  background-color: var(--o-bg-color-base);
  padding-top: 10px;
  padding-bottom: 10px;
  padding-right: 20px;
  padding-left: 20px;
  margin-top: 20px;
}

.backgroundcard {
  :deep(.el-input-number .el-input__inner) {
    text-align: start;
  }

  :deep(.el-tabs__nav.is-left) {
    --o-tabs-item-max-width: 280px !important;
  }
  :deep(.el-tabs__item.is-left) {
    width: 280px !important;
    max-width: 280px !important;
    min-width: 280px !important;
  }

  background-color: var(--o-bg-color-base);
  box-shadow: 0px 0px 0px;
  border: 0;

  :deep( .el-select .el-select__selected-item) {
    line-height: 70px;
  }

  :deep(.tab-label-container) {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 8px;

    > div:first-child {
      flex: 1;
      min-width: 0;
      overflow: hidden;

      .status-dot {
        flex-shrink: 0;
      }

      span {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }

    > div:last-child {
      flex-shrink: 0;
      margin-left: auto;
      margin-right: 20px;
    }
  }
}

.tab-aside {
  width: 300px;
  height: auto;
  display: flex;
  flex-direction: column;
}

.select-width {
  width: 500px;
}

.delete-icon {
  cursor: pointer;
  margin-left: 5px;
}

.my-tabs {
  :deep(.el-tabs__header) {
    background-color: var(--o-bg-color-light);
    color: var(--o-text-color);
    margin: 0;
  }

  :deep(.el-tabs__nav) {
    border: none;
  }

  :deep(.el-tabs__item) {
    display: flex;
    justify-content: left;
    align-items: center;
    padding: 0 !important;
    height: 40px;
    line-height: 40px;
    background-color: var(--o-bg-color-light);
    margin-bottom: 4px;

  }

  :deep(.el-tabs__item.is-active) {
    background: var(--o-color-primary);
    color: var(--o-text-color-fourth);
    border-color: var(--o-color-primary);
  }
}

.custom-tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 16px;
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin: 16px;
}

.info-dot {
  background-color: var(--o-color-info-secondary);
}

.error-dot {
  background-color: var(--o-color-danger);
}

.success-dot {
  background-color: var(--o-color-success);
}

:deep .el-radio-button__orig-radio:checked + .el-radio-button__inner {
  background-color: var(--o-color-primary);
}

.tree-selection {
  :deep(.el-tree-node__content:hover) {
    background-color: black;
  }
}

.page-input-size {
  .el-form-item .el-input,
  .el-form-item .el-select {
    width: 440px;
  }
}
</style>

