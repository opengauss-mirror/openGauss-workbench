<template>
  <div class="clusterManage">
    <div class="header">
      <div>
        <a-link type="primary" @click="clusterManage">集群管理</a-link> /
        <select style="border: 0px;font-weight: bold;">
          <option>{{list.data.clusterName}}</option>
        </select>
      </div>
      <div class="operation">
        <a-link @click="logDownload">执行日志下载</a-link>&nbsp;&nbsp;
        <a-link @click="copy">复制</a-link>&nbsp;&nbsp;
        <a-link @click="editClusterTask">编辑</a-link>&nbsp;&nbsp;
        <a-link @click="singleRowDelete">删除</a-link>&nbsp;&nbsp;
        <a-button class="execute" @click="reExecute">重新执行</a-button>&nbsp;&nbsp;
      </div>
      <p class="clusterName">{{list.data.clusterName}}</p>
    </div>
    <div class="hostInfo">
      <span style="font-weight: bold;">服务器信息</span><br><br>
      <table>
        <tr>
          <td class="key">服务器IP</td>
          <td class="value">{{ list.data.hostIp }}</td>
          <td class="key">安装用户</td>
          <td class="value">{{ list.data.hostUsername }}</td>
          <td class="key">服务器系统</td>
          <td class="value">{{ list.data.os }}</td>
        </tr>
        <tr>
          <td class="key">CPU架构</td>
          <td class="value">{{ list.data.cpuArch }}</td>
          <td class="key"></td>
          <td class="value"></td>
          <td class="key"></td>
          <td class="value"></td>
        </tr>
      </table>
    </div>
    <br>
    <div class="clusterInfo">
      <span style="font-weight: bold;">集群信息</span><br><br>
      <table>
        <tr>
          <td class="key">集群名称</td>
          <td class="value">{{ list.data.clusterName }}</td>
          <td class="key">openGauss版本</td>
          <td class="value" v-if="list.data.version === 'ENTERPRISE'">企业版</td>
          <td class="value" v-if="list.data.version === 'LITE'">轻量版</td>
          <td class="value" v-if="list.data.version === 'MINIMAL_LIST'">极简版</td>
          <td class="key">版本号</td>
          <td class="value">{{ list.data.versionNum }}</td>
        </tr>
        <tr>
          <td class="key">集群节点数量</td>
          <td class="value">
            <span v-if="list.data.deployType === 'CLUSTER' && list.data.clusterNodeNum > 1">
              一主{{list.data.clusterNodeNum - 1}}备
            </span>
            <span v-else>
              单节点
            </span>
          </td>
          <td class="key">集群端口号</td>
          <td class="value">{{ list.data.databasePort }}</td>
          <td class="key">环境变量地址</td>
          <td class="value">{{ list.data.envPath }}</td>
        </tr>
        <tr>
          <td class="key">安装目录</td>
          <td class="value">{{ list.data.installPath }}</td>
          <td class="key">软件包路径</td>
          <td class="value">{{ list.data.installPackagePath }}</td>
          <td class="key">日志目录</td>
          <td class="value">{{ list.data.logPath }}</td>
        </tr>
        <tr>
          <td class="key">临时文件目录</td>
          <td class="value">{{ list.data.tmpPath }}</td>
          <td class="key">数据库工具目录</td>
          <td class="value">{{ list.data.omToolsPath }}</td>
          <td class="key">数据库core目录</td>
          <td class="value">{{ list.data.corePath }}</td>
        </tr>
        <tr>
          <td class="key">是否安装CM</td>
          <td class="value">{{ list.data.enableCmTool }}</td>
          <td class="key">是否环境分离</td>
          <td class="value">{{ list.data.enableGenerateEnvironmentVariableFile }}</td>
          <td class="key" v-if="list.data.enableGenerateEnvironmentVariableFile">环境分离路径</td>
          <td class="value" v-if="list.data.enableGenerateEnvironmentVariableFile">{{ list.data.envPath }}</td>
        </tr>
        <tr>
          <td class="key">数据库密码</td>
          <td class="value">
            <span v-if="data.visible">******</span>
            <span v-if="!data.visible">{{ list.data.databasePassword }}</span>
            <button @click="changeVisible">{{data.visible?'显示':'隐藏'}} </button>
          </td>
          <td class="key"></td>
          <td class="value"></td>
          <td class="key"></td>
          <td class="value"></td>
        </tr>
      </table>
    </div>
    <br>
    <div class="nodeInfo">
      <span style="font-weight: bold;">节点信息</span><br><br>
      <div v-for="(item, index) in list.data.clusterNodes" >
        <span style="font-weight: bold;">节点{{index+1}}</span><br><br>
        <table>
          <tr>
            <td class="key">节点类型</td>
            <td class="value">{{item.nodeType}}</td>
            <td class="key">IP地址</td>
            <td class="value">{{item.hostIp}}</td>
            <td class="key">安装用户</td>
            <td class="value">{{item.hostUsername}}</td>
          </tr>
          <tr>
            <td class="key">数据路径</td>
            <td class="value">{{item.dataPath}}</td>
            <td class="key">所属AZ</td>
            <td class="value">{{item.azOwner}}</td>
            <td class="key">AZ优先级</td>
            <td class="value">{{item.azPriority}}</td>
          </tr>
        </table>
        <div class="dashed-line"></div>
      </div>
    </div><br>
  </div>
</template>

<script setup>
import {reactive, onMounted, toRaw, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router';
import {KeyValue} from "@/types/global";
import axios from "axios";
import {batchDeleteTask, clusterLogDownload, copyTask, reExecuteTask} from "@/api/ops";
import {Message} from "@arco-design/web-vue";

const route = useRoute();
const router = useRouter();

//跳转集群管理界面
const clusterManage = () => {
  router.replace({ name: 'DailyOps', params: {status: list.data.status} });
};
//跳转新增集群页面
const editClusterTask = () => {
  console.log(JSON.stringify(toRaw(list.data)))
  router.push({ name:'step', params: { record: JSON.stringify(toRaw(list.data)) } });
}
//复制
const copy = () => {
  copyTask(list.data.clusterId)
    .then(res => {
      console.log("Number(res.code):"+Number(res.code))
      if (Number(res.code) === 200) {
        Message.success("复制" + list.data.clusterId + "成功!")
      }
    }).catch(error => {
    console.error("copyTask error:"+error);
  }).finally(() => {
    // clusterManage();
  })
}
//删除
const singleRowDelete = () => {
  batchDeleteTask([list.data.clusterId])
    .then(res => {
      if (Number(res.code) === 200) {
        Message.success("删除" + list.data.clusterId + "成功!")
      }
    })
    .catch(error => {
      console.log("DeleteTask error:" + error);
    }).finally(() => {
      clusterManage();
    })
}
//重新执行
const reExecute = () => {
  reExecuteTask(list.data.clusterId)
  .then(res => {
    if (Number(res.code) === 200) {
      Message.success(res.msg)
    }
  })
  .catch((error) => {
    console.log("reExecute error:" + error);
  })
}
//控制显示信息
const data = reactive({
  visible: true,
});

const changeVisible = () => {
  data.visible = !data.visible;
}
//存储集群信息
const list = reactive({
  data: {},
})

const getListData = () => {
  taskMenu
}

onMounted(() => {

  getListData()
})

//调用集群任务详情接口
const taskMenu = axios.get(`/clusterTask/detail/${route.query.clusterId}`).then((res) => {
  console.log("Number(res.code):"+Number(res.code))
  if (Number(res.code) === 200) {
    list.data = res.data;
    console.log(list.data);
  }
}).catch(error => {
  console.error("taskMenu infoError:"+error);
});
//执行日志下载
const logDownload = () => {
  console.log('logDownload');
  console.log(list.data.clusterId);
  clusterLogDownload(list.data.clusterId)
    .then((res)=>{
      if (res) {
        const blob = new Blob([res]);
        const link = document.createElement("a");
        const URL = window.URL || window.webkitURL;
        let herf = URL.createObjectURL(blob);
        link.href = herf;
        link.download = "OPERATE_LOG_" + list.data.clusterName + ".log";
        link.click();
        window.URL.revokeObjectURL(herf);
      }
    }).catch((err)=>{
    console.log("error:"+err);
  })
}
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
  color: white !important;
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
