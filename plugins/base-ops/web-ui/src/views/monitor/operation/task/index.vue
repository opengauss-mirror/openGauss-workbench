<template>
  <div class="home-container" id="opTask">
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
             :clusterNodeList="createClusternodeList"
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
             :clusterTaskList="clusterTaskList"
             :createClusterId="createClusterId"/>
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
  createClusterTask, createClustertaskNode, envCheckResult, pathEmpty,
  submitCluster,
  updateClusterTask,
  updateClustertaskNode,
  checkPkg
} from "@/api/ops";
import {useRoute, useRouter} from "vue-router";
import {OpenGaussVersionEnum} from "@/types/ops/install";
import router from "@/router";
import {map} from "lodash";
import { encryptPassword } from "@/utils/jsencrypt";

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
          checkPkg(subTaskConfig.value.packageId).then(res=>{
            if(!res.data){
              Message.error("安装包检查未通过，可能会影响后续流程")
            }else{
              currentStep.value = 2
              saveFlag.value = false
            }
          }).catch(err=>{
            Message.error("安装包检查未通过，可能会影响后续流程")
          })
        } else {
          Message.error('检测未通过')
        }
      }) .catch ((error) => {
        console.error('检测未通过', error)
      })
    }
  } else if (currentStep.value === 2) {
    envCheckResult(clusterId.value || tempClusterId.value).then((res) =>{
      if (Number(res.code) === 200 ) {
        if (res.data.result === "SUCCESS" ){
          createClusterId.value = clusterId.value
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

const createClusterNode = async (clusterId:string, hostId:string, hostUserId:string, nodeType:string, dataPath:string,
                                 azOwner:string, azPriority:string, isCMMaster:boolean, cmDataPath:string, cmPort:number) => {
  return new Promise((resolve) => {
    createClustertaskNode({
      "clusterNodeId": '',
      "clusterId": clusterId,
      "hostId":hostId,
      "hostUserId": hostUserId,
      "nodeType": nodeType,
      "dataPath": dataPath,
      "azOwner": azOwner,
      "azPriority": azPriority,
      "isCMMaster": isCMMaster,
      "cmDataPath": cmDataPath,
      "cmPort": cmPort
    }) .then((res) => {
      if(Number(res.code) !== 200) {
        resolve(false)
      } else {
        resolve(res.msg)
      }
    }) .catch((error) => {
      console.error(error)
      resolve(false)
    })
  })
}

const updateClusterNode = async (clusterNodeId:string, clusterId:string, hostId:string, hostUserId:string, nodeType:string,
                                 dataPath:string, azOwner:string, azPriority:string, isCMMaster:boolean, cmDataPath:string, cmPort:number) => {
  return new Promise((resolve) => {
    updateClustertaskNode({
      "clusterNodeId": clusterNodeId,
      "clusterId": clusterId,
      "hostId":hostId,
      "hostUserId": hostUserId,
      "nodeType": nodeType,
      "dataPath": dataPath,
      "azOwner": azOwner,
      "azPriority": azPriority,
      "isCMMaster": isCMMaster,
      "cmDataPath": cmDataPath,
      "cmPort": cmPort
    }) .then((res) => {
      if(Number(res.code) !== 200) {
        resolve(false)
      } else {
        resolve(true)
      }
    }) .catch((error) => {
      console.error(error)
      resolve(false)
    })
  })
}

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
      clusterTaskList.databasePassword = await encryptPassword(subTaskConfig.value.databasePassword)
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
      }).then(async (res) => {
        if (res.code === 200) {
          clusterId.value = res.msg
          createClusterId.value = clusterId.value
          saveFlag.value = true
          for (const item of clusterTaskList.clusterNodes) {
            if (item.nodeType === 'MASTER') {
              const masterUserId=item.hostUserId || subTaskConfig.value.hostUserId;
              const masterHostId=item.hostId || subTaskConfig.value.hostId;
              const result = await createClusterNode(clusterId.value, masterHostId, masterUserId, item.nodeType,
                item.dataPath, item.azOwner, item.azPriority, item.isCMMaster, item.cmDataPath, item.cmPort)
              if (result !== false) {
                item.clusterNodeId = result
                saveFlag.value = saveFlag.value && true
              } else {
                saveFlag.value = saveFlag.value && result
              }
            }
          }
          for (const item of clusterTaskList.clusterNodes) {
            if (item.nodeType !== 'MASTER') {
              const result = await createClusterNode(clusterId.value, item.hostId, item.hostUserId, item.nodeType,
                item.dataPath, item.azOwner, item.azPriority, item.isCMMaster, item.cmDataPath, item.cmPort)
              if (result !== false) {
                item.clusterNodeId = result
                saveFlag.value = saveFlag.value && true
              } else {
                saveFlag.value = saveFlag.value && result
              }
            }
          }
        } else {
          Message.error('保存草稿箱失败')
        }
      }) .catch((error) => {
        Message.error('保存草稿箱失败' + error)
      }) .finally(() => {
        if (subTaskConfig.value.deployType === "SINGLE_NODE" && clusterTaskList.clusterNodes.length > 1){
          Message.error('当前选择单节点模式，请删除多余节点')
          saveFlag.value = false
        }
        if (saveFlag.value) {
          const tempClusternodeList = new Map()
          clusterTaskList.clusterNodes.forEach((item:any) => {tempClusternodeList.set(item.hostId, item.clusterNodeId)})
          createClusternodeList.value = tempClusternodeList
          Message.success('保存草稿箱成功')
        }
      })
    }
  } else if (editFlag.value && currentStep.value === 1 && subTaskConfig.value.clusterId !== '') {
    if (subTaskConfig.value.deployType === "CLUSTER"
      && subTaskConfig.value.packageVersion !== OpenGaussVersionEnum.MINIMAL_LIST
      && clusterTaskList.clusterNodes.length < 2) {
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
      clusterTaskList.databasePassword = await encryptPassword(subTaskConfig.value.databasePassword)
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
          batchClusterNodes(clusterId.value).then(async (res) => {
            if (res.code === 200) {
              saveFlag.value= true
              const clusterNodesMap = new Map(res.data.clusterNodes.map(item => [item.clusterNodeId, item]))
              const nodesIpMap = new Map(res.data.clusterNodes.map(item => [item.hostId, item]))
              for (const item of clusterTaskList.clusterNodes) {
                if (item.clusterNodeId) {
                  const itemClusterNode = clusterNodesMap.get(item.clusterNodeId)
                  if (itemClusterNode
                    && (!itemClusterNode.hostId || itemClusterNode.hostId !== item.hostId)
                    && (!itemClusterNode.hostUserId || itemClusterNode.hostUserId !== item.hostUserId)) {
                    const hostUserId = item.nodeType === 'MASTER' && !item.hostUserId?subTaskConfig.value.hostUserId:item.hostUserId;
                    const hostId = item.nodeType === 'MASTER' && !item.hostId?subTaskConfig.value.hostId:item.hostId;
                    const result = await updateClusterNode(item.clusterNodeId, clusterId.value, hostId, hostUserId,
                      item.nodeType, item.dataPath, item.azOwner, item.azPriority, item.isCMMaster, item.cmDataPath, item.cmPort)
                    saveFlag.value = saveFlag.value && result
                  }
                } else if (!nodesIpMap.get(item.hostId)) {
                  const result = await createClusterNode(clusterId.value, item.hostId, item.hostUserId, item.nodeType,
                    item.dataPath, item.azOwner, item.azPriority, item.isCMMaster, item.cmDataPath, item.cmPort)
                  if (result !== false) {
                    item.clusterNodeId = result
                    saveFlag.value = saveFlag.value && true
                  } else {
                    saveFlag.value = saveFlag.value && result
                  }
                }
              }
            if (saveFlag.value) {
              Message.success('保存草稿箱成功')
            }
            }
          }) .catch((error) => {
            console.error((error))
          })
        } else {
          Message.error('保存草稿箱失败')
        }
      }).catch((error) => {
        Message.error('保存草稿箱失败' + error)
      })  .finally(() => {
        if (subTaskConfig.value.deployType === "SINGLE_NODE" && clusterTaskList.clusterNodes.length > 1){
          Message.error('当前选择单节点模式，请删除多余节点')
          saveFlag.value = false
        }
        if (saveFlag.value) {
          const tempClusternodeList = new Map()
          clusterTaskList.clusterNodes.forEach((item:any) => {tempClusternodeList.set(item.hostId, item.clusterNodeId)})
          createClusternodeList.value = tempClusternodeList
          Message.success('保存草稿箱成功')
        }
      })
    }
  }
}

const saveConfig = async () => {
  clusterTaskList.clusterNodes = []
  editFlag.value = true
  subTaskConfig.value.clusterNodes.forEach((item) => {
    if (item.editing && subTaskConfig.value.packageVersion !== OpenGaussVersionEnum.MINIMAL_LIST) {
      editFlag.value = false
      Message.error('仍有节点处于编辑状态，无法保存')
      return
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
    try{
    const res = await checkPkg(subTaskConfig.value.packageId)
    if(!res.data) {
      Message.error("安装包检查未通过，可能会影响流程")
      return
      }
    } catch (error) {
      Message.error("安装包检查未通过，可能会影响流程")
      return
    }
    await checkPathsEmpty()
    if (isValid && editFlag.value && pathIsEmpty.value) {
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
//检查所有目录是否为空目录
const pathIsEmpty = ref(true);
const list = reactive({
  pathList: []
})
const checkPathsEmpty = async () => {
  pathIsEmpty.value = true;
  list.pathList = [];
  if (subTaskConfig.value.installPath !== '') {
    list.pathList.push(subTaskConfig.value.installPath)
  }
  if (subTaskConfig.value.installPackagePath !== '') {
    list.pathList.push(subTaskConfig.value.installPackagePath)
  }
  if (subTaskConfig.value.packageVersion === OpenGaussVersionEnum.ENTERPRISE) {
    if (subTaskConfig.value.logPath !== '') {
      list.pathList.push(subTaskConfig.value.logPath)
    }
    if (subTaskConfig.value.tmpPath !== '') {
      list.pathList.push(subTaskConfig.value.tmpPath)
    }
    if (subTaskConfig.value.omToolsPath !== '') {
      list.pathList.push(subTaskConfig.value.omToolsPath)
    }
  }
  if (subTaskConfig.value.envPath !== '' && subTaskConfig.value.enableGenerateEnvironmentVariableFile) {
    list.pathList.push(subTaskConfig.value.envPath)
  }
  const promises = subTaskConfig.value.clusterNodes.map(node => {
    let promisesArray = [];
    if (node.dataPath !== '' && subTaskConfig.value.packageVersion !== OpenGaussVersionEnum.MINIMAL_LIST) {
      list.pathList.push(node.dataPath)
    }
    if (node.cmDataPath !== '' && subTaskConfig.value.packageVersion === OpenGaussVersionEnum.ENTERPRISE && subTaskConfig.value.enableCmTool) {
      list.pathList.push(node.cmDataPath)
    }
    list.pathList.map(item => {
      const data = {
        path: item
      }
      promisesArray.push(pathEmpty(node.hostId, data).then(res => {
        if (Number(res.code) === 200) {
          if (!res.data) {
            pathIsEmpty.value = false;
            Message.error(node.hostIp + "的路径：" + item + " 不为空, 保存草稿失败")
          }
        }
      }).catch(error => {
        console.log(error);
        throw error;
      }));
    });
    if (node.dataPath !== '' && subTaskConfig.value.packageVersion !== OpenGaussVersionEnum.MINIMAL_LIST) {
      list.pathList.pop();
    }
    if (node.cmDataPath !== '' && subTaskConfig.value.packageVersion === OpenGaussVersionEnum.ENTERPRISE && subTaskConfig.value.enableCmTool) {
      list.pathList.pop();
    }
    return Promise.all(promisesArray);
  });

  try {
    await Promise.all(promises);
    // 所有异步操作完成
    console.log("所有路径检查完成");
  } catch (error) {
    console.log("路径检查过程中发生错误：", error);
  }
}

const tempClusterId = ref('')
const createClusterId = ref('')
const createClusternodeList = ref(new Map())
const init = () => {
  currentStep.value = 1
  const tempRecord = route.params.record?JSON.parse(route.params.record):{}
  if (tempRecord.clusterId) {
    tempClusterId.value = tempRecord.clusterId
  } else {
    tempClusterId.value = ''
  }
  saveFlag.value = false
  clusterId.value = ''
  createClusterId.value = ''
  createClusternodeList.value = new Map()
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
