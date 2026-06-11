<template>
  <div class="common-layout">
    <div class="background-main" v-if="taskBasicInfo.subTaskData && taskBasicInfo.subTaskData.length > 0">
      <el-card class="backgroundcard" style="margin-bottom: 20px;">
        <el-form :model="taskBasicInfo" :rules="taskBasicRules" validateTrigger="onBlur" labelAlign="left"
                 label-width="300px" ref="taskNameFormRef">
          <h3>{{ $t('step1.index.taskConfig') }}</h3>
          <el-form-item :label="t('step1.index.taskName')" prop="taskName">
            <el-input v-model.trim="taskBasicInfo.taskName" class="select-width" :placeholder="t('step1.index.taskNamePlace')"/>
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
                     class="page-input-size openDesignForm"
            >
              <h4> {{ $t('step1.index.sourceData') }} </h4>
              <el-form-item :label="t('step1.index.sqlType')" label-position="left" prop="sourceDbType">
                <el-radio-group v-model="taskBasicInfo.subTaskData[curTableTabs].sourceDbType"
                                @change="changeSourceType('select')">
                  <el-radio-button :value="JDBCType.MySQL" :label="JDBCType.normalize(JDBCType.MySQL)" />
                  <el-radio-button :value="JDBCType.PostgreSQL" :label="JDBCType.normalize(JDBCType.PostgreSQL)" />
                  <el-radio-button :value="JDBCType.openGauss" disabled :label="JDBCType.normalize(JDBCType.openGauss)" />
                  <el-radio-button :value="JDBCType.Elasticsearch" :label="JDBCType.normalize(JDBCType.Elasticsearch)" />
                  <el-radio-button :value="JDBCType.Milvus" :label="JDBCType.normalize(JDBCType.Milvus)" />
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
                <div v-if="sourceVersionNum" class="spacing-left">
                  <el-text type="info">
                    {{ $t('step1.index.dbVersionInfo', { sqlType: taskBasicInfo.subTaskData[curTableTabs].sourceDbType, versionNum: sourceVersionNum}) }}
                  </el-text>
                  <el-text type="info" v-if="checkSourceclusterVersion"> {{ $t('step1.index.dbVersionSuc') }} </el-text>
                  <el-text type="danger" v-else> {{ $t('step1.index.dbVersionFail') }} </el-text>
                </div>
                <div v-else-if="taskBasicInfo.subTaskData[curTableTabs].sourceIpPort !== ''" class="spacing-left">
                  <el-text type="info">{{ $t('step1.index.getVersion') }}</el-text>
                  <el-link type="primary" @click="handleGetVersion('source')">
                    {{ $t('step1.index.getVersionBtn') }}
                  </el-link>
                </div>
              </el-form-item>
              <div v-if="taskBasicInfo.subTaskData[curTableTabs].sourceDbType !== JDBCType.Elasticsearch">
                <el-form-item :label="t('step1.index.sourceDB')" label-position="left" prop="sourceDBName">
                  <el-select v-model="taskBasicInfo.subTaskData[curTableTabs].sourceDBName"
                             :placeholder="t('step1.index.pleaseSelect')" filterable
                             class="select-width"
                             :teleported="false"
                             :rules="[{ required: true, message: t('transcribe.create.required'), trigger: ['blur', 'change'] }]"
                             @change="changeSourceDb"
                             :disabled="taskBasicInfo.subTaskData[curTableTabs].sourceDbType === JDBCType.Milvus"
                  >
                    <el-option v-for="option in sourceDBOptions" :key="option.key" :label="option.value"
                               :value="option.value"/>
                  </el-select>
                </el-form-item>

                <div v-if="taskBasicInfo.subTaskData[curTableTabs].sourceDbType.toUpperCase() === JDBCType.PostgreSQL">
                  <el-form-item :label="t('step1.index.sourceSchema')" label-position="left" prop="sourceSchema"
                                :rules="[{ required: true, message: t('transcribe.create.required'), trigger: ['blur', 'change'] }]">
                    <el-select v-model="taskBasicInfo.subTaskData[curTableTabs].sourceSchema"
                               :placeholder="t('step1.index.pleaseSelect')" filterable
                               multiple collapse-tags collapse-tags-tooltip :max-collapse-tags="3"
                               class="select-width" :teleported="false">
                      <el-option v-for="option in sourceSchemaOptions" :key="option.key" :label="option.label"
                                 :value="option.value"/>
                    </el-select>
                  </el-form-item>
                </div>
                <div v-else-if="taskBasicInfo.subTaskData[curTableTabs].sourceDbType === JDBCType.MySQL">
                  <el-form-item :label="t('step1.index.sourceTable')" label-position="left" prop="sourceTable">
                    <el-radio-group v-model="taskBasicInfo.subTaskData[curTableTabs].isSelectAlltables">
                      <el-radio-button :value="true" @click="changeSeleTbl(true)">{{$t('step1.index.allTable') }}</el-radio-button>
                      <el-radio-button :value="false" @click="changeSeleTbl(false)">{{$t('step1.index.selectedTable') }}</el-radio-button>
                    </el-radio-group>
                    <div v-if="!taskBasicInfo.subTaskData[curTableTabs].isSelectAlltables">
                      {{ $t("step1.index.selectTblNum", {num: seleTblNum}) }}
                    </div>
                  </el-form-item>
                </div>
                <div v-else-if="taskBasicInfo.subTaskData[curTableTabs].sourceDbType === JDBCType.Milvus">
                  <el-form-item :label="t('step1.index.collection')" label-position="left" prop="sourceTables">
                    <div v-if="seleTblNum !== 0" class="spacing-right">
                      {{ $t("step1.index.selectTblNum", {num: seleTblNum}) }}
                    </div>
                    <div class="link-group">
                      <el-link type="primary" @click="changeSeleTbl(false)"
                               :disabled="!taskBasicInfo.subTaskData[curTableTabs].sourceIpPort">{{ t('step1.index.collectionSelect') }}</el-link>
                      <el-link @click="changeSeleTbl(true)">{{ t('step1.index.clearSelection') }}</el-link>
                    </div>
                  </el-form-item>
                </div>
              </div>
              <div v-else>
                <el-form-item :label="t('step1.index.index')" label-position="left" prop="sourceTables">
                  <div v-if="seleTblNum !== 0" class="spacing-right">
                    {{ $t("step1.index.selectTblNum", {num: seleTblNum}) }}
                  </div>
                  <div class="link-group">
                    <el-link type="primary" @click="changeSeleTbl(false)"
                             :disabled="!taskBasicInfo.subTaskData[curTableTabs].sourceIpPort">{{ t('step1.index.indexSelect') }}</el-link>
                    <el-link @click="changeSeleTbl(true)">{{ t('step1.index.clearSelection') }}</el-link>
                  </div>
                </el-form-item>
              </div>
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
                  <el-link @click="handleAddSql(JDBCType.openGauss)" type="primary">{{ $t('step1.index.newsource') }}</el-link>
                </div>
                <div v-if="targetVersionNum" class="spacing-left">
                  <el-text type="info">
                    {{ $t('step1.index.dbVersionInfo', { sqlType: JDBCType.normalize('OPENGAUSS'), versionNum: targetVersionNum}) }}
                  </el-text>

                  <el-text type="info" v-if="checkTargetclusterVersion"> {{ $t('step1.index.dbVersionSuc') }} </el-text>
                  <el-text type="danger" v-else> {{ $t('step1.index.dbVersionFail') }} </el-text>
                </div>
                <div v-else-if="taskBasicInfo.subTaskData[curTableTabs].targetIpPort !== ''" class="spacing-left">
                  <el-text type="info">{{ $t('step1.index.getVersion') }}</el-text>
                  <el-link type="primary" @click="handleGetVersion('target')">
                    {{ $t('step1.index.getVersionBtn') }}
                  </el-link>
                </div>
              </el-form-item>
              <el-form-item :label="t('transcribe.create.targetdb')" prop="targetDBName" label-position="left"
                            :rules="[{ required: true, message: t('transcribe.create.required'), trigger: ['blur', 'change'] }]">
                <el-select v-model="taskBasicInfo.subTaskData[curTableTabs].targetDBName"
                           :placeholder="t('transcribe.create.sourcedb')" filterable class="select-width"
                           :teleported="false"
                           :rules="[{ required: true, message: t('transcribe.create.required'), trigger: ['blur', 'change'] }]">
                  <el-option v-for="option in targetDBOptions" :key="option.key" :label="option.key"
                             :disabled="!option.select"
                             :value="option.value"/>
                </el-select>
                <el-tooltip class="item" effect="light"
                            :content="t('step1.index.dbTypeContent')"
                            placement="bottom"
                            :popper-style="{ maxWidth: '45vw', width: 'auto' }"
                            :teleported="false">
                  <i class="el-icon icon spacing-left">
                    <el-icon>
                      <IconHelpCircle/>
                    </el-icon>
                  </i>
                </el-tooltip>
              </el-form-item>
              <h4>{{ $t('step1.index.migrationSet') }}</h4>
              <div v-if="shouldShowDbColumn">
                <el-form-item :label="t('step1.index.migrationMode')" label-position="left" prop="mode">
                  <el-select v-model="taskBasicInfo.subTaskData[curTableTabs].mode"
                             :placeholder="t('transcribe.create.mode')"
                             filterable
                             class="select-width"
                             :teleported="false">
                    <el-option :value="3" :label="$t('step1.index.offlineWithoutCheck')"></el-option>
                    <el-option :value="1" :label="$t('step1.index.offline')" v-if="checkOptionVisible"></el-option>
                    <el-option :value="4" :label="$t('step1.index.onlineWithoutCheck')"></el-option>
                    <el-option :value="2" :label="$t('step1.index.online')" v-if="checkOptionVisible"></el-option>
                  </el-select>
                  <el-tooltip class="item" effect="light"
                              :content="t('step1.index.modeMsg')"
                              placement="bottom"
                              :popper-style="{ maxWidth: '5vw', width: 'auto' }"
                              :teleported="false">
                    <i class="el-icon icon spacing-left">
                      <el-icon>
                        <IconHelpCircle/>
                      </el-icon>
                    </i>
                  </el-tooltip>
                </el-form-item>
              </div>
              <el-form-item
                v-if="shouldShowMigrationObject"
                :label="t('step1.index.migrationObject')"
                label-position="left"
              >
                <el-switch
                  v-model="taskBasicInfo.subTaskData[curTableTabs].isMigrationObject"
                />
                <div v-if="shouldShowMigrationObjectPrompt" style="margin-left: 6px;">
                  <el-text class="mx-1" type="warning"> {{ $t('step1.index.migrationObjectPrompt') }} </el-text>
                </div>
              </el-form-item>
              <el-form-item :label="t('step1.index.adjust')" label-position="left">
                <el-switch v-model="taskBasicInfo.subTaskData[curTableTabs].isAdjustKernelParam"
                           :disabled="!taskBasicInfo.subTaskData[curTableTabs].isSystemAdmin"/>
                <el-tooltip class="item" effect="light" :content="t('step1.index.adjustMsg')"
                            placement="bottom"
                            :popper-style="{ maxWidth: '10vw', width: 'auto' }"
                            :teleported="false">
                  <i class="el-icon icon" style="margin-left: 6px;">
                    <el-icon>
                      <IconHelpCircle/>
                    </el-icon>
                  </i>
                </el-tooltip>
              </el-form-item>
              <el-form-item :label="t('step1.index.migrationConfig')" label-position="left"
                            prop="isDefaultRecordConfig">
                <el-radio-group v-model="taskBasicInfo.subTaskData[curTableTabs].isDefaultConfig">
                  <el-radio-button :value='true' @click="defaultParamsConfig('customized')">
                    {{ $t('step1.index.defaultConfig') }}
                  </el-radio-button>
                  <el-radio-button :value='false' @click="handleParamsConfig('customized')">
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
                    <el-link type="danger" @click="defaultParamsConfig('customized')">
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
      <params-config v-model:open="paramsConfigVisible" :mode="configMode" :task-info="subTaskInfo"
                     :default-data="defaultBasicData" @syncTaskParams="syncTaskParams"/>
      <add-jdbc2 ref="addJdbc2Ref" @finish="finishAddJdbc"/>
      <add-host ref="addHostRef" @finish="labelClose"/>
      <dataTblModal  ref="dataTblModalRef" @close="dataTblWinClose" :seleDBMsg="seleDBMsg"
                     @data-selected="handleTableSeleted">
      </dataTblModal>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref, toRaw, watch} from "vue"
import {
  checkTargetclusterMaster, clusterVersioNnum,
  sourceClusterDbsType,
  sourceClusterSchema,
  sourceClustersType,
  targetClusterDbs,
  targetClustersType,
} from "@/api/task"
import ParamsConfig from './components/ParamsConfig.vue'
import AddJdbc2 from './components/AddJdbc.vue'
import {IconHelpCircle} from "@computing/opendesign-icons"
import showMessage from "@/utils/showMessage"
import {useI18n} from 'vue-i18n'
import {KeyValue} from "@/types/global";
import {isAdmin} from "@/api/task";
import dayjs from "dayjs";
import {ElTable, ElDialog, ElForm, FormItemRule, FormRules} from 'element-plus'
import type { Node } from 'element-plus/es/components/tree/src/tree'
import dataTblModal from "./components/dataTableModal.vue";
import {Delete, CirclePlus, QuestionFilled} from '@element-plus/icons-vue';
import type {FormInstance} from 'element-plus'
import {JDBCType} from "@/types/jdbc";

const {t} = useI18n()

const editableTabsValue = ref('0')
const curTableTabs = computed(() => {
  return Number(editableTabsValue.value)
})
const editableTabs = ref<Array<{
  title: string
  name: string
  isValid: boolean | null
}>>([])

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

const hoverTabName = ref<string | null>(null)
const popoverVisible = ref(false);

const handlePopoverHide = () => {
  popoverVisible.value = false;
  setTimeout(() => {
    if (!popoverVisible.value) {
      hoverTabName.value = null
    }
  }, 100);
}

const shouldShowMigrationObjectPrompt = computed(() => {
  const sourceDbType = taskBasicInfo.value.subTaskData[curTableTabs.value]?.sourceDbType;
  const targetIpPort = taskBasicInfo.value.subTaskData[curTableTabs.value]?.targetIpPort;

  if (sourceDbType === JDBCType.PostgreSQL
    || !taskBasicInfo.value.subTaskData[curTableTabs.value]?.isMigrationObject) {
    return false
  }

  if (sourceDbType === JDBCType.MySQL) {
    if (!targetIpPort || targetIpPort.trim() === '') {
      return false
    }

    return targetIpPort && targetIpPort.trim() !== ''
      && !taskBasicInfo.value.subTaskData[curTableTabs.value]?.isSystemAdmin
  }
  return false
})

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
    preSourceDb.value = taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDBName
  }
}

const addHostRef = ref()

const finishAddJdbc = (type: string) => {
  if (type.toUpperCase() !== JDBCType.openGauss) {
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

const syncTaskParams = (params: any) => {
  subTaskInfo.value = {
    ...subTaskInfo.value,
    configType: params.basic.length || params.more.length ? 2 : 1,
    taskParamsObject: params
  }
  const result: { basic: ParamItem[], more: ParamItem[] } = { basic: [], more: [] };
  (['basic', 'more'] as const).forEach((key: 'basic' | 'more') => {
    const paramsMap = new Map<string, ParamItem>(params[key].map((item: ParamItem) => [item.paramKey, item]));
    const defaultMap = key === 'basic'
      ? new Map<string, ParamItem>(defaultBasicData.value.map((item: ParamItem) => [item.paramKey, item]))
      : new Map<string, ParamItem>();
    const allIds = new Set([
      ...paramsMap.keys(),
      ...defaultMap.keys()
    ])
    allIds.forEach(id => {
      const paramsItem = paramsMap.get(id)
      const defaultItem = defaultMap.get(id)
      if (key === 'basic' && paramsItem && defaultItem && (paramsItem.paramValue === defaultItem.paramValue)) {
      } else if (key === 'basic' && !paramsItem && defaultItem ) {
      } else {
        if (paramsItem) {
          result[key].push({...paramsItem})
        }
      }
      if (key === 'more') {
        const hasRulesEnable = result.more.some((param: ParamItem) => param.paramKey === "rules.enable")
        if (!hasRulesEnable) {
          result.more.push({
            paramKey: "rules.enable",
            paramValue: "true",
            paramDesc: "规则过滤，true代表开启，false代表关闭"
          })
        }
      }

    });
  });
  const currentSubTask = taskBasicInfo.value.subTaskData[curTableTabs.value]
  currentSubTask.taskParamsObject = { ...result }
}

const defaultParamsConfig = (type: string) => {
  const currentTab = taskBasicInfo.value.subTaskData[curTableTabs.value]
  if (currentTab.sourceDbType === JDBCType.MySQL) {
    taskBasicInfo.value.subTaskData[curTableTabs.value].taskParamsObject.more.length = 1
  }
  taskBasicInfo.value.subTaskData[curTableTabs.value].taskParamsObject.basic.length = 0
}

const addJdbc2Ref = ref<InstanceType<typeof AddJdbc2> | null>(null)
const handleAddSql = (dbType: string) => {
  if (dbType === 'source') {
    const type = taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDbType.toUpperCase()
    addJdbc2Ref.value?.open(type)
  } else {
    addJdbc2Ref.value?.open(dbType)
  }
}

const taskBasicInfo = ref<migrationTaskList>({
  taskId: 0,
  taskName: '',
  subTaskData: [],
  selectedHosts: [],
})
const seleTblNum = computed(() => taskBasicInfo.value.subTaskData[curTableTabs.value].sourceTables?.length || 0)

const taskBasicRules = computed<FormRules>(() => {
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
    sourceIndex: [
      {
        validator: (rule: FormItemRule, value: any, callback: (error?: string | Error) => void) => {
          if (taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDbType === JDBCType.Elasticsearch) {
            if (seleTblNum.value <= 0) {
              callback(new Error(t('transcribe.create.required')))
            } else {
              callback()
            }
          } else {
            callback()
          }
        },
        trigger: ['blur', 'change']
      }
    ],
    sourceTables: [
      {required: taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDbType === JDBCType.Elasticsearch || JDBCType.Milvus,
        message: t('transcribe.create.required'), trigger: ['blur', 'change']},
    ],
    targetIpPort: [
      {required: true, trigger: ['blur', 'change'], message: t('transcribe.create.withouytargetip')},
    ],
    targetDbName: [
      {required: true, message: t('transcribe.create.required'), trigger: ['blur', 'change']},
    ],
    mode: [
      {required: shouldShowDbColumn,
        message: t('step1.index.onlineContent'), trigger: ['blur', 'change']},
    ],
  } as FormRules
})

const shouldShowDbColumn = computed(() => {
  const types = [JDBCType.MySQL, JDBCType.PostgreSQL].map(String)
  const currentType = taskBasicInfo.value.subTaskData[curTableTabs.value]?.sourceDbType
  return types.includes(currentType || '')
})

const checkOptionVisible = computed(() => {
  const currentType = taskBasicInfo.value.subTaskData[curTableTabs.value]?.sourceDbType
  return currentType === JDBCType.MySQL
})

const shouldShowMigrationObject = computed(() => {
  const currentType = taskBasicInfo.value.subTaskData[curTableTabs.value]?.sourceDbType
  return currentType === JDBCType.MySQL || currentType === JDBCType.PostgreSQL
})

const changeSourceType = (type?: string) => {
  if (type === 'select') {
    const currentTask = taskBasicInfo.value.subTaskData[curTableTabs.value]
    currentTask.sourceIpPort = ''
    currentTask.sourceDBName = ''
    currentTask.sourceTables = []
    currentTask.sourceSchema = []
    currentTask.targetIpPort = ''
    currentTask.targetDBName = ''
    currentTask.isDefaultConfig = true
    currentTask.isSystemAdmin = false
    currentTask.isMigrationObject = true
    currentTask.mode = 3
    defaultParamsConfig('customized')
    preSourceDb.value = ''
    changeSeleTbl(true)
  }
  getSourceClustersData()
  setTimeout(() => {
    const formRef = taskDataFormRef.value[curTableTabs.value]
    if (formRef) {
      formRef.clearValidate()
    }
  }, 10)
}

const taskNameFormRef = ref<InstanceType<typeof ElForm> | null>(null)
const taskDataFormRef = ref<(FormInstance | null)[]>([])

interface TreeNode {
  value: string | number
  label: string
  disabled?: boolean
  isLeaf?: boolean
  children?: TreeNode[]
  level?: number
}

interface ClusterNodeInfo {
  selection: string;
  clusterNodeId: string;
  versionNum: string;
  clusterId: string;
  sourceTable: string;

}

const sourceClusterOption = ref<TreeNode[]>([])
const sourceClusterfilterOption = ref<TreeNode[]>([])
const sourceClusterInfo = ref<{ [key: string]: ClusterNodeInfo }>({})
const getSourceClustersData = () => {
  const propsType = props?.modelValue?.subTaskData?.[0]?.sourceDbType ?? ''
  const tempDbType = taskBasicInfo.value?.subTaskData?.[curTableTabs.value]?.sourceDbType || propsType || JDBCType.MySQL
  sourceClustersType(tempDbType).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      const processedData = res.data.map((item: any) => {
        const children = item.nodes.map((node: any) => {
          const value = `${node.ip}:${node.port}`
          return {
            value: value,
            label: value,
          }
        })
        return {
          value: item.name,
          label: item.name,
          children: children
        }
      })
      sourceClusterOption.value = processedData
      res.data.forEach((item: any) => {
        Object.values(item.nodes).forEach((node: any) => {
          const tempname = `${node.ip}:${node.port}`

          sourceClusterInfo.value[tempname] = {
            selection: node.username,
            clusterNodeId: node.clusterNodeId,
            versionNum: item.versionNum || '',
            clusterId: item.clusterId,
            sourceTable:'JDBC_CLUSTER'
          }
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
    }else {
      getSourceClusterDB('select')
    }
  })
}

const compareVersions = (basic: string, real: string) => {
  const parse = (v: string) => v.split(/[_-]/)[0].split('.').map(Number)
  const a = parse(real), b = parse(basic)
  for (let i = 0; i < Math.max(a.length, b.length); i++) {
    const diff = (a[i] || 0) - (b[i] || 0)
    if (diff !== 0) return Math.sign(diff)
  }
  return 0
}

const sourceVersionNum = computed(() => {
  const ip = taskBasicInfo.value.subTaskData[curTableTabs.value].sourceIpPort
  const version = sourceClusterInfo.value[ip]?.versionNum
  return version || ''
})

const checkSourceclusterVersion = computed(() => {
  const dbType = taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDbType
  if (sourceVersionNum.value !== '') {
    const basicVersion = dbType === JDBCType.MySQL ? '5.7' :
      dbType === JDBCType.Elasticsearch ? '7.3' : dbType === JDBCType.PostgreSQL? '9.4.26':'2.3'
    return compareVersions(basicVersion, sourceVersionNum.value) !== -1
  }
  return true
})

const targetVersionNum = computed(() => {
  const ip = taskBasicInfo.value.subTaskData[curTableTabs.value].targetIpPort
  const version = targetClusterInfo.value[ip]?.versionNum
  return version || ''
})

const checkTargetclusterVersion = computed(() => {
  const dbType = taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDbType
  if (targetVersionNum.value !== '') {
    const basicVersion = dbType === JDBCType.Milvus ? '7.0' :
      dbType === JDBCType.Elasticsearch ? '7.0' : dbType === JDBCType.PostgreSQL? '6.0.0': '5.0.0'
    return compareVersions(basicVersion, targetVersionNum.value) !== -1
  }
  return true
})

const checkMigrationConditions = () => {
  const res = checkSourceclusterVersion.value && checkTargetclusterVersion.value
  if (!res) {
    showMessage('error', t('step1.index.versionCheckError'))
  }
  return res
}

const handleGetVersion = (type: string) => {
  if ( type === 'source') {
    const sourceDbType = taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDbType
    const ip = taskBasicInfo.value.subTaskData[curTableTabs.value].sourceIpPort
    if (sourceClusterInfo.value[ip]) {
      clusterVersioNnum(sourceClusterInfo.value[ip].clusterId, sourceDbType) .then((res: KeyValue) => {
        if(Number(res.code) === 200) {
          sourceClusterInfo.value[ip].versionNum = res.msg
        }
      }) .catch ((error: any) => {
        console.log(error)
      })
    }

  } else {
    const ip = taskBasicInfo.value.subTaskData[curTableTabs.value].targetIpPort
    const clusterInfo = {...targetClusterInfo.value[ip]} || []
    if (clusterInfo.sourceTable === 'JDBC_CLUSTER') {
      clusterVersioNnum(clusterInfo.clusterId, JDBCType.openGauss) .then((res: KeyValue) => {
        if(Number(res.code) === 200) {
          targetClusterInfo.value[ip].versionNum = res.msg
        }
      }) .catch ((error: any) => {
        console.log(error)
      })
    }
  }

}


const filterSourceMethod = (value: any) => {
  sourceClusterfilterOption.value = [...sourceClusterOption.value].filter((item) => item.label.includes(value))
}

const targetClusterOption = ref<TreeNode[]>([])
const targetClusterfilterOption = ref<TreeNode[]>([])

const targetClusterInfo = ref<{ [key: string]: ClusterNodeInfo }>({})
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
  targetClustersType().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      targetClusterOption.value = []
      targetClusterInfo.value = {}
      targetClusterOption.value = res.data.map((item: any) => ({
        value: item.name,
        label: item.name,
        isLeaf: false,
        children: item.nodes?.map((node: any) => ({
          value: `${node.ip}:${node.port}`,
          label: `${node.ip}:${node.port}`,
          isLeaf: true,
          disabled: false
        }))
      }))
      res.data.forEach((item: any) => {
        Object.values(item.nodes).forEach((node: any) => {
          const tempname = `${node.ip}:${node.port}`;
          targetClusterInfo.value[tempname] = {
            selection: item.name,
            clusterNodeId: node.clusterNodeId,
            versionNum: item.versionNum || '',
            clusterId: item.clusterId,
            sourceTable: item.sourceTable
          }
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
    const findClusterInfoBySelection = (selectionValue: string) => {
      return Object.values(targetClusterInfo.value).find(
        item => item.selection === selectionValue
      )
    }
    const clusterInfo = findClusterInfoBySelection(node.data.label)
    checkTargetclusterMaster(clusterInfo.sourceTable, clusterInfo.clusterId) .then((res: any) => {
      if(Number(res.code) === 200) {
        res.data.nodes.forEach((node: any) => {
          const nodeKey = `${node.ip}:${node.port}`
          disabledMap.set(nodeKey, !node.primary)
        })
        taskBasicInfo.value.subTaskData[curTableTabs.value].isSystemAdmin = res.data.userMaster
        const processedChildren = (node.data.children || []).map((child: any) => ({
          ...child,
          disabled: disabledMap.get(String(child.value)) || false
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

const changeSourceDb = async() => {
  if(taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDbType === JDBCType.PostgreSQL) {
    await getSourceSchema()
    const allSchemaValues = sourceSchemaOptions.value.map(opt => opt.value)
    taskBasicInfo.value.subTaskData[curTableTabs.value].sourceSchema = [...allSchemaValues]
    const formRef = taskDataFormRef.value[curTableTabs.value]
    if (formRef) {
      formRef.clearValidate()
    }
  } else {
    const currentTab = taskBasicInfo.value.subTaskData[curTableTabs.value]
    const { sourceDbType, sourceDBName, sourceIpPort } = currentTab
    if (preSourceDb.value !== '') {
      const isDbChanged = preSourceDb.value !== sourceDBName
      const isIpChanged = preSourceDb.value !== sourceIpPort
      if (sourceDbType === JDBCType.MySQL && isDbChanged) {
        currentTab.isSelectAlltables = true
      }
      currentTab.sourceTables.length = 0
    }
    preSourceDb.value = sourceDbType === JDBCType.Elasticsearch ? sourceIpPort : sourceDBName
  }
}

const sourceDBOptions = ref<{ [key: string]: string }[]>([])

const sourceSchemaOptions = ref<{ [key: string]: string }[]>([])
const getSourceSchema = async (type?: string) => {
  sourceSchemaOptions.value = []
  const subTask = taskBasicInfo.value.subTaskData[curTableTabs.value]
  const dbName = subTask.sourceDBName
  const dbType = subTask.sourceDbType
  const [ip, port] = parseIpPort(subTask.sourceIpPort)
  const nodeId = sourceClusterInfo.value[subTask.sourceIpPort]?.clusterNodeId
  subTask.sourceIp = ip
  subTask.sourcePort = port
  subTask.sourceNodeId = nodeId
  try {
    const res: KeyValue = await sourceClusterSchema(
      dbType, nodeId, dbName
    )
    if (Number(res.code) === 200) {
      sourceSchemaOptions.value = res.data.map((db: any) => ({
        key: db,
        value: db,
        label: db,
      }))
    }
  } catch (error) {
    console.error('获取源schema失败:', error)
  }
}

const parseIpPort = (ipPort: string): [string, number] => {
  const lastColonIndex = ipPort.lastIndexOf(':');
  if (lastColonIndex === -1) return ['', 0];
  return [
    ipPort.slice(0, lastColonIndex),
    Number(ipPort.slice(lastColonIndex + 1))
  ]
}

const getDbTypeCode = (dbType: string) => {
  const typeMap: Record<string, string> = {
    [JDBCType.MySQL]: 'B',
    [JDBCType.PostgreSQL]: 'PG',
  }
  return typeMap[dbType] || 'A'
}

const updateDatabaseOptions = (databases: string[], type: string) => {
  if (type === 'source') {
    sourceDBOptions.value = databases.map((db: any) => ({key: db, value: db}))
  } else {
    const { sourceDbType } = taskBasicInfo.value.subTaskData[curTableTabs.value]
    let dbType = getDbTypeCode(sourceDbType);
    targetDBOptions.value = databases.map((db: any) => ({key: db.dbName + '(' +  db.datcompatibility.toUpperCase() + ')',
      value: db.dbName, select: db.select && (db.datcompatibility.toUpperCase() === dbType)}))
  }
}

const getSourceClusterDB = async (type?: string) => {
  if (taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDBName !== '' && type !== 'init') {
    taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDBName = ''
  }
  sourceDBOptions.value = []
  const subTask = taskBasicInfo.value.subTaskData[curTableTabs.value]
  const [ip, port] = parseIpPort(subTask.sourceIpPort)
  const clusterNodeId = sourceClusterInfo.value[subTask.sourceIpPort]?.clusterNodeId
  subTask.sourceIp = ip
  subTask.sourcePort = port
  subTask.sourceNodeId = clusterNodeId
  const dbType = subTask.sourceDbType
  if (clusterNodeId && dbType !== JDBCType.Elasticsearch) {
    sourceClusterDbsType(clusterNodeId, dbType).then((res: any) => {
      if (Number(res.code) === 200) {
        updateDatabaseOptions(res.data, 'source')
      }
    }).catch((error) => {
      console.error('get source databases error:', error)
    }) .finally(() => {
      if (dbType === JDBCType.Milvus) {
        subTask.sourceDBName = 'default'
      }
    })
  }
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
  subTask.targetIp = ip
  subTask.targetPort = port
  const clusterInfo = {...targetClusterInfo.value[targetIpPort]} || []
  subTask.targetIp = ip
  subTask.targetPort = port
  subTask.targetNodeId = clusterInfo.clusterNodeId
  targetClusterDbs(clusterInfo.sourceTable, clusterInfo.clusterNodeId).then((res: any) => {
    if (Number(res.code) === 200) {
      updateDatabaseOptions(res.data, 'target')
    }
  }).catch((error) => {
    console.error('get target databases error:', error);
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
    showMessage('error', t('transcribe.create.saveerr'))
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
      showMessage('error', t('step1.index.validErrorMsg', {num: curTableTabs.value + 1}))
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
    console.error('save error:', error)
    showMessage('error', t('step1.index.saveErrMsg'))
    return false
  }
}
defineExpose({
  saveAllTask, checkMigrationConditions
})

const changeSeleTbl = (value: boolean) => {
  const currentTask = taskBasicInfo.value.subTaskData[curTableTabs.value]
  const { sourceDbType, sourceIpPort, sourceDBName, isSelectAlltables } = currentTask

  if (sourceDbType === JDBCType.MySQL) {
    if (!sourceIpPort || !sourceDBName) {
      currentTask.isSelectAlltables = true
      showMessage('error', t('step1.index.seleTblMsg'))
      return
    }
    currentTask.isSelectAlltables = value
    if (value) {
      currentTask.sourceTables.length = 0
    } else {
      dataTblWin()
    }
  } else {
    if (value) {
      currentTask.sourceTables.length = 0
    } else {
      dataTblWin()
    }
  }
}

const seleDBMsg = ref({
  dbName: '',
  seletedTbl: [] as string[],
  dbType: '',
  nodeId: ''
})
const dataTblModalRef = ref<{ init: () => void } | null>(null)
const dataTblWin = async () => {
  const currentTask = taskBasicInfo.value.subTaskData[curTableTabs.value]
  let sourceDbType = currentTask.sourceDbType
  const clusterNodeId = sourceClusterInfo.value[currentTask.sourceIpPort]?.clusterNodeId
  seleDBMsg.value.dbName = sourceDbType === JDBCType.Elasticsearch
    ? currentTask.sourceIpPort
    : currentTask.sourceDBName
  seleDBMsg.value.seletedTbl = [...(currentTask.sourceTables || [])]
  seleDBMsg.value.dbType = currentTask.sourceDbType
  seleDBMsg.value.nodeId = clusterNodeId
  dataTblModalRef.value?.init()
}

const dataTblWinClose = () => {
  let sourceDbType = taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDbType
  if (sourceDbType === JDBCType.MySQL) {
    if(taskBasicInfo.value.subTaskData[curTableTabs.value].sourceTables.length === 0) {
      taskBasicInfo.value.subTaskData[curTableTabs.value].isSelectAlltables = true
      taskBasicInfo.value.subTaskData[curTableTabs.value].sourceTables = []
      showMessage('info', t('step1.index.selectTblContent'))
    }
  }
}

const handleTableSeleted = (selectedTblCurrent: any) => {
  let sourceDbType = taskBasicInfo.value.subTaskData[curTableTabs.value].sourceDbType
  const newTables = Array.isArray(selectedTblCurrent) ? [...selectedTblCurrent] : []
  if (sourceDbType === JDBCType.MySQL) {
    if (newTables.length === 0) {
      taskBasicInfo.value.subTaskData[curTableTabs.value].isSelectAlltables = true
      taskBasicInfo.value.subTaskData[curTableTabs.value].sourceTables = []
      showMessage('info', t('step1.index.selectTblContent'))
    } else {
      taskBasicInfo.value.subTaskData[curTableTabs.value].sourceTables = newTables
    }
  } else {
    taskBasicInfo.value.subTaskData[curTableTabs.value].sourceTables = newTables
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
    sourceNodeId: '',
    sourceDBName: '',
    sourceSchema: [],
    sourceTables: [],
    targetNodeName: '',
    targetNodeId: '',
    targetDBName: '',
    configType: 1,
    isAdjustKernelParam: false,
    isSystemAdmin: false,
    isMigrationObject: true,
    taskParamsObject: {
      basic: [],
      more: [{paramKey: "rules.enable", paramValue: "true", paramDesc: "规则过滤，true代表开启，false代表关闭"}],
    },
    id: '',
    curretTab: Number(currentTab),
    subTaskName: `Task_${dayjs().format('YYYYMMDDHHmm')}_${Math.random().toString(36).substring(2, 8)}` + currentTab,
    sourceDbType: JDBCType.MySQL,
    sourceIpPort: '',
    targetIpPort: '',
    sourceIp: '',
    targetIp: '',
    sourcePort: 0,
    targetPort: 0,
    selectHost: '',
    mode: 3,
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
      const tabIndex = editableTabs.value.findIndex(tab => {
        const tabNameAsNumber = Number(tab.name)
        return !isNaN(tabNameAsNumber) && tabNameAsNumber === item.curretTab
      })
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
  const currentTab = taskBasicInfo.value.subTaskData[curTableTabs.value]
  preSourceDb.value = currentTab ?
    (currentTab.sourceDbType === JDBCType.Elasticsearch ?
      currentTab.sourceIpPort || '' :
      currentTab.sourceDBName || '')
    : ''
}

const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

const initialized = ref();
onMounted(() => {
  if (!initialized.value) {
    scrollToTop()
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
  width: 100%;
  width: -moz-available;
  width: -webkit-fill-available;
  width: stretch;

  .h3 {
    color: var(--o-text-color-primary)
  }

  .h4 {
    color: var(--o-text-color-primary)
  }
}

.backgroundform {
  background-color: var(--o-bg-color-base);
  padding: 10px 20px;
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

.link-group {
  display: flex;
  gap: 12px;
}

.spacing-left {
  margin-left: 8px;
}
.spacing-right {
  margin-right: 8px;
}
</style>

