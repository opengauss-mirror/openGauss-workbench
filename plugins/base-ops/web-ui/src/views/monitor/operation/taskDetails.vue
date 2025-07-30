<template>
  <div class="clusterManage" id="opTaskDetail">
    <div class="header">
      <div>
        <a-link type="primary" @click="clusterManage">{{$t('operation.DailyOps.sl3u5s5cf2y0')}}</a-link> /
        <select style="border: 0px;font-weight: bold;">
          <option>{{list.data.clusterName}}</option>
        </select>
      </div>
      <div class="operation">
        <a-link @click="logDownload">{{$t('operation.DailyOps.sl3u5s5cf224')}}</a-link>&nbsp;&nbsp;
        <a-link @click="copy">{{$t('operation.DailyOps.sl3u5s5cf222')}}</a-link>&nbsp;&nbsp;
        <a-link @click="editClusterTask">{{$t('operation.DailyOps.sl3u5s5cf225')}}</a-link>&nbsp;&nbsp;
        <a-link @click="singleRowDelete">{{$t('operation.DailyOps.sl3u5s5cf212')}}</a-link>&nbsp;&nbsp;
        <a-button class="execute" @click="reExecute">{{$t('operation.DailyOps.sl3u5s5cf221')}}</a-button>&nbsp;&nbsp;
      </div>
      <p class="clusterName">{{list.data.clusterName}}</p>
    </div>
    <div class="hostInfo">
      <span style="font-weight: bold;">{{$t('operation.DailyOps.sl3u5s5cf228')}}</span><br><br>
      <table>
        <tr>
          <td class="key">{{$t('operation.DailyOps.sl3u5s5cf229')}}</td>
          <td class="value">{{ list.data.displayHostIp }}</td>
          <td class="key">{{$t('operation.DailyOps.else16')}}</td>
          <td class="value">{{ list.data.hostUsername }}</td>
          <td class="key">{{$t('operation.DailyOps.sl3u5s5cf230')}}</td>
          <td class="value">{{ list.data.os }}</td>
        </tr>
        <tr>
          <td class="key">{{$t('operation.DailyOps.sl3u5s5cf231')}}</td>
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
      <span style="font-weight: bold;">{{$t('enterprise.InstallPrompt.5mpmb9e6puk0')}}</span><br><br>
      <table>
        <tr>
          <td class="key">{{$t('enterprise.ClusterConfig.5mpm3ku3hn40')}}</td>
          <td class="value">{{ list.data.clusterName }}</td>
          <td class="key">{{$t('operation.DailyOps.sl3u5s5cf232')}}</td>
          <td class="value" v-if="list.data.version === 'ENTERPRISE'">{{$t('operation.DailyOps.5mplp1xc4fg0')}}</td>
          <td class="value" v-if="list.data.version === 'LITE'">{{$t('operation.DailyOps.5mplp1xc4b40')}}</td>
          <td class="value" v-if="list.data.version === 'MINIMAL_LIST'">{{$t('operation.DailyOps.5mplp1xc46o0')}}</td>
          <td class="key">{{$t('packageManage.index.5myq5c8zp680')}}</td>
          <td class="value">{{ list.data.versionNum }}</td>
        </tr>
        <tr>
          <td class="key">{{$t('operation.DailyOps.sl3u5s5cf233')}}</td>
          <td class="value">
            <span v-if="list.data.deployType === 'CLUSTER' && list.data.clusterNodeNum > 1">
              {{$t('operation.DailyOps.5mplp1xbzmw0')}}{{list.data.clusterNodeNum - 1}}{{$t('operation.DailyOps.5mplp1xbztc0')}}
            </span>
            <span v-else>
              {{$t('operation.DailyOps.5mplp1xc0000')}}
            </span>
          </td>
          <td class="key">{{$t('operation.DailyOps.sl3u5s5cf234')}}</td>
          <td class="value">{{ list.data.databasePort }}</td>
          <td class="key">{{$t('operation.DailyOps.sl3u5s5cf235')}}</td>
          <td class="value">{{ list.data.envPath }}</td>
        </tr>
        <tr>
          <td class="key">{{$t('enterprise.ClusterConfig.5mpm3ku3hv40')}}</td>
          <td class="value">{{ list.data.installPath }}</td>
          <td class="key">{{$t('components.OfflineInstall.5mpn1nwazgw0')}}</td>
          <td class="value">{{ list.data.installPackagePath }}</td>
          <td class="key">{{$t('enterprise.ClusterConfig.5mpm3ku3i6s0')}}</td>
          <td class="value">{{ list.data.logPath }}</td>
        </tr>
        <tr>
          <td class="key">{{$t('enterprise.ClusterConfig.5mpm3ku3ie40')}}</td>
          <td class="value">{{ list.data.tmpPath }}</td>
          <td class="key">{{$t('enterprise.ClusterConfig.5mpm3ku3il80')}}</td>
          <td class="value">{{ list.data.omToolsPath }}</td>
          <td class="key">{{$t('enterprise.ClusterConfig.5mpm3ku3is00')}}</td>
          <td class="value">{{ list.data.corePath }}</td>
        </tr>
        <tr>
          <td class="key">{{$t('operation.DailyOps.sl3u5s5cf236')}}</td>
          <td class="value">{{ list.data.enableCmTool ? $t('enterprise.InstallPrompt.5mpmb9e6r4g0') : $t('enterprise.InstallPrompt.5mpmb9e6r800') }}</td>
          <td class="key">{{$t('operation.DailyOps.sl3u5s5cf237')}}</td>
          <td class="value">{{ list.data.enableGenerateEnvironmentVariableFile ? $t('enterprise.InstallPrompt.5mpmb9e6r4g0') : $t('enterprise.InstallPrompt.5mpmb9e6r800') }}</td>
          <td class="key" v-if="list.data.enableGenerateEnvironmentVariableFile">{{$t('operation.DailyOps.sl3u5s5cf238')}}</td>
          <td class="value" v-if="list.data.enableGenerateEnvironmentVariableFile">{{ list.data.envPath }}</td>
        </tr>
        <tr>
          <td class="key">{{$t('enterprise.ClusterConfig.5mpm3ku3jdg0')}}</td>
          <td class="value">
            <span v-if="data.visible">
              ******
              <img src="@/assets/images/modeling/ops/preview-close.png" @click="changeVisible" style="vertical-align: middle;width:20px;"/>
            </span>
            <span v-if="!data.visible" style="height:20px;">
              {{ list.data.databasePassword }}
              <img src="@/assets/images/modeling/ops/preview-open.png" @click="changeVisible" style="vertical-align: middle;width:20px"/>
            </span>
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
      <span style="font-weight: bold;">{{$t('components.AuditLog.5mplidlifr40')}}</span><br><br>
      <div v-for="(item, index) in list.data.clusterNodes" >
        <span style="font-weight: bold;">{{$t('wdr.GenerateWdrDlg.5mpm0eufzyg0')}}{{index+1}}</span><br><br>
        <table>
          <tr>
            <td class="key">{{$t('operation.DailyOps.sl3u5s5cf239')}}</td>
            <td class="value">{{item.nodeType}}</td>
            <td class="key">{{$t('operation.HostPwdDlg.IpAddress')}}</td>
            <td class="value">{{item.displayHostIp}}</td>
            <td class="key">{{$t('enterprise.NodeConfig.5mpme7w6bak0')}}</td>
            <td class="value">{{item.hostUsername}}</td>
          </tr>
          <tr>
            <td class="key">{{$t('enterprise.NodeConfig.5mpme7w6brs0')}}</td>
            <td class="value">{{item.dataPath}}</td>
            <td class="key">{{$t('enterprise.NodeConfig.5mpme7w6aj40')}}</td>
            <td class="value">{{item.azOwner}}</td>
            <td class="key">{{$t('operation.DailyOps.sl3u5s5cf240')}}</td>
            <td class="value">{{item.azPriority}}</td>
          </tr>
          <tr v-if="list.data.enableCmTool">
            <td class="key">{{$t('enterprise.NodeConfig.5mpme7w6be40')}}</td>
            <td class="value">{{item.isCmMaster ? $t('enterprise.InstallPrompt.5mpmb9e6r4g0') : $t('enterprise.InstallPrompt.5mpmb9e6r800')}}</td>
            <td class="key">{{$t('enterprise.NodeConfig.else4')}}</td>
            <td class="value">{{item.cmDataPath}}</td>
            <td class="key">{{$t('enterprise.NodeConfig.else5')}}</td>
            <td class="value">{{item.cmPort}}</td>
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
import {batchClusterNodes, batchDeleteTask, clusterLogDownload, copyTask, reExecuteTask} from "@/api/ops";
import {Message} from "@arco-design/web-vue";
import { useI18n } from 'vue-i18n'
import { encryptPassword } from "@/utils/jsencrypt";

const { t } = useI18n()

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
      if (Number(res.code) === 200) {
        Message.success("copy" + list.data.clusterId + "success!")
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
        Message.success("delete" + list.data.clusterId + "success!")
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
  batchClusterNodes(route.query.clusterId).then(async (res) => {
    if (Number(res.code) === 200) {
      list.data = res.data;
      list.data.databasePassword = t('enterprise.ClusterConfig.passwdWarning')
    }
  }).catch(error => {
    console.error("taskMenu infoError:"+error);
  });
}

onMounted(() => {
  getListData()
})

watch(() => route.query.clusterId, (newClusterId) => {
  if (route.query.clusterId) {
    getListData()
  }
})

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
  color: white !important;
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
