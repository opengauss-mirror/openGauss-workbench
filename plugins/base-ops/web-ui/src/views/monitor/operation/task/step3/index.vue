<template>
  <div class="clusterManage">
    <div class="hostInfo">
      <span style="font-weight: bold;">安装包配置</span><br><br>
      <table>
        <tr>
          <td class="key">安装包名称</td>
          <td class="value">{{ props.clusterTaskList.packageName }}</td>
          <td class="key">操作系统</td>
          <td class="value">{{ props.clusterTaskList.cpuArch }}</td>
          <td class="key">系统架构</td>
          <td class="value">{{ props.clusterTaskList.os }}</td>
        </tr>
        <tr>
          <td class="key">openGauss版本</td>
          <td class="value">
            <div v-if="props.clusterTaskList.version === OpenGaussVersionEnum.ENTERPRISE">
              企业版
            </div>
            <div v-if="props.clusterTaskList.version === OpenGaussVersionEnum.LITE">
              轻量版
            </div>
            <div v-if="props.clusterTaskList.version === OpenGaussVersionEnum.MINIMAL_LIST">
              极简版
            </div>
          </td>
          <td class="key">openGauss版本号</td>
          <td class="value">{{ props.clusterTaskList.versionNum }}</td>
          <td class="key">所选安装包</td>
          <td class="value">{{ tempPackageData.fileName }}</td>
        </tr>
      </table>
    </div>
    <br>
    <div class="clusterInfo">
      <span style="font-weight: bold;">集群配置</span><br><br>
      <table>
        <tr>
          <td class="key">集群标识</td>
          <td class="value">{{ props.clusterTaskList.clusterName }}</td>
          <td class="key">内核架构</td>
          <td class="value" v-if="props.clusterTaskList.deployType === 'CLUSTER'">单主架构</td>
          <td class="value" v-else>主备架构</td>
          <td class="key">安装目录</td>
          <td class="value">{{ props.clusterTaskList.installPath }}</td>
        </tr>
        <tr>
          <td class="key">软件包路径</td>
          <td class="value">{{ props.clusterTaskList.installPackagePath }}</td>
          <td class="key">端口号</td>
          <td class="value">{{ props.clusterTaskList.port }}</td>
          <td class="key">数据库密码</td>
          <td class="value">
            <span v-if="data.visible">******</span>
            <span v-if="!data.visible">{{ props.clusterTaskList.databasePassword }}</span>
            <button @click="changeVisible">{{data.visible?'显示':'隐藏'}} </button>
          </td>
        </tr>
        <tr>
          <td class="key">是否安装CM</td>
          <td class="value">{{ props.clusterTaskList.enableCmTool }}</td>
          <td class="key">是否环境分离</td>
          <td class="value">{{ props.clusterTaskList.enableGenerateEnvironmentVariableFile }}</td>
          <td class="key">环境分离路径</td>
          <td class="value">{{ props.clusterTaskList.envPath }}</td>
        </tr>
        <tr v-if="props.clusterTaskList.version === OpenGaussVersionEnum.ENTERPRISE">
          <td class="key">日志目录</td>
          <td class="value">{{ props.clusterTaskList.logPath }}</td>
          <td class="key">临时文件目录</td>
          <td class="value">{{ props.clusterTaskList.tmpPath }}</td>
          <td class="key">数据库工具目录</td>
          <td class="value">{{ props.clusterTaskList.omToolsPath }}</td>
        </tr>
        <tr v-if="props.clusterTaskList.version === OpenGaussVersionEnum.ENTERPRISE">
          <td class="key">数据库core目录</td>
          <td class="value">{{ props.clusterTaskList.corePath }}</td>
          <td class="key"></td>
          <td class="value"></td>
          <td class="key"></td>
          <td class="value"></td>
        </tr>
      </table>
    </div>
    <br>
    <div class="nodeInfo">
      <span style="font-weight: bold;">节点配置</span><br><br>
      <div v-for="(item, index) in list.nodes" >
        <span style="font-weight: bold;">节点{{index+1}}</span><br><br>
        <table>
          <tr>
            <td class="key">节点类型</td>
            <td class="value">{{item.nodeType}}</td>
            <td class="key">IP地址</td>
            <td class="value">{{item.hostIp}}</td>
            <td class="key">安装用户</td>
            <td class="value">{{props.clusterTaskList.databaseUsername}}</td>
          </tr>
          <tr>
            <td class="key">数据路径</td>
            <td class="value">{{item.dataPath}}</td>
            <td class="key">所属AZ</td>
            <td class="value">{{item.azOwner}}</td>
            <td class="key">AZ优先级</td>
            <td class="value">{{item.azPriority}}</td>
          </tr>
          <tr v-if="props.clusterTaskList.enableCmTool">
            <td class="key">CM节点类型</td>
            <td class="value" v-if="item.isCMMaster === true">主节点</td>
            <td class="value" v-else>备节点</td>
            <td class="key">CM路径</td>
            <td class="value">{{item.cmDataPath}}</td>
            <td class="key">CM端口号</td>
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
import axios from "axios"
import { defineProps } from 'vue'
import {OpenGaussVersionEnum} from "@/types/ops/install";
import {getHostIp, getPackageList} from "@/api/ops";
import {Message} from "@arco-design/web-vue";

const props = defineProps({
  message: Array,
  clusterTaskList : Object
})

const route = useRoute();
console.log(route.query.clusterId)

const data = reactive({
  selectedOption: 'colony_name01',
  visible: true,
});

const changeVisible = () => {
  data.visible = !data.visible;
}

const list = reactive({
  nodes: []
})
const tempPackageData = reactive({
  name:'',
  fileName: ''
})
const getListData = () => {
  getPackageList({
    name: '',
    os: props.clusterTaskList.os,
    cpuArch: props.clusterTaskList.cpuArch,
    openGaussVersion: props.clusterTaskList.version,
    openGaussVersionNum: props.clusterTaskList.versionNum,
  }).then((res) => {
    res.forEach((item) => {
      if (item.packageId === props.clusterTaskList.packageId) {
        tempPackageData.name = res.data.name
        tempPackageData.fileName = res.fileName
      }
      console.log('166')
      console.log(tempPackageData)
    })
  }).catch(error => {
    console.error("taskMenu infoError:"+error);
  })
}
const hostIdIp = new FormData
const hostPuPr = new FormData
const getClusterData = () => {
  console.log(props.message)
  const param = {
    os: '',
    osVersion: '',
    cpuArch: ''
  }
  getHostIp(param).then((res) => {
    if (res.code === 200) {
      res.data.forEach(item => {hostIdIp.append(item.hostId, item.publicIp)})
      res.data.forEach(item => {hostPuPr.append(item.publicIp,item.privateIp)})
    } else {
      Message.error({
        content: '获取ip失败'
      })
    }
  }) .catch((error) => {
    console.error(error)
  }) .finally(() => {
    list.nodes = []
    props.message.forEach((item, index) => {
      let tempPublicIp = hostIdIp.get(item.hostId)
      const newData = {
        "order" : index,
        "hostId": item.hostId,
        "hostIp": tempPublicIp,
        "hostUser": item.hostUser,
        "hostUserId": item.hostUserId,
        "nodeType": item.nodeType,
        "dataPath": item.dataPath,
        "azOwner": item.azOwner,
        "azPriority": item.azPriority,
        "isCMMaster": item.isCMMaster,
        "cmDataPath": item.cmDataPath,
        "cmPort": item.cmPort
      }
      list.nodes.push(newData)
    })
  })
}

  onMounted(() => {
  getListData()
  getClusterData()
})

</script>

<style scoped>
.clusterManage {
  background-color: #f4f6fa;
  height: auto;
}
.header {
  background-color: #fdfeff;
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
}
.hostInfo {
  width: 98%;
  height: 15%;
  background-color: #fdfeff;
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
  background-color: #fdfeff;
  margin-left: 1%;
  margin-right: 1%;
  padding-left: 1%;
  padding-top: 1%;
}
.nodeInfo {
  width: 98%;
  height: 30%;
  background-color: #fdfeff;
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
