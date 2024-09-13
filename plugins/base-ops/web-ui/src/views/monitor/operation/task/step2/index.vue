<template>
  <a-spin class="body" :loading="loading" tip="环境监测中...">
    <a-collapse
      style="background: #f4f6fa"
      :default-active-key="list.nodeIndex"
      class="node1"
    >
      <a-collapse-item
        v-for="(item, index) of list.envCheckDetails"
        :header="listNodes[index]"
        :key="index"
      >
        <div class="hardware">
          <div class="rowHeader">硬件环境监测测结果</div>
          <div class="rowCheck">
            <span class="hardEnv">
              <div class="hardEnvFirst">
                <span style="padding-left:20px;">
                  <svg-icon icon-class="ops-pass" class="icon-s mr-s" v-if="item.envCheckDetails.hardwareEnv.envProperties[0].status === 'NORMAL'"></svg-icon>
                  <svg-icon icon-class="ops-warning" class="icon-s mr-s" v-else-if="item.envCheckDetails.hardwareEnv.envProperties[0].status === 'WARMING'"></svg-icon>
                  <svg-icon icon-class="ops-error" class="icon-s mr-s" v-else></svg-icon>
                  操作系统
                </span>
                <span style="padding-right:24px">{{item.envCheckDetails.hardwareEnv.envProperties[0].value}}</span>
              </div>
              <span :style="{ color: list.color[index][0] }" class="hardEnvSecond" v-if="item.envCheckDetails.hardwareEnv.envProperties[0].status !== 'NORMAL'">请检查操作系统是否为{{item.envCheckDetails.hardwareEnv.envProperties[0].value}}</span>
              <span :style="{ color: list.color[index][0] }" class="hardEnvSecond" v-else>{{ $t('操作系统符合要求') }}</span>
            </span>
            <span class="hardEnv">
              <div class="hardEnvFirst">
                <span style="padding-left:20px;">
                  <svg-icon icon-class="ops-pass" class="icon-s mr-s" v-if="item.envCheckDetails.hardwareEnv.envProperties[1].status === 'NORMAL'"></svg-icon>
                  <svg-icon icon-class="ops-warning" class="icon-s mr-s" v-else-if="item.envCheckDetails.hardwareEnv.envProperties[1].status === 'WARMING'"></svg-icon>
                  <svg-icon icon-class="ops-error" class="icon-s mr-s" v-else></svg-icon>
                  操作系统版本
                </span>
                <span style="padding-right:24px">{{item.envCheckDetails.hardwareEnv.envProperties[1].value}}</span>
              </div>
              <span :style="{ color: list.color[index][1] }" class="hardEnvSecond" v-if="item.envCheckDetails.hardwareEnv.envProperties[1].status !== 'NORMAL'">请检查操作系统版本是否符合要求</span>
              <span :style="{ color: list.color[index][1] }" class="hardEnvSecond" v-else>操作系统版本符合要求</span>
            </span>
            <span class="hardEnv">
              <div class="hardEnvFirst">
                <span style="padding-left:20px;">
                  <svg-icon icon-class="ops-pass" class="icon-s mr-s" v-if="item.envCheckDetails.hardwareEnv.envProperties[2].status === 'NORMAL'"></svg-icon>
                  <svg-icon icon-class="ops-warning" class="icon-s mr-s" v-else-if="item.envCheckDetails.hardwareEnv.envProperties[2].status === 'WARMING'"></svg-icon>
                  <svg-icon icon-class="ops-error" class="icon-s mr-s" v-else></svg-icon>
                  可用内存
                </span>
                <span style="padding-right:24px">{{item.envCheckDetails.hardwareEnv.envProperties[2].value}}</span>
              </div>
              <span :style="{ color: list.color[index][2] }" class="hardEnvSecond" v-if="item.envCheckDetails.hardwareEnv.envProperties[2].status !== 'NORMAL'">建议内存32GB或者更大</span>
              <span :style="{ color: list.color[index][2] }" class="hardEnvSecond" v-else>可用内存符合要求</span>
            </span>
            <span class="hardEnv">
              <div class="hardEnvFirst">
                <span style="padding-left:20px;">
                  <svg-icon icon-class="ops-pass" class="icon-s mr-s" v-if="item.envCheckDetails.hardwareEnv.envProperties[3].status === 'NORMAL'"></svg-icon>
                  <svg-icon icon-class="ops-warning" class="icon-s mr-s" v-else-if="item.envCheckDetails.hardwareEnv.envProperties[3].status === 'WARMING'"></svg-icon>
                  <svg-icon icon-class="ops-error" class="icon-s mr-s" v-else></svg-icon>
                  CPU核数
                </span>
                <span style="padding-right:24px">{{item.envCheckDetails.hardwareEnv.envProperties[3].value}}</span>
              </div>
              <span :style="{ color: list.color[index][3] }" class="hardEnvSecond" v-if="item.envCheckDetails.hardwareEnv.envProperties[3].status !== 'NORMAL'">CPU核数不符合系统要求</span>
              <span :style="{ color: list.color[index][3] }" class="hardEnvSecond" v-else>CPU核数符合系统要求</span>
            </span>
            <span class="hardEnv">
              <div class="hardEnvFirst">
                <span style="padding-left:20px;">
                  <svg-icon icon-class="ops-pass" class="icon-s mr-s" v-if="item.envCheckDetails.hardwareEnv.envProperties[4].status === 'NORMAL'"></svg-icon>
                  <svg-icon icon-class="ops-warning" class="icon-s mr-s" v-else-if="item.envCheckDetails.hardwareEnv.envProperties[4].status === 'WARMING'"></svg-icon>
                  <svg-icon icon-class="ops-error" class="icon-s mr-s" v-else></svg-icon>
                  CPU频率
                </span>
                <span style="padding-right:24px">{{item.envCheckDetails.hardwareEnv.envProperties[4].value}}</span>
              </div>
              <span :style="{ color: list.color[index][4] }" class="hardEnvSecond" v-if="item.envCheckDetails.hardwareEnv.envProperties[4].status !== 'NORMAL'">CPU频率最小为2.0GHz</span>
              <span :style="{ color: list.color[index][4] }" class="hardEnvSecond" v-else>CPU频率符合要求</span>
            </span>
            <span class="hardEnv">
              <div class="hardEnvFirst">
                <span style="padding-left:20px;">
                  <svg-icon icon-class="ops-pass" class="icon-s mr-s" v-if="item.envCheckDetails.hardwareEnv.envProperties[5].status === 'NORMAL'"></svg-icon>
                  <svg-icon icon-class="ops-warning" class="icon-s mr-s" v-else-if="item.envCheckDetails.hardwareEnv.envProperties[5].status === 'WARMING'"></svg-icon>
                  <svg-icon icon-class="ops-error" class="icon-s mr-s" v-else></svg-icon>
                  可用硬盘空间
                </span>
                <span style="padding-right:24px">{{item.envCheckDetails.hardwareEnv.envProperties[5].value}}</span>
              </div>
              <span :style="{ color: list.color[index][5] }" class="hardEnvSecond" v-if="item.envCheckDetails.hardwareEnv.envProperties[5].status !== 'NORMAL'">可用硬盘空间不符合系统要求</span>
              <span :style="{ color: list.color[index][5] }" class="hardEnvSecond" v-else>可用硬盘空间符合系统要求</span>
            </span>
          </div>
          <br>
        </div>
        <br>
        <div class="software">
          <div class="rowHeader">软件环境检测结果</div>
          <div class="rowCheck">
            <span class="softEnv">
              <div class="softEnvFirst">
                <span style="padding-left:24px;">
                  <svg-icon icon-class="ops-pass" class="icon-s mr-s" v-if="item.envCheckDetails.softwareEnv.envProperties[0].status === 'NORMAL'"></svg-icon>
                  <svg-icon icon-class="ops-warning" class="icon-s mr-s" v-else-if="item.envCheckDetails.softwareEnv.envProperties[0].status === 'WARMING'"></svg-icon>
                  <svg-icon icon-class="ops-error" class="icon-s mr-s" v-else></svg-icon>
                  软件依赖性
                </span>
              </div>
              <span :style="{ color: list.color[index][6] }" class="softEnvSecond" v-if="item.envCheckDetails.softwareEnv.envProperties[0].status !== 'NORMAL'">未安装依赖：{{item.envCheckDetails.softwareEnv.envProperties[0].statusMessage}}</span>
              <span :style="{ color: list.color[index][6] }" class="softEnvSecond" v-else>符合要求</span>
            </span>
            <span class="softEnv">
              <div class="softEnvFirst">
                <span style="padding-left:24px;">
                  <svg-icon icon-class="ops-pass" class="icon-s mr-s" v-if="item.envCheckDetails.softwareEnv.envProperties[1].status === 'NORMAL'"></svg-icon>
                  <svg-icon icon-class="ops-warning" class="icon-s mr-s" v-else-if="item.envCheckDetails.softwareEnv.envProperties[1].status === 'WARMING'"></svg-icon>
                  <svg-icon icon-class="ops-error" class="icon-s mr-s" v-else></svg-icon>
                  防火墙
                </span>
              </div>
              <span :style="{ color: list.color[index][7] }" class="softEnvSecond" v-if="item.envCheckDetails.softwareEnv.envProperties[1].status !== 'NORMAL'">未开启防火墙</span>
              <span :style="{ color: list.color[index][7] }" class="softEnvSecond" v-else>已开启防火墙</span>
            </span>
            <span class="softEnv">
              <div class="softEnvFirst">
                <span style="padding-left:24px;">
                  <svg-icon icon-class="ops-pass" class="icon-s mr-s" v-if="item.envCheckDetails.softwareEnv.envProperties[2].status === 'NORMAL'"></svg-icon>
                  <svg-icon icon-class="ops-warning" class="icon-s mr-s" v-else-if="item.envCheckDetails.softwareEnv.envProperties[2].status === 'WARMING'"></svg-icon>
                  <svg-icon icon-class="ops-error" class="icon-s mr-s" v-else></svg-icon>
                  安装用户
                </span>
              </div>
              <span :style="{ color: list.color[index][8] }" class="softEnvSecond" v-if="item.envCheckDetails.softwareEnv.envProperties[2].status !== 'NORMAL'">不允许用户安装</span>
              <span :style="{ color: list.color[index][8] }" class="softEnvSecond" v-else>允许用户安装</span>
            </span>
            <span class="softEnv">
              <div class="softEnvFirst">
                <span style="padding-left:24px;">
                  <svg-icon icon-class="ops-pass" class="icon-s mr-s" v-if="item.envCheckDetails.softwareEnv.envProperties[3].status === 'NORMAL'"></svg-icon>
                  <svg-icon icon-class="ops-warning" class="icon-s mr-s" v-else-if="item.envCheckDetails.softwareEnv.envProperties[3].status === 'WARMING'"></svg-icon>
                  <svg-icon icon-class="ops-error" class="icon-s mr-s" v-else></svg-icon>
                  其他
                </span>
              </div>
              <span :style="{ color: list.color[index][9] }" class="softEnvSecond" v-if="item.envCheckDetails.softwareEnv.envProperties[3].status !== 'NORMAL'">不符合要求：{{item.envCheckDetails.softwareEnv.envProperties[3].statusMessage}}</span>
              <span :style="{ color: list.color[index][9] }" class="softEnvSecond" v-else>其他条件均符合要求</span>
            </span>
          </div>
          <br>
        </div>
        <template #extra>
          <a-link @click.stop="checkAgain" style="color: #3291fe;">重新监测</a-link>
        </template>
      </a-collapse-item>
    </a-collapse>
  </a-spin>
</template>

<script setup>
import {onMounted, reactive, ref, watch} from 'vue';
import {clusterEnvCheck, getHostIp} from "@/api/ops";
import {Message} from "@arco-design/web-vue";
import { useRoute } from 'vue-router';

import { defineProps } from 'vue';

const props = defineProps({
  message: Array,
  clusterId : Object
})


const loading = ref(true);

const list = reactive({
  envCheckDetails: [],
  nodes: [
  ],
  nodeName: [],
  nodeIndex: [],
  clusterId: [],
  color: [],
  result : ''
})

const route = useRoute();

onMounted(() => {
  checkEnv();
})

const checkEnv = async () => {
  await init()
   clusterEnvCheck(props.clusterId)
    .then((res) => {
      if (Number(res.code) === 200) {
        list.result = res.data.result
        props.message.forEach(info => {
          res.data.envCheckDetails.forEach(item => {
            if (item.clusterNodeId === info.clusterNodeId) {
              list.envCheckDetails.push(item);
            }
          })
        })
        list.envCheckDetails.forEach((item, index) => {
          if (list.color.length < list.envCheckDetails.length) {
            list.color.push([]);
          }
          list.color[index] = [];
          item.envCheckDetails.hardwareEnv.envProperties.forEach(result => {
            if (result.status === 'NORMAL') {
              list.color[index].push('#8d98aa');
            } else if (result.status === 'WARMING') {
              list.color[index].push('#faad14');
            } else {
              list.color[index].push('#e32020');
            }
          })
          item.envCheckDetails.softwareEnv.envProperties.forEach(result => {
            if (result.status === 'NORMAL') {
              list.color[index].push('#8d98aa');
            } else if (result.status === 'WARMING') {
              list.color[index].push('#faad14');
            } else {
              list.color[index].push('#e32020');
            }
          })
        })
      }
    }).catch(error => {
    Message.error("checkEnv infoError:"+error);
  }).finally(() => {
    loading.value = false;
  })
}
//重新监测
const checkAgain = () => {
  loading.value = true;
  checkEnv()
}

const emits = defineEmits(['subTaskEnv'])
watch(list.result, (val) => {
  emits('subTaskEnv', val)
}, { deep: true })

const activeCluster = ref([])
const hostIdIp = new FormData
const hostPuPr = new FormData
const listNodes = reactive([])
const fetchHostIp = () => {
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
    list.clusterId = []
    list.clusterId.push(props.clusterId)
    list.nodes = []
    props.message.forEach((item, index) => {
      let tempPublicIp = hostIdIp.get(item.hostId)
      let tempPrivateIp = hostPuPr.get(tempPublicIp)
      let tempIp = tempPublicIp + '(' + tempPrivateIp + ')'
      listNodes.push(tempIp)
      activeCluster.value.push(index)
    })
  })
}
 const init = () => {
   fetchHostIp()
 }

</script>

<style scoped>
.body {
  display: block;
  background-color: #f4f6fa;
}
.node1 {
  width: 97%;
  height: auto;
  background-color: #ffffff !important;
  margin-left: 1%;
  padding-left: 1%;
}
.nodeRow {
  width: 97%;
  line-height: 64px;
}
.hardware {
  width: 99%;
  height: auto;
  background-color: white;
}
.software {
  width: 99%;
  height: auto;
  background-color: #ffffff;
}
.rowHeader {
  background-color: #ffffff;
  width: 98%;
  margin-left: 1%;
  line-height: 54px;
  font-weight: bold;
}
.hardEnv {
  display: inline-block;
  width: 15%;
  height: 82px;
  background-color: rgb(247,248,250);
  margin-left: 1%;
}
.softEnv {
  display: inline-block;
  width: 23%;
  height: 82px;
  background-color: rgb(247,248,250);
  margin-left: 1%;
}
.icon-s {
  margin:0px;
  margin-right:4px;
}
.hardEnvFirst, .softEnvFirst {
  display: flex;
  justify-content: space-between;
  align-items: center;
  line-height:47px;
}
.hardEnvSecond, .softEnvSecond{
  padding-left:40px;
}

</style>
