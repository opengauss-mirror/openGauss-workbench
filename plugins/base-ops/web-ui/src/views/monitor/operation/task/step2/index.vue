<template>
  <a-spin class="body" :loading="loading" :tip="$t('operation.task.step2s5cf2y0')" id="taskStep2">
    <a-collapse
      :default-active-key="activeCluster"
      class="node1"
    >
      <a-collapse-item
        v-for="(item, index) of list.envCheckDetails"
        :header="listNodes[index]"
        :key="index"
      >
        <div class="hardware">
          <div class="rowHeader">{{ $t('operation.task.step2s5cf2y1') }}</div>
          <div class="rowCheck">
            <span class="hardEnv">
              <div class="hardEnvFirst">
                <span style="padding-left:20px;">
                  <svg-icon icon-class="ops-pass" class="icon-s mr-s" v-if="item.envCheckDetails.hardwareEnv.envProperties[0].status === 'NORMAL'"></svg-icon>
                  <svg-icon icon-class="ops-warning" class="icon-s mr-s" v-else-if="item.envCheckDetails.hardwareEnv.envProperties[0].status === 'WARMING'"></svg-icon>
                  <svg-icon icon-class="ops-error" class="icon-s mr-s" v-else></svg-icon>
                  {{ $t('operation.task.step2s5cf2y2') }}
                </span>
                <span style="padding-right:24px">{{item.envCheckDetails.hardwareEnv.envProperties[0].value}}</span>
              </div>
              <span :style="{ color: list.color[index][0] }" class="hardEnvSecond" v-if="item.envCheckDetails.hardwareEnv.envProperties[0].status !== 'NORMAL'" :title="item.envCheckDetails.softwareEnv.envProperties[0].statusMessage">{{ $t('operation.task.step2s5cf2y3') }}{{item.envCheckDetails.hardwareEnv.envProperties[0].value}}</span>
              <span :style="{ color: list.color[index][0] }" class="hardEnvSecond" v-else :title="$t('operation.task.step2s5cf2y4')">{{ $t('operation.task.step2s5cf2y4') }}</span>
            </span>
            <span class="hardEnv">
              <div class="hardEnvFirst">
                <span style="padding-left:20px;">
                  <svg-icon icon-class="ops-pass" class="icon-s mr-s" v-if="item.envCheckDetails.hardwareEnv.envProperties[1].status === 'NORMAL'"></svg-icon>
                  <svg-icon icon-class="ops-warning" class="icon-s mr-s" v-else-if="item.envCheckDetails.hardwareEnv.envProperties[1].status === 'WARMING'"></svg-icon>
                  <svg-icon icon-class="ops-error" class="icon-s mr-s" v-else></svg-icon>
                  {{ $t('operation.task.step2s5cf2y5') }}
                </span>
                <span style="padding-right:24px">{{item.envCheckDetails.hardwareEnv.envProperties[1].value}}</span>
              </div>
              <span :style="{ color: list.color[index][1] }" class="hardEnvSecond" v-if="item.envCheckDetails.hardwareEnv.envProperties[1].status !== 'NORMAL'" :title="$t('operation.task.step2s5cf2y6')">{{ $t('operation.task.step2s5cf2y6') }}</span>
              <span :style="{ color: list.color[index][1] }" class="hardEnvSecond" v-else :title="$t('operation.task.step2s5cf2y7')">{{ $t('operation.task.step2s5cf2y7') }}</span>
            </span>
            <span class="hardEnv">
              <div class="hardEnvFirst">
                <span style="padding-left:20px;">
                  <svg-icon icon-class="ops-pass" class="icon-s mr-s" v-if="item.envCheckDetails.hardwareEnv.envProperties[2].status === 'NORMAL'"></svg-icon>
                  <svg-icon icon-class="ops-warning" class="icon-s mr-s" v-else-if="item.envCheckDetails.hardwareEnv.envProperties[2].status === 'WARMING'"></svg-icon>
                  <svg-icon icon-class="ops-error" class="icon-s mr-s" v-else></svg-icon>
                  {{ $t('operation.task.step2s5cf2y8') }}
                </span>
                <span style="padding-right:24px">{{item.envCheckDetails.hardwareEnv.envProperties[2].value}}</span>
              </div>
              <span :style="{ color: list.color[index][2] }" class="hardEnvSecond" v-if="item.envCheckDetails.hardwareEnv.envProperties[2].status !== 'NORMAL'" :title="$t('operation.task.step2s5cf2y9')">{{ $t('operation.task.step2s5cf2y9') }}</span>
              <span :style="{ color: list.color[index][2] }" class="hardEnvSecond" v-else :title="$t('operation.task.step2s5cf210')">{{ $t('operation.task.step2s5cf210') }}</span>
            </span>
            <span class="hardEnv">
              <div class="hardEnvFirst">
                <span style="padding-left:20px;">
                  <svg-icon icon-class="ops-pass" class="icon-s mr-s" v-if="item.envCheckDetails.hardwareEnv.envProperties[3].status === 'NORMAL'"></svg-icon>
                  <svg-icon icon-class="ops-warning" class="icon-s mr-s" v-else-if="item.envCheckDetails.hardwareEnv.envProperties[3].status === 'WARMING'"></svg-icon>
                  <svg-icon icon-class="ops-error" class="icon-s mr-s" v-else></svg-icon>
                  {{ $t('operation.task.step2s5cf211') }}
                </span>
                <span style="padding-right:24px">{{item.envCheckDetails.hardwareEnv.envProperties[3].value}}</span>
              </div>
              <span :style="{ color: list.color[index][3] }" class="hardEnvSecond" v-if="item.envCheckDetails.hardwareEnv.envProperties[3].status !== 'NORMAL'" :title="$t('operation.task.step2s5cf212')">{{ $t('operation.task.step2s5cf212') }}</span>
              <span :style="{ color: list.color[index][3] }" class="hardEnvSecond" v-else :title="$t('operation.task.step2s5cf213')">{{ $t('operation.task.step2s5cf213') }}</span>
            </span>
            <span class="hardEnv">
              <div class="hardEnvFirst">
                <span style="padding-left:20px;">
                  <svg-icon icon-class="ops-pass" class="icon-s mr-s" v-if="item.envCheckDetails.hardwareEnv.envProperties[4].status === 'NORMAL'"></svg-icon>
                  <svg-icon icon-class="ops-warning" class="icon-s mr-s" v-else-if="item.envCheckDetails.hardwareEnv.envProperties[4].status === 'WARMING'"></svg-icon>
                  <svg-icon icon-class="ops-error" class="icon-s mr-s" v-else></svg-icon>
                  {{ $t('operation.task.step2s5cf214') }}
                </span>
                <span style="padding-right:24px">{{item.envCheckDetails.hardwareEnv.envProperties[4].value}}</span>
              </div>
              <span :style="{ color: list.color[index][4] }" class="hardEnvSecond" v-if="item.envCheckDetails.hardwareEnv.envProperties[4].status !== 'NORMAL'" :title="$t('operation.task.step2s5cf215')">{{ $t('operation.task.step2s5cf215') }}</span>
              <span :style="{ color: list.color[index][4] }" class="hardEnvSecond" v-else :title="$t('operation.task.step2s5cf216')">{{ $t('operation.task.step2s5cf216') }}</span>
            </span>
            <span class="hardEnv">
              <div class="hardEnvFirst">
                <span style="padding-left:20px;">
                  <svg-icon icon-class="ops-pass" class="icon-s mr-s" v-if="item.envCheckDetails.hardwareEnv.envProperties[5].status === 'NORMAL'"></svg-icon>
                  <svg-icon icon-class="ops-warning" class="icon-s mr-s" v-else-if="item.envCheckDetails.hardwareEnv.envProperties[5].status === 'WARMING'"></svg-icon>
                  <svg-icon icon-class="ops-error" class="icon-s mr-s" v-else></svg-icon>
                  {{ $t('operation.task.step2s5cf217') }}
                </span>
                <span style="padding-right:24px">{{item.envCheckDetails.hardwareEnv.envProperties[5].value}}</span>
              </div>
              <span :style="{ color: list.color[index][5] }" class="hardEnvSecond" v-if="item.envCheckDetails.hardwareEnv.envProperties[5].status !== 'NORMAL'" :title="$t('operation.task.step2s5cf218')">{{ $t('operation.task.step2s5cf218') }}</span>
              <span :style="{ color: list.color[index][5] }" class="hardEnvSecond" v-else :title="$t('operation.task.step2s5cf219')">{{ $t('operation.task.step2s5cf219') }}</span>
            </span>
          </div>
          <br>
        </div>
        <br>
        <div class="software">
          <div class="rowHeader">{{ $t('operation.task.step2s5cf220') }}</div>
          <div class="rowCheck">
            <span class="softEnv">
              <div class="softEnvFirst">
                <span style="padding-left:24px;">
                  <svg-icon icon-class="ops-pass" class="icon-s mr-s" v-if="item.envCheckDetails.softwareEnv.envProperties[0].status === 'NORMAL'"></svg-icon>
                  <svg-icon icon-class="ops-warning" class="icon-s mr-s" v-else-if="item.envCheckDetails.softwareEnv.envProperties[0].status === 'WARMING'"></svg-icon>
                  <svg-icon icon-class="ops-error" class="icon-s mr-s" v-else></svg-icon>
                  {{ $t('operation.task.step2s5cf221') }}
                </span>
              </div>
              <span :style="{ color: list.color[index][6] }" class="softEnvSecond" v-if="item.envCheckDetails.softwareEnv.envProperties[0].status !== 'NORMAL'" :title="item.envCheckDetails.softwareEnv.envProperties[0].statusMessage">{{ $t('operation.task.step2s5cf222') }}{{item.envCheckDetails.softwareEnv.envProperties[0].statusMessage}}</span>
              <span :style="{ color: list.color[index][6] }" class="softEnvSecond" v-else :title="$t('operation.task.step2s5cf223')">{{ $t('operation.task.step2s5cf223') }}</span>
            </span>
            <span class="softEnv">
              <div class="softEnvFirst">
                <span style="padding-left:24px;">
                  <svg-icon icon-class="ops-pass" class="icon-s mr-s" v-if="item.envCheckDetails.softwareEnv.envProperties[1].status === 'NORMAL'"></svg-icon>
                  <svg-icon icon-class="ops-warning" class="icon-s mr-s" v-else-if="item.envCheckDetails.softwareEnv.envProperties[1].status === 'WARMING'"></svg-icon>
                  <svg-icon icon-class="ops-error" class="icon-s mr-s" v-else></svg-icon>
                  {{ $t('operation.task.step2s5cf224') }}
                </span>
              </div>
              <span :style="{ color: list.color[index][7] }" class="softEnvSecond" v-if="item.envCheckDetails.softwareEnv.envProperties[1].status !== 'NORMAL'" :title="$t('operation.task.step2s5cf225')">{{ $t('operation.task.step2s5cf225') }}</span>
              <span :style="{ color: list.color[index][7] }" class="softEnvSecond" v-else :title="$t('operation.task.step2s5cf226')">{{ $t('operation.task.step2s5cf226') }}</span>
            </span>
            <span class="softEnv">
              <div class="softEnvFirst">
                <span style="padding-left:24px;">
                  <svg-icon icon-class="ops-pass" class="icon-s mr-s" v-if="item.envCheckDetails.softwareEnv.envProperties[2].status === 'NORMAL'"></svg-icon>
                  <svg-icon icon-class="ops-warning" class="icon-s mr-s" v-else-if="item.envCheckDetails.softwareEnv.envProperties[2].status === 'WARMING'"></svg-icon>
                  <svg-icon icon-class="ops-error" class="icon-s mr-s" v-else></svg-icon>
                  {{ $t('operation.task.step2s5cf227') }}
                </span>
              </div>
              <span :style="{ color: list.color[index][8] }" class="softEnvSecond" v-if="item.envCheckDetails.softwareEnv.envProperties[2].status !== 'NORMAL'" :title="$t('operation.task.step2s5cf228')">{{ $t('operation.task.step2s5cf228') }}</span>
              <span :style="{ color: list.color[index][8] }" class="softEnvSecond" v-else :title="$t('operation.task.step2s5cf229')">{{ $t('operation.task.step2s5cf229') }}</span>
            </span>
            <span class="softEnv">
              <div class="softEnvFirst">
                <span style="padding-left:24px;">
                  <svg-icon icon-class="ops-pass" class="icon-s mr-s" v-if="item.envCheckDetails.softwareEnv.envProperties[3].status === 'NORMAL'"></svg-icon>
                  <svg-icon icon-class="ops-warning" class="icon-s mr-s" v-else-if="item.envCheckDetails.softwareEnv.envProperties[3].status === 'WARMING'"></svg-icon>
                  <svg-icon icon-class="ops-error" class="icon-s mr-s" v-else></svg-icon>
                  {{ $t('operation.task.step2s5cf230') }}
                </span>
              </div>
              <span :style="{ color: list.color[index][9] }" class="softEnvSecond" v-if="item.envCheckDetails.softwareEnv.envProperties[3].status !== 'NORMAL'" :title="item.envCheckDetails.softwareEnv.envProperties[3].statusMessage">{{ $t('operation.task.step2s5cf231') }}{{item.envCheckDetails.softwareEnv.envProperties[3].statusMessage}}</span>
              <span :style="{ color: list.color[index][9] }" class="softEnvSecond" v-else :title="$t('operation.task.step2s5cf232')">{{ $t('operation.task.step2s5cf232') }}</span>
            </span>
          </div>
          <br>
        </div>
        <template #extra>
          <a-link @click.stop="checkAgain" style="color: #3291fe;">{{ $t('operation.task.step2s5cf233') }}</a-link>
        </template>
      </a-collapse-item>
    </a-collapse>
  </a-spin>
</template>

<script setup>
import {onMounted, reactive, ref, watch} from 'vue';
import {batchClusterNodes, clusterEnvCheck, getHostIp} from "@/api/ops";
import {Message} from "@arco-design/web-vue";
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { defineProps } from 'vue';

const props = defineProps({
  clusterId : Object
})

const loading = ref(true);

const list = reactive({
  message: [],
  envCheckDetails: [],
  clusterId: [],
  color: [],
  result : ''
})

const getHostId = () => {
  return batchClusterNodes(props.clusterId).then(res => {
    if (Number(res.code) === 200) {
      list.message = res.data.clusterNodes;
      return res.data.clusterNodes;
    }
    throw new Error('Cluster nodes fetch failed');
  }).catch(error => {
    console.error("getHostId Error:" + error);
    throw error; // 重新抛出错误，以便在外部.catch()中捕获
  });
}

onMounted(() => {
  getHostId().then(() => {
    fetchHostIp().then(() => { // 等待fetchHostIp完成
      checkEnv();
    }).catch(error => {
      console.error('Error in fetchHostIp:', error);
    });
  }).catch(error => {
    console.error('Error in onMounted:', error);
  });
});

const checkEnv = () => {
  clusterEnvCheck(props.clusterId)
    .then((res) => {
      if (Number(res.code) === 200) {
        list.result = res.data.result
        list.envCheckDetails = [];
        list.message.forEach(info => {
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
  return getHostIp(param).then((res) => {
    if (res.code === 200) {
      res.data.forEach(item => {hostIdIp.append(item.hostId, item.publicIp)})
      res.data.forEach(item => {hostPuPr.append(item.publicIp,item.privateIp)})
      list.clusterId = []
      list.clusterId.push(props.clusterId)
      list.message.forEach((item, index) => {
        let tempPublicIp = hostIdIp.get(item.hostId)
        let tempPrivateIp = hostPuPr.get(tempPublicIp)
        let tempIp = tempPublicIp + '(' + tempPrivateIp + ')'
        listNodes.push(tempIp)
        activeCluster.value.push(index)
      })
    } else {
      Message.error({
        content: t('operation.task.step2s5cf234')
      })
      throw new Error('Failed to fetch host IP');
    }
  }) .catch((error) => {
    console.error(error)
    throw error;
  })
}

</script>

<style scoped>
.body {
  display: block;

}
.node1 {
  width: 97%;
  height: auto;
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
}
.software {
  width: 99%;
  height: auto;
}
.rowHeader {
  width: 98%;
  margin-left: 1%;
  line-height: 54px;
  font-weight: bold;
}
.hardEnv {
  display: inline-block;
  width: 15%;
  margin-left: 1%;
}
.softEnv {
  display: inline-block;
  width: 23%;
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
  display: inline-block;
  white-space: nowrap;
  padding-left: 40px;
  height: 20px;
  width: 88%;
  overflow: hidden;
  text-overflow: ellipsis;
}

</style>
