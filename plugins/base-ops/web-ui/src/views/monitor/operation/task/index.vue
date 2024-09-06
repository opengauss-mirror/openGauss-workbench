<template>
  <div class="home-container">
    <div class="main-con">
      <header class="header">
        <a-page-header
          class="demo-page-header"
          title="创建并行安装任务"
          @back="backToIndex"
        >
        </a-page-header>
      </header>
      <a-divider />
      <div class="task-steps-con">
        <a-steps :current="currentStep" class="step_title">
          <a-step>
            <div class="word_style" @click="toStepFirst">
              <h1>1</h1>
              {{$t('任务配置')}}
            </div>
          </a-step>
          <a-step>
            <div class="word_style" @click="toStepSecond">
              <h1>2</h1>
              {{$t('环境监测')}}
            </div>
          </a-step>
          <a-step @click="toStepThird">
            <div class="word_style">
              <h1>3</h1>
              {{$t('信息确认')}}
            </div>
          </a-step>
        </a-steps>
      </div>
      <step1 v-if="currentStep === 1"
             ref="stepOneComp"
             :clusterId="tempClusterId"
             :createClusterId="createClusterId"
             :sub-task-config="subTaskConfig"
             @syncConfig="syncSubTask" />
      <step2 v-if="currentStep === 2"
             :sub-task-config="subTaskEnv"
             :message="clusterTaskList.clusterNodes"
             :clusterId="clusterId || tempClusterId"
             @subTaskEnv="syncSubTaskEnv"/>
      <step3 v-if="currentStep === 3"
             :sub-task-config="subTaskConfig"
             :message="clusterTaskList.clusterNodes"
             :clusterTaskList="clusterTaskList"/>
    </div>
    <div class="submit-con">
      <a-button v-if="currentStep === 1 " class="btn-item" @click="backToIndex">{{$t('取消')}}</a-button>
      <a-button v-if="currentStep === 2 || currentStep === 3" type="outline" class="btn-item" @click="onPrev" :loading="submitLoading">{{$t('上一步')}}</a-button>
      <a-button  class="btn-item" @click="saveConfig" :loading="submitLoading">{{$t('保存为草稿')}}</a-button>
      <a-button v-if="currentStep === 1 || currentStep === 2" type="primary" class="btn-item" @click="onNext">{{$t('下一步')}}</a-button>
      <a-button v-if="currentStep === 3" type="primary" class="btn-item" @click="submitClusterInfo" :loading="submitLoading">{{$t('提交')}}</a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, reactive, onMounted, provide, watch} from 'vue'
import { Message } from '@arco-design/web-vue'
import Step1 from './step1'
import Step2 from './step2'
import Step3 from './step3'
import {
  batchClusterNodes,
  checkCluster,
  createClusterTask, createClustertaskNode, envCheckResult,
  submitCluster,
  updateClusterTask, updateClustertaskNode
} from "@/api/ops";
import {useRoute, useRouter} from "vue-router";
import {OpenGaussVersionEnum} from "@/types/ops/install";
import router from "@/router";

const currentStep = ref(1)
const taskId = ref()
const taskName = ref('')
const subTaskConfig = ref(['2'])
const subTaskEnv = ref()
const globalParamsObject = reactive({
  basic: [],
  more: []
})
const defaultBasicData = ref([])
const stepOneComp = ref(null)
const submitLoading = ref(false)
provide('changeSubmitLoading', (val) => {
  submitLoading.value = val
})
// prev step
const onPrev = () => {
  if (currentStep.value === 2) {
    tempClusterId.value = clusterId.value
  }
  currentStep.value = Math.max(1, currentStep.value - 1)
}

// next step
const onNext = () => {
  if (currentStep.value === 1) {
    if (saveFlag.value === false) {
      saveConfig()
    }
    if (saveFlag.value === false) {
      Message.error('没有保存草稿箱，无法进行下一步')
    } else {
      checkCluster(clusterId.value || tempClusterId.value) .then((res) => {
        if (Number(res.code) === 200 ) {
          currentStep.value = 2
        } else {
          Message.error('检测未通过')
        }
      }) .catch ((error) => {
        console.error('检测未通过', error)
      })
    }
  } else if (currentStep.value === 2) {
    envCheckResult(clusterId.value || tempClusterId.value) .then((res) =>{
      if (Number(res.code) === 200 ) {
        if (res.data.result === "SUCCESS" ){
          currentStep.value = 3
        } else {
          Message.error('环境监测未通过，请重新进行环境监测')
        }
      }
    }) .catch((error) => {
      console.error(error)
    })
  } else {
    currentStep.value = Math.min(3, currentStep.value + 1)
  }
}
const toStepFirst = () => {
  currentStep.value = 1
}
const toStepSecond = () => {
  if (currentStep.value === 1) {
    if (saveFlag.value === false) {
      saveConfig()
    }
    if (saveFlag.value === false) {
      Message.error('没有保存草稿箱，无法进行下一步')
    } else {
      currentStep.value = 2
    }
  } else {
    currentStep.value = 2
  }
}
const toStepThird = () => {
  if (currentStep.value === 2) {
    envCheckResult(clusterId.value).then((res) => {
      if (Number(res.code) === 200) {
        if (res.data.result === "SUCCESS") {
          currentStep.value = 3
        } else {
          Message.error('环境监测未通过，请重新进行环境监测')
        }
      }
    }).catch((error) => {
      console.error(error)
    })
  }
}

const syncSubTask = (configData: any) => {
  subTaskConfig.value = configData
}
const syncSubTaskEnv = (configData: any) => {
  subTaskEnv.value = configData
}

const clusterTaskList = reactive( {
  clusterNodes: [],
  hostId: "",
  hostUserId: "",
  os: "",
  cpuArch: "",
  version: "",
  versionNum: "",
  packageName: "",
  packageId: "",
  clusterName: "",
  databaseUsername: "",
  databasePassword: "",
  port: 0,
  installPackagePath: "",
  installPath: "",
  logPath: "",
  tmpPath: "",
  omToolsPath: "",
  corePath: "",
  envPath: "",
  enableCmTool: false,
  enableGenerateEnvironmentVariableFile: false,
  xmlConfigPath: "",
  deployType: "CLUSTER"
})

const clusterId = ref('')
const saveFlag = ref(false)
const checkPathFlag = ref(false)
const editFlag  = ref(true)

const saveUpdateCulster = async () => {
  if (editFlag.value && (!subTaskConfig.value.clusterId || subTaskConfig.value.clusterId === '') && currentStep.value === 1) {
    let nodeSaveFlag = 0
    if (subTaskConfig.value.deployType === "CLUSTER"
      && subTaskConfig.value.packageVersion !== OpenGaussVersionEnum.MINIMAL_LIST
      && clusterTaskList.clusterNodes.length < 2){
      Message.error('当前选择多节点模式，请至少输入两个节点数据')
    } else {
      clusterTaskList.hostId = subTaskConfig.value.hostId
      clusterTaskList.hostUserId = subTaskConfig.value.hostUserId
      clusterTaskList.os = subTaskConfig.value.os
      clusterTaskList.cpuArch = subTaskConfig.value.cpuArch
      clusterTaskList.version = subTaskConfig.value.packageVersion
      clusterTaskList.versionNum = subTaskConfig.value.packageVersionNum
      clusterTaskList.packageName = subTaskConfig.value.packageName
      clusterTaskList.packageId = subTaskConfig.value.packageId
      clusterTaskList.clusterName = subTaskConfig.value.clusterName
      clusterTaskList.databaseUsername = subTaskConfig.value.hostUser
      clusterTaskList.databasePassword = subTaskConfig.value.databasePassword
      clusterTaskList.port = subTaskConfig.value.port
      clusterTaskList.installPackagePath = subTaskConfig.value.installPackagePath
      clusterTaskList.installPath = subTaskConfig.value.installPath
      clusterTaskList.logPath = subTaskConfig.value.logPath
      clusterTaskList.tmpPath = subTaskConfig.value.tmpPath
      clusterTaskList.omToolsPath = subTaskConfig.value.omToolsPath
      clusterTaskList.corePath = subTaskConfig.value.corePath
      clusterTaskList.envPath = subTaskConfig.value.envPath
      clusterTaskList.enableCmTool = subTaskConfig.value.enableCmTool
      clusterTaskList.enableGenerateEnvironmentVariableFile = subTaskConfig.value.enableGenerateEnvironmentVariableFile
      clusterTaskList.xmlConfigPath = subTaskConfig.value.xmlConfigPath
      clusterTaskList.deployType = subTaskConfig.value.deployType
      createClusterTask({
        "hostId": clusterTaskList.hostId,
        "hostUserId": clusterTaskList.hostUserId,
        "os": clusterTaskList.os,
        "cpuArch": clusterTaskList.cpuArch,
        "version": clusterTaskList.version,
        "versionNum": clusterTaskList.versionNum,
        "packageName": clusterTaskList.packageName,
        "packageId": clusterTaskList.packageId,
        "clusterName": clusterTaskList.clusterName,
        "databaseUsername": clusterTaskList.databaseUsername,
        "databasePassword": clusterTaskList.databasePassword,
        "port": clusterTaskList.port,
        "installPackagePath": clusterTaskList.installPackagePath,
        "installPath": clusterTaskList.installPath,
        "logPath": clusterTaskList.logPath,
        "tmpPath": clusterTaskList.tmpPath,
        "omToolsPath": clusterTaskList.omToolsPath,
        "corePath": clusterTaskList.corePath,
        "envPath": clusterTaskList.envPath,
        "enableCmTool": clusterTaskList.enableCmTool,
        "enableGenerateEnvironmentVariableFile": clusterTaskList.enableGenerateEnvironmentVariableFile,
        "xmlConfigPath": clusterTaskList.xmlConfigPath,
        "deployType": clusterTaskList.deployType
      }).then((res) => {
        if (res.code === 200) {
          clusterId.value = res.msg
          createClusterId.value = clusterId.value
          saveFlag.value = true
          clusterTaskList.clusterNodes.forEach((record) => {
            createClustertaskNode({
              "clusterNodeId": '',
              "clusterId": clusterId.value,
              "hostId": record.hostId,
              "hostUserId": record.hostUserId,
              "nodeType": record.nodeType,
              "dataPath": record.dataPath,
              "azOwner": record.azOwner,
              "azPriority": record.azPriority,
              "isCMMaster": record.isCMMaster,
              "cmDataPath": record.cmDataPath,
              "cmPort": record.cmPort
            }) .then((res) => {
              if(Number(res.code) !== 200) {
                Message.error('保存草稿箱失败')
                saveFlag.value = false
              } else {
                nodeSaveFlag = nodeSaveFlag + 1
              }
            }) .catch((error) => {
              console.error(error)
            }) .finally(() => {
              if (subTaskConfig.value.deployType === "SINGLE_NODE" && clusterTaskList.clusterNodes.length > 1){
                Message.error('当前选择单节点模式，请删除多余节点')
              } else if (nodeSaveFlag === clusterTaskList.clusterNodes.length) {
                saveFlag.value = true
              }
            })
          })
        } else {
          Message.error('294保存草稿箱失败')
        }
      }) .catch((error) => {
        Message.error('保存草稿箱失败' + error)
      }) .finally(() => {
        if (subTaskConfig.value.deployType === "SINGLE_NODE" && clusterTaskList.clusterNodes.length > 1){
          Message.error('当前选择单节点模式，请删除多余节点')
        } else if (nodeSaveFlag === clusterTaskList.clusterNodes.length) {
          saveFlag.value = true
        }
      })
    }
    if (nodeSaveFlag === clusterTaskList.clusterNodes.length) {
      saveFlag.value = true
    }
    if (saveFlag.value) {
      Message.success('保存草稿箱成功')
    }
  } else if (editFlag.value && currentStep.value === 1 && subTaskConfig.value.clusterId !== '') {
    if (subTaskConfig.value.deployType === "CLUSTER"
      && subTaskConfig.value.packageVersion !== OpenGaussVersionEnum.MINIMAL_LIST
      && clusterTaskList.clusterNodes.length < 2) {
      Message.error('当前选择多节点模式，请至少输入两个节点数据')
    } else {
      let clusterSaveFlag = true
      clusterTaskList.hostId = subTaskConfig.value.hostId
      clusterTaskList.hostUserId = subTaskConfig.value.hostUserId
      clusterTaskList.os = subTaskConfig.value.os
      clusterTaskList.cpuArch = subTaskConfig.value.cpuArch
      clusterTaskList.version = subTaskConfig.value.packageVersion
      clusterTaskList.versionNum = subTaskConfig.value.packageVersionNum
      clusterTaskList.packageName = subTaskConfig.value.packageName
      clusterTaskList.packageId = subTaskConfig.value.packageId
      clusterTaskList.clusterName = subTaskConfig.value.clusterName
      clusterTaskList.databaseUsername = subTaskConfig.value.hostUser
      clusterTaskList.databasePassword = subTaskConfig.value.databasePassword
      clusterTaskList.port = subTaskConfig.value.port
      clusterTaskList.installPackagePath = subTaskConfig.value.installPackagePath
      clusterTaskList.installPath = subTaskConfig.value.installPath
      clusterTaskList.logPath = subTaskConfig.value.logPath
      clusterTaskList.tmpPath = subTaskConfig.value.tmpPath
      clusterTaskList.omToolsPath = subTaskConfig.value.omToolsPath
      clusterTaskList.corePath = subTaskConfig.value.corePath
      clusterTaskList.envPath = subTaskConfig.value.envPath
      clusterTaskList.enableCmTool = subTaskConfig.value.enableCmTool
      clusterTaskList.enableGenerateEnvironmentVariableFile = subTaskConfig.value.enableGenerateEnvironmentVariableFile
      clusterTaskList.xmlConfigPath = subTaskConfig.value.xmlConfigPath
      clusterTaskList.deployType = subTaskConfig.value.deployType
      clusterId.value = subTaskConfig.value.clusterId?subTaskConfig.value.clusterId:tempClusterId.value
      updateClusterTask({
        "clusterId": clusterId.value,
        "hostId": clusterTaskList.hostId,
        "hostUserId": clusterTaskList.hostUserId,
        "os": clusterTaskList.os,
        "cpuArch": clusterTaskList.cpuArch,
        "version": clusterTaskList.version,
        "versionNum": clusterTaskList.versionNum,
        "packageName": clusterTaskList.packageName,
        "packageId": clusterTaskList.packageId,
        "clusterName": clusterTaskList.clusterName,
        "databaseUsername": clusterTaskList.databaseUsername,
        "databasePassword": clusterTaskList.databasePassword,
        "port": clusterTaskList.port,
        "installPackagePath": clusterTaskList.installPackagePath,
        "installPath": clusterTaskList.installPath,
        "logPath": clusterTaskList.logPath,
        "tmpPath": clusterTaskList.tmpPath,
        "omToolsPath": clusterTaskList.omToolsPath,
        "corePath": clusterTaskList.corePath,
        "envPath": clusterTaskList.envPath,
        "enableCmTool": clusterTaskList.enableCmTool,
        "enableGenerateEnvironmentVariableFile": clusterTaskList.enableGenerateEnvironmentVariableFile,
        "xmlConfigPath": clusterTaskList.xmlConfigPath,
        "deployType": clusterTaskList.deployType
      }).then((res) => {
        if (res.code === 200) {
          batchClusterNodes(clusterId.value).then((res) => {
            if (res.code === 200) {
              const clusterNodesMap = new Map(res.data.clusterNodes.map(item => [item.clusterNodeId, item]))
              let countUpdateFlag = 0
              clusterTaskList.clusterNodes.forEach((item) => {
                if (item.clusterNodeId) {
                  const itemClusterNode = clusterNodesMap.get(item.clusterNodeId)
                  if (itemClusterNode
                    && (!itemClusterNode.hostId || itemClusterNode.hostId !== item.hostId)
                    && (!itemClusterNode.hostUserId || itemClusterNode.hostUserId !== item.hostUserId)) {
                    updateClustertaskNode({
                      "clusterNodeId": item.clusterNodeId,
                      "clusterId": item.clusterId,
                      "hostId": item.hostId,
                      "hostUserId": item.hostUserId,
                      "nodeType": item.nodeType,
                      "dataPath": item.dataPath,
                      "azOwner": item.azOwner,
                      "azPriority": item.azPriority,
                      "isCMMaster": item.isCMMaster,
                      "cmDataPath": item.cmDataPath,
                      "cmPort": item.cmPort
                    }) .then((response) => {
                      if(Number(response.code) !== 200) {
                        console.error(response)
                        clusterSaveFlag = false
                      } else {
                        countUpdateFlag = countUpdateFlag + 1
                      }
                    }) .catch((error) => {
                      console.error('updateClusterNode' + error)
                      clusterSaveFlag = false
                    }) .finally(() => {
                      if (clusterSaveFlag) {
                        saveFlag.value = true
                      } else {
                        saveFlag.value = false
                      }
                    })
                  } else {
                    countUpdateFlag = countUpdateFlag + 1
                  }
                } else{
                  createClustertaskNode({
                    "clusterNodeId": '',
                    "clusterId": clusterId.value,
                    "hostId":item.hostId,
                    "hostUserId": item.hostUserId,
                    "nodeType": item.nodeType,
                    "dataPath": item.dataPath,
                    "azOwner": item.azOwner,
                    "azPriority": item.azPriority,
                    "isCMMaster": item.isCMMaster,
                    "cmDataPath": item.cmDataPath,
                    "cmPort": item.cmPort
                  }) .then((res) => {
                    if(Number(res.code) !== 200) {
                      console.error(response.data)
                      clusterSaveFlag = false
                    } else {
                      countUpdateFlag = countUpdateFlag + 1
                    }
                  }) .catch((error) => {
                    console.error(error)
                  }) .finally(() => {
                    if (clusterSaveFlag) {
                      saveFlag.value = true
                    } else {
                      saveFlag.value = false
                    }
                  })
                }
              })
              if (subTaskConfig.value.deployType === "SINGLE_NODE" && clusterTaskList.clusterNodes.length > 1){
                Message.error('当前选择单节点模式，请删除多余节点')
              }
            }
          }) .catch((error) => {
            console.error((error))
            clusterSaveFlag = false
            saveFlag.value = false
          }) .finally(() => {
            if (clusterSaveFlag) {
              saveFlag.value = true
            } else {
              saveFlag.value = false
            }
          })
        } else {
          Message.error('保存草稿箱失败')
        }
      }).catch((error) => {
        Message.error('保存草稿箱失败' + error)
      })
    }
    if (saveFlag.value) {
      Message.success('保存草稿箱成功')
    }
  }
}

const saveConfig = async () => {
  let tempFlag = false
  tempFlag = clusterTaskList.installPath === subTaskConfig.value.installPath ? true : false
  tempFlag = clusterTaskList.installPackagePath === subTaskConfig.value.installPackagePath ? tempFlag && true : false
  tempFlag = clusterTaskList.logPath === subTaskConfig.value.logPath ? tempFlag && true : false
  tempFlag = clusterTaskList.tmpPath === subTaskConfig.value.tmpPath ? tempFlag && true : false
  tempFlag = clusterTaskList.omToolsPath === subTaskConfig.value.omToolsPath ? tempFlag && true : false
  tempFlag = clusterTaskList.corePath === subTaskConfig.value.corePath ? tempFlag && true : false
  tempFlag = clusterTaskList.envPath === subTaskConfig.value.envPath ? tempFlag && true : false
  clusterTaskList.clusterNodes = []
  editFlag.value = true
  subTaskConfig.value.clusterNodes.forEach((item) => {
    if (item.editing === true) {
      editFlag.value = false
      Message.error('仍有节点处于编辑状态，无法保存')
    }
    const newData = {
      "clusterNodeId": item.clusterNodeId,
      "clusterId": item.clusterId,
      "hostId": item.hostId,
      "hostUserId": item.hostUserId,
      "nodeType": item.nodeType,
      "dataPath": item.dataPath,
      "azOwner": item.azOwner,
      "azPriority": item.azPriority,
      "isCMMaster": item.isCMMaster,
      "cmDataPath": item.cmDataPath,
      "cmPort": item.cmPort
    }
    clusterTaskList.clusterNodes.push(newData)
  })
  if (currentStep.value === 1) {
    const formRuleCheck = stepOneComp.value
    const isValid = formRuleCheck.validateAllFields()
    if (isValid && editFlag.value) {
      try {
        await saveUpdateCulster()
      } catch (error) {
        console.error('Error executing parts:', error)
      }
    }
  } else {
    Message.success('保存草稿箱成功')
    saveFlag.value = true
  }
}

const routerToCluster = useRouter()

const submitClusterInfo = () => {
  submitCluster(clusterId.value) .then((res) => {
    res.code === 200 ?Message.success('提交成功'):Message.error('提交失败')
    routerToCluster.push({ name: 'DailyOps', params: { status: 'task' } })
  }) .catch((error) => {
    console.error(error)
  })
}

const tempClusterId = ref('')
const createClusterId = ref('')
const init = () => {
  currentStep.value = 1
  const tempRecord = route.params.record?JSON.parse(route.params.record):{}
  if (tempRecord.clusterId) {
    tempClusterId.value = tempRecord.clusterId
    saveFlag.value=true
  } else {
    saveFlag.value = false
    tempClusterId.value = ''
  }
  clusterId.value = ''
  createClusterId.value = ''
}

const route = useRoute()
onMounted(() => {
  init()
})
const previousPath = ref(route.fullPath)
watch(() => route.fullPath, (newPath) => {
  if (newPath !== previousPath.value) {
    previousPath.value = newPath
    init()
  }
}, { immediate: true })

const backToIndex = () => {
  router.push({ name:'DailyOps'})
}
</script>

<style lang="less" scoped>
.home-container {
  position: relative;
  height: calc(100vh - 123px);
  overflow: hidden;
  .main-con {
    height: calc(100vh - 123px);
    padding-bottom: 40px;
    overflow-y: auto;
  }
  .title-con {
    padding: 20px 20px 0;
    display: flex;
    align-items: center;
    .title {
      font-size: 20px;
      color: var(--color-text-1);
    }
    .task-name-con {
      margin-left: 40px;
      display: flex;
      justify-content: center;
      align-items: center;
      .task-name {
        color: var(--color-text-1);
        white-space: nowrap;
        margin-right: 10px;
        display: flex;
        align-items: center;
        i {
          font-size: 16px;
          margin-right: 3px;
          font-style: normal;
          color: rgb(var(--danger-6));
        }
      }
    }
  }
  .task-steps-con {
    width: 80%;
    margin: 30px auto;
  }
  .submit-con {
    position: fixed;
    height:auto;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 10;
    background: var(--color-bg-2);
    display: flex;
    justify-content: right;
    .btn-item {
      margin: 10px;
    }
  }
}
.step_title{
  .word_style {
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .h1 {
    font-size: large;
  }
  .p {
    font-size: medium;
  }
  .rectangle {
    width: calc(33.33% - 20px);
    height: 32px;
    background-color: red;
    margin: 0;
    float: left;
  }
}

</style>
