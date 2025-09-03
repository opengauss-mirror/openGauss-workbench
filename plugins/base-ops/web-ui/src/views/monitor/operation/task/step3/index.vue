<template>
  <div class="clusterManage" id="taskStep3">
    <div class="hostInfo">
      <span style="font-weight: bold;">{{ $t('operation.task.step1.index.packageConfig') }}</span><br><br>
      <table>
        <tr>
          <td class="key">{{ $t('packageManage.AddPackageDlg.5myq5c8zpu93') }}</td>
          <td class="value">{{ list.packageName }}</td>
          <td class="key">{{ $t('operation.task.step1.index.system') }}</td>
          <td class="value">{{ list.cpuArch }}</td>
          <td class="key">{{ $t('operation.task.step1.index.cpuArch') }}</td>
          <td class="value">{{ list.os }}</td>
        </tr>
        <tr>
          <td class="key">{{ $t('operation.task.step1.index.version') }}</td>
          <td class="value">
            <div v-if="list.packageVersion === OpenGaussVersionEnum.ENTERPRISE">
              {{ $t('operation.task.step1.index.ENTERPRISE') }}
            </div>
            <div v-if="list.packageVersion === OpenGaussVersionEnum.LITE">
              {{ $t('operation.task.step1.index.LITE') }}
            </div>
            <div v-if="list.packageVersion === OpenGaussVersionEnum.MINIMAL_LIST">
              {{ $t('operation.task.step1.index.MINIMAL_LIST') }}
            </div>
          </td>
          <td class="key">{{ $t('operation.task.step1.index.versionNum') }}</td>
          <td class="value">{{ list.packageVersionNum }}</td>
          <td class="key">{{ $t('operation.task.step1.index.selecPack') }}</td>
          <td class="value">{{ list.packageName }}</td>
        </tr>
      </table>
    </div>
    <br>
    <div class="clusterInfo">
      <span style="font-weight: bold;">{{ $t('operation.task.step1.index.clusterConfig') }}</span><br><br>
      <table>
        <tr>
          <td class="key">{{ $t('operation.task.step1.index.clusterName') }}</td>
          <td class="value">{{ list.clusterName }}</td>
          <td class="key">{{ $t('enterprise.ClusterConfig.test0') }}</td>
          <td class="value" v-if="list.deployType === 'CLUSTER'">{{ $t('enterprise.ClusterConfig.option3') }}</td>
          <td class="value" v-else>{{ $t('enterprise.ClusterConfig.option1') }}</td>
          <td class="key">{{ $t('enterprise.ClusterConfig.5mpm3ku3hv40') }}</td>
          <td class="value">{{ list.installPath }}</td>
        </tr>
        <tr>
          <td class="key">{{ $t('operation.task.step1.index.installPackagePath') }}</td>
          <td class="value">{{ list.installPackagePath }}</td>
          <td class="key">{{ $t('operation.task.step1.index.port') }}</td>
          <td class="value">{{ list.port }}</td>
          <td class="key">{{ $t('operation.task.step1.index.dbPwd') }}</td>
          <td class="value">
            <span v-if="data.visible">******</span>
            <span v-if="!data.visible">{{ $t('enterprise.ClusterConfig.passwdWarning') }}</span>
            <button @click="changeVisible">{{data.visible? $t('operation.task.step1.index.visible') :$t('operation.task.step1.index.hide')}} </button>
          </td>
        </tr>
        <tr>
          <td class="key">{{ $t('operation.task.step1.index.checkflagCM') }}</td>
          <td class="value">{{list.enableCmTool }}</td>
          <td class="key">{{ $t('operation.task.step1.index.checkflagEnvSeqar') }}</td>
          <td class="value">{{ list.enableGenerateEnvironmentVariableFile }}</td>
          <td class="key">{{ $t('operation.task.step1.index.envPath') }}</td>
          <td class="value">{{ list.envPath }}</td>
        </tr>
        <tr v-if="list.packageVersion === OpenGaussVersionEnum.ENTERPRISE">
          <td class="key">{{ $t('operation.task.step1.index.logPath') }}</td>
          <td class="value">{{ list.logPath }}</td>
          <td class="key">{{ $t('operation.task.step1.index.tmpPath') }}</td>
          <td class="value">{{ list.tmpPath }}</td>
          <td class="key">{{ $t('operation.task.step1.index.omToolsPath') }}</td>
          <td class="value">{{ list.omToolsPath }}</td>
        </tr>
        <tr v-if="props.clusterTaskList.version === OpenGaussVersionEnum.ENTERPRISE">
          <td class="key">{{ $t('operation.task.step1.index.corePath') }}</td>
          <td class="value">{{ list.corePath }}</td>
          <td class="key"></td>
          <td class="value"></td>
          <td class="key"></td>
          <td class="value"></td>
        </tr>
      </table>
    </div>
    <br>
    <div class="nodeInfo">
      <span style="font-weight: bold;">{{ $t('operation.task.step1.index.nodeConfig') }}</span><br><br>
      <div v-for="(item, index) in list.nodes" >
        <span style="font-weight: bold;">{{ $t('wdr.GenerateWdrDlg.5mpm0eufzyg0') }}{{index+1}}</span><br><br>
        <table>
          <tr>
            <td class="key">{{ $t('operation.task.step1.index.clusterType') }}</td>
            <td class="value">{{item.nodeType}}</td>
            <td class="key">{{ $t('logCenter.index.else1') }}</td>
            <td class="value">{{item.hostIp}}</td>
            <td class="key">{{ $t('operation.task.step1.index.installUser') }}</td>
            <td class="value">{{props.clusterTaskList.databaseUsername}}</td>
          </tr>
          <tr>
            <td class="key">{{ $t('operation.task.step1.index.dataPath') }}</td>
            <td class="value">{{item.dataPath}}</td>
            <td class="key">{{ $t('operation.task.step1.index.azOwner') }}</td>
            <td class="value">{{item.azOwner}}</td>
            <td class="key">{{ $t('operation.task.step1.index.azPriority') }}</td>
            <td class="value">{{item.azPriority}}</td>
          </tr>
          <tr v-if="props.clusterTaskList.enableCmTool">
            <td class="key">CM{{ $t('operation.task.step1.index.clusterType') }}</td>
            <td class="value" v-if="item.isCMMaster === true">{{ $t('operation.task.step1.index.clusterMaster') }}</td>
            <td class="value" v-else>{{ $t('operation.task.step1.index.clusterOthers') }}</td>
            <td class="key">{{ $t('operation.task.step1.index.cmDataPath') }}</td>
            <td class="value">{{item.cmDataPath}}</td>
            <td class="key">{{ $t('operation.task.step1.index.cmPort') }}</td>
            <td class="value">{{item.cmPort}}</td>
          </tr>
        </table>
        <div class="dashed-line"></div>
      </div>
    </div><br>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router';
import {KeyValue} from "@/types/global";
import { defineProps } from 'vue'
import {OpenGaussVersionEnum} from "@/types/ops/install";
import {batchClusterNodes, getHostIp, getPackageList} from "@/api/ops";
import { encryptPassword } from "@/utils/jsencrypt";

const props = defineProps({
  message: Array,
  clusterTaskList : Object,
  createClusterId : Object
})

const route = useRoute();

const data = reactive({
  selectedOption: 'colony_name01',
  visible: true,
});

const changeVisible = () => {
  data.visible = !data.visible;
}

const list = reactive({
  hostIp: '',
  hostId: "",
  hostUser: "",
  hostUserId: '',
  os: '',
  osVersion: '',
  cpuArch: '',
  basePath: '',
  packageVersion: '',
  packageVersionNum: '',
  packageId:'',
  packageName:'',
  clusterId:'',
  clusterName:'',
  databasePassword: "",
  port: 5432,
  deployType: "",
  installPath: "",
  installPackagePath: "",
  logPath: "",
  tmpPath: "",
  omToolsPath: "",
  corePath: "",
  envPath: "",
  enableCmTool: false,
  enableGenerateEnvironmentVariableFile: false,
  xmlConfigPath: "",
  nodes: []
})
const tempPackageData = reactive({
  name:'',
  fileName: ''
})

const hostIdIp = new FormData
const hostPuPr = new FormData

const init = () => {
  batchClusterNodes(props.createClusterId) .then(async (res) => {
    if (res.code === 200) {
      res.data.clusterNodes.forEach((item) => {
        const newData = {
          "clusterId": item.clusterId,
          "clusterNodeId": item.clusterNodeId,
          "hostId": item.hostId,
          "hostIp": item.displayHostIp,
          "hostUserId": item.hostUserId,
          "hostUser": item.hostUsername,
          "nodeType": item.nodeType,
          "dataPath": item.dataPath,
          "azOwner": item.azOwner,
          "azPriority": item.azPriority,
          "isCMMaster": item.isCmMaster,
          "cmDataPath": item.cmDataPath,
          "cmPort": item.cmPort,
          "editing": (!item.hostId && res.data.version !== OpenGaussVersionEnum.MINIMAL_LIST)
        }
        list.nodes.push(newData)
      })
      list.clusterId = props.clusterId
      list.os = res.data.os
      list.cpuArch = res.data.cpuArch
      list.packageVersion = res.data.version
      list.packageVersionNum = res.data.versionNum
      list.packageName = res.data.packageName
      list.packageId = res.data.packageId
      list.clusterName = res.data.clusterName
      list.databasePassword = res.data.databasePassword
      list.port = Number(res.data.databasePort)
      list.installPackagePath = res.data.installPackagePath
      list.installPath = res.data.installPath
      list.logPath = res.data.logPath
      list.tmpPath = res.data.tmpPath
      list.omToolsPath = res.data.omToolsPath
      list.corePath = res.data.corePath
      list.envPath = res.data.envPath
      list.enableCmTool = res.data.enableCmTool
      list.enableGenerateEnvironmentVariableFile = res.data.enableGenerateEnvironmentVariableFile
      list.xmlConfigPath = res.data.xmlConfigPath
      list.deployType = res.data.deployType
      list.hostIp = res.data.displayHostIp
      list.hostUser = res.data.hostUsername
      list.hostUserId = res.data.hostUserId
      list.hostId = res.data.hostId
      console.log(list)
    }
  }) .catch((error) => {
    console.error(error)
  })
}
onMounted(() => {
  init()
})

</script>

<style scoped>
.clusterManage {
  background-color: var(--color-neutral-2);
  height: auto;
  span {
    color:var(--color-neutral-10)
  }
}
.header {
  background-color: var(--color-bg-1);
  padding: 1% 1% 0 1%
}
.operation {
  float: right;
}
.clusterName {
  font-size: 20px;
  font-weight: bold;
  padding-bottom: 20px;
}
.execute {
  background-color: #0077ff !important;
  color: white;
  border-radius: 2px;
}
table {
  line-height: 37px;
  td {
    color:var(--color-neutral-10)
  }
}
.hostInfo {
  width: 98%;
  height: 15%;
  background-color: var(--color-bg-1);
  margin-left: 1%;
  margin-right: 1%;
  padding-left: 1%;
  padding-top: 1%;
}
.key {
  width: 150px;
}
.value {
  width: 590px;
}
.clusterInfo {
  width: 98%;
  height: 30%;
  background-color: var(--color-bg-1);
  margin-left: 1%;
  margin-right: 1%;
  padding-left: 1%;
  padding-top: 1%;
}
.nodeInfo {
  width: 98%;
  height: 30%;
  background-color: var(--color-bg-1);
  margin-left: 1%;
  margin-right: 1%;
  padding-left: 1%;
  padding-top: 1%;
}
.dashed-line {
  border-top: 1px dashed #d5d9e4; /* 黑色虚线上边框 */
  width: 100%; /* 宽度100% */
  margin: 20px 0; /* 上下外边距 */
}
</style>
