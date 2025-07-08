<template>
  <div class="home-container">
    <div class="main-con">
      <el-container class="page-container">
        <el-header class="header">
          <el-page-header @back="backToIndex" :title="t('task.index.back')" class="page-header text-color-second">
            <template #content>
              <span class="text-color font-600 mr-3">{{ $t('task.index.indexName') }}</span>
            </template>
          </el-page-header>
        </el-header>
        <el-main  class="main-content">
          <div class="content-wrapper">
            <div class="content-box">
              <step1 v-if="currentStep === 1" ref="stepOneComp" v-model="taskBasicInfo" :defaultBasicData="defaultBasicData" key="child-stable" />
              <step2 v-if="currentStep === 2" ref="stepTwoComp" :taskBasicInfo="taskBasicInfo" @syncHost="syncHost" />
            </div>
          </div>
        </el-main>
        <el-footer class="submit-con">
          <div class="btn-group">
            <div v-if="currentStep === 1">
              <el-button class="btn-item" @click="backToIndex">{{ $t('task.index.cancel') }}</el-button>
              <el-button class="btn-item" type="primary"  @click="onNext">{{ $t('task.index.nextStep') }}</el-button>
            </div>
            <div v-else-if="currentStep === 2">
              <el-button class="btn-item" @click="backToIndex">{{ $t('task.index.cancel') }}</el-button>
              <el-button class="btn-item"  @click="onPrev">{{ $t('task.index.prevStep') }}</el-button>
              <el-button class="btn-item" type="primary" @click="saveConfig">{{ $t('task.index.finish') }}</el-button>
            </div>
          </div>
        </el-footer>
      </el-container>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {ref, onMounted, provide, computed, toRaw} from 'vue'
import Step1 from './step1/index.vue'
import Step2 from './step2/index.vue'
import {migrationSave, migrationUpdate, defaultParams} from '@/api/task'
import {taskEditInfo} from '@/api/detail'
import dayjs from 'dayjs'
import {mergeObjectArray} from '@/utils'
import {KeyValue} from "@/types/global";
import showMessage from "@/utils/showMessage";
import {useI18n} from 'vue-i18n'
import {PORTAL_INSTALL_STATUS} from "@/utils/constants";

const {t} = useI18n()

const currentStep = ref(0)

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
      paramKey: string,
      paramValue: string,
      paramDesc: string,
    }[],
    more: {
      paramKey: string,
      paramValue: string,
      paramDesc: string,
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
    more: [],
  }
})

const defaultBasicData = ref([])
const defaultMoreData = ref([])
const stepOneComp = ref()
const submitLoading = ref(false)
provide('changeSubmitLoading', (val) => {
  submitLoading.value = val
})

const validateForms = () => {
  const childComponent = stepOneComp.value
  if (childComponent) {
    return childComponent.saveAllTask()
  }
  return false
}

const onPrev = () => {
  currentStep.value = Math.max(1, currentStep.value - 1)
}

const onNext = async () => {
  if (currentStep.value === 1) {
    const validRes = await validateForms()
    if (validRes) {
      currentStep.value = Math.min(2, currentStep.value + 1)
    } else {
      showMessage('error', t("task.index.saveErrMsg"))
    }
  }
}


const selectedPortal = ref()
const syncHost = (hosts: any) => {
  selectedPortal.value = toRaw(hosts)
  taskBasicInfo.value.selectedHosts = [ ...selectedPortal.value ]
}

const findHostsFromTableByStatus = (keys, status) => {
  if (keys.length > 0) {
    const hostList = keys.filter(
      (item) => item.installPortalStatus === status
    )
    return hostList
  } else {
    return []
  }
}

const stepTwoComp = ref()

const saveConfig = async () => {
  if (selectedPortal.value.length > 0) {
    const installedHosts = await findHostsFromTableByStatus(selectedPortal.value, PORTAL_INSTALL_STATUS.INSTALLED)
    taskBasicInfo.value.selectedHosts = []
    installedHosts.map((item) => taskBasicInfo.value.selectedHosts.push(item.hostId))
    if(selectedPortal.value.length !== installedHosts.length) {
      showMessage('error',t('task.index.portalStatuserrMsg'))
      return
    }
    if (selectedPortal.value.length !== taskBasicInfo.value.subTaskData.length) {
      showMessage('error',t('task.index.portalNumerrMsg'))
      return
    }
  }
  if (taskBasicInfo.value.selectedHosts.length === 0) {
    showMessage('error','Please select a machine with migration kit installed')
    return
  }
  const params = {
    taskId: taskBasicInfo.value.taskId,
    taskName: taskBasicInfo.value.taskName,
    globalParams: [
      ...taskBasicInfo.value.globalParamsObject.basic.map(item => ({
        paramKey: item.paramKey,
        paramValue: item.paramValue,
        paramDesc: item.paramDesc
      })),
      ...taskBasicInfo.value.globalParamsObject.more.map(item => ({
        paramKey: item.paramKey,
        paramValue: item.paramValue,
        paramDesc: item.paramDesc
      }))],
    hostIds: taskBasicInfo.value.selectedHosts,
    tasks: taskBasicInfo.value.subTaskData.map(item => {
      const taskParamsObject = {
        basic: mergeObjectArray(taskBasicInfo.value.globalParamsObject.basic, item.taskParamsObject.basic, 'paramKey'),
        more: mergeObjectArray(taskBasicInfo.value.globalParamsObject.more, item.taskParamsObject.more, 'paramKey')
      }
      taskParamsObject.more = taskParamsObject.more.filter((taskparam: KeyValue) => {
          let flag = true
          if (taskparam.parentKey) {
            flag = taskparam.childIndex <= taskParamsObject.more.find((e: KeyValue) => e.paramKey === taskparam.parentKey).paramValue
          }
          return flag
        }
      )
      return {
        isAdjustKernelParam: item.isAdjustKernelParam,
        migrationModelId: item.mode,
        sourceDb: item.sourceDBName,
        sourceDbHost: item.sourceNodeInfo.host,
        sourceDbPass: item.sourceNodeInfo.password,
        sourceDbPort: item.sourceNodeInfo.port,
        sourceDbUser: item.sourceNodeInfo.username,
        sourceNodeId: item.sourceNodeInfo.nodeId,
        sourceSchemas: item.sourceSchema? Object.values(item.sourceSchema).join(','): '',
        sourceDbType: item.sourceDbType.toUpperCase(),
        targetDb: item.targetDBName,
        targetDbHost: item.targetNodeInfo.host,
        targetDbPass: item.targetNodeInfo.password,
        targetDbPort: item.targetNodeInfo.port,
        targetDbUser: item.targetNodeInfo.username,
        targetDbVersion: item.targetNodeInfo.versionNum,
        targetNodeId: item.targetNodeInfo.nodeId,
        isSystemAdmin: item.isSystemAdmin,
        sourceTables: item.sourceTables,
        taskParams: [...taskParamsObject.basic.map((item: KeyValue) => ({
          paramKey: item.paramKey,
          paramValue: item.paramValue,
          paramDesc: item.paramDesc
        })), ...taskParamsObject.more.map((item: KeyValue) => ({
          paramKey: item.paramKey,
          paramValue: item.paramValue,
          paramDesc: item.paramDesc
        }))]
      }
    })
  }
  if (taskBasicInfo.value.taskId !== 0) {
    migrationUpdate(params).then(() => {
      currentStep.value = 1
      showMessage('success','Update success')
      window.$wujie?.bus.$emit('data-migration-update')
      window.$wujie?.bus.$emit('opengauss-close-tab', {
        name: 'Static-pluginData-migrationTaskConfig',
        fullPath: '/static-plugin/data-migration/taskConfig'
      })
      window.$wujie?.props.methods.jump({
        name: `Static-pluginData-migrationIndex`
      })
    })
  } else {
    delete params.taskId
    migrationSave(params).then(() => {
      currentStep.value = 1
      showMessage('success','Save success')
      window.$wujie?.bus.$emit('data-migration-update')
      window.$wujie?.bus.$emit('opengauss-close-tab', {
        name: 'Static-pluginData-migrationTaskConfig',
        fullPath: '/static-plugin/data-migration/taskConfig'
      })
      window.$wujie?.props.methods.jump({
        name: `Static-pluginData-migrationIndex`
      })
    }) .catch((error) => {
      console.log(error)
    })
  }
}

const getDefaultParams = (type: string) => {
  return defaultParams(type.toUpperCase()).then((res: any) => {
    if(Number(res.code) === 200) {
      if (type === 'MYSQL') {
        defaultBasicData.value = res.data.slice(0, 12)
        defaultMoreData.value = res.data.slice(12)
      } else {
        defaultBasicData.value = res.data
      }

    }
  }).catch((error) => {
    console.log(error)
  })
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
    sourceSchema: [],
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
      basic:  [],
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

const getTaskDetail = async (id: number) => {
  await getDefaultParams('MYSQL')
  taskEditInfo(id).then((res: KeyValue) => {
    if (Number(res.code) !== 200) return
    const {data} = res
    const {taskName, taskId, hostIds, globalParams, tasks} = data
    taskBasicInfo.value = {
      ...taskBasicInfo.value,
      taskName,
      taskId,
      selectedHosts: hostIds,
      globalParamsObject: {
        basic: globalParams.filter((child: KeyValue) => defaultBasicData.value.includes(child.paramKey)),
        more: globalParams.filter((child: KeyValue) => !defaultBasicData.value.includes(child.paramKey))
      }
    }
    tasks.map((task: KeyValue, index: number) => {
      const {
        isSystemAdmin, isAdjustKernelParam, migrationModelId, taskParams,
        sourceDb, sourceDbHost, sourceDbPort, sourceDbUser, sourceDbPass, sourceNodeId,
        targetDb, targetDbHost, targetDbPort, targetDbUser, targetDbPass, targetNodeId, targetDbVersion,
        sourceTables, sourceDbType, sourceSchemas
      } = task

      const sourceNodeName = `${sourceDbHost}:${sourceDbPort}`;
      const targetNodeName = `${targetDbHost}:${targetDbPort}`;
      const subTask = initSubTask(index.toString())
      const isDefaultConfigtemp = (taskParams.length > 1) || (taskParams.length === 1 && taskParams[0].paramKey !== "rules.enable")
      const defaultBasicDataSet = computed(() =>
        new Set(defaultBasicData.value.map(item => item.paramKey))
      )
      const defaultMoreDataSet = computed(() =>
        new Map(defaultMoreData.value.map(item => [item.paramKey, item]))
      )
      Object.assign(subTask, {
          isSystemAdmin,
          isAdjustKernelParam,
          isDefaultConfig:!isDefaultConfigtemp,
          mode: migrationModelId,
          configType: taskParams.length ? 2 : 1,
          sourceDBName: sourceDb,
          sourceNodeName,
          sourceIpPort: sourceNodeName,
          sourceDbType: sourceDbType? sourceDbType: 'MYSQL',
          sourceSchema: [],
          targetDBName: targetDb,
          targetNodeName,
          targetIpPort: targetNodeName,
          sourceNodeInfo: {
            host: sourceDbHost,
            password: sourceDbPass,
            port: sourceDbPort,
            username: sourceDbUser,
            nodeId: sourceNodeId
          },
          targetNodeInfo: {
            host: targetDbHost,
            password: targetDbPass,
            port: targetDbPort,
            username: targetDbUser,
            nodeId: targetNodeId,
            versionNum: targetDbVersion
          },
          taskParamsObject: {
            basic: taskParams.filter((child: KeyValue) => defaultBasicDataSet.value.has(child.paramKey)),
            more: taskParams
              .filter((child: KeyValue) => !defaultBasicDataSet.value.has(child.paramKey))
              .map((child: KeyValue) => {
                if (!defaultMoreDataSet.value.has(child.paramKey)) {
                  const tempParamKey = child.paramKey.split('.').slice(0, 2).join('.')
                  if (defaultMoreDataSet.value.has(tempParamKey)) {
                    const tempParamExtends = JSON.parse(defaultMoreDataSet.value.get(tempParamKey).paramExtends)
                    const matchedItem = tempParamExtends.find((item: any )=> {
                      const regex = new RegExp(`^${item.subKeyPrefix}\\d*$`)
                      return regex.test(child.paramKey)
                    })
                    return {
                      ...child,
                      paramType: matchedItem.paramType,
                      paramRules: matchedItem.paramRules,
                    }
                  }
                  return child
                }
                return child
              })
          },
          sourceTables
      });
      taskBasicInfo.value.subTaskData.push(subTask)
    });
  }).catch(console.error)
    .finally(() => {
      currentStep.value = 1
    })
}

const inittaskBasicInfo = async () => {
  await getDefaultParams('MYSQL')
  taskBasicInfo.value.taskName = `Task_${dayjs().format('YYYYMMDDHHmm')}_${Math.random().toString(36).substring(2, 8)}`
  taskBasicInfo.value.subTaskData.length = 0
  taskBasicInfo.value.selectedHosts = []
  taskBasicInfo.value.globalParamsObject.basic = []
  taskBasicInfo.value.globalParamsObject.more = []
  currentStep.value = 1
}

onMounted(() => {
  currentStep.value = 0
  const id = window.$wujie?.props.data.id
  if (id) {
    getTaskDetail(id)
  } else {
    inittaskBasicInfo()
  }
});

const backToIndex = () => {
  currentStep.value = 1
  window.$wujie?.bus.$emit('data-migration-update')
  window.$wujie?.bus.$emit('opengauss-close-tab', {
    name: 'Static-pluginData-migrationTaskConfig',
    fullPath: '/static-plugin/data-migration/taskConfig'
  })
  window.$wujie?.props.methods.jump({
    name: `Static-pluginData-migrationIndex`
  })
}

</script>

<style lang="less" scoped>
.home-container {
  position: relative;
  height: calc(100vh - 123px);
  overflow: hidden;
  background: var(--o-bg-color-light);

  .main-con {
    height: calc(100vh - 123px);
    padding-bottom: 40px;
    overflow-y: auto;
  }
  .submit-con {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 10;
    height: 45px;
    padding-left: 20px;
    padding-right: 20px;
    padding-top: 10px;

    background: var(--color-bg-2);
    border-top: 1px solid #e4e7ed;
    .el-footer& {
      padding: 0 !important;
      box-sizing: border-box;
    }

    .btn-group {
      height: 100%;
      display: flex;
      justify-content: flex-end;
      align-items: center;
      gap: 20px;
    }

    .btn-item {
      margin-left: 10px;
      min-width: 80px;
    }
  }
}

.page-container {
  display: flex;
  flex-direction: column;
}

.header {
  padding: 0 !important;
}

.page-header {
  padding: 20px;
  background: var(--o-bg-color-light);
  .text-color {
    color: var(--o-text-color-primary);
  }
}
.text-color-second {
  color: var(--o-text-color-secondary);
}

.header-title {
  font-size: 1.2rem;
  font-weight: 600;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content-wrapper {
  flex: 1;
  overflow-y: auto;
  position: relative;

}
.content-box {
  height: 1080px;
  background: var(--o-bg-color-light);
  border-radius: 4px;
}

</style>
