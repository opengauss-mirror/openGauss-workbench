<template>
  <div class="cluster-details-title">
    <span v-if="detailsData.clusterStatus === 'Running'"
      >集群状态：
      <span class="green">正常</span>
    </span>
    <span v-else-if="detailsData.clusterStatus === 'Creating'"
      >集群状态：
      <span class="gold">创建中</span>
    </span>
    <span v-else
      >集群状态：
      <span class="red">异常</span>
    </span>
    <a-space>
      <a-button type="primary" @click="refresh" :loading="loading"
        >刷新</a-button
      >
      <a-button type="primary" @click="rollingRestart">滚动重启</a-button>
    </a-space>
  </div>
  <a-table :columns="columns" :data="podList" :bordered="false">
    <template #status="{ record }">
      {{ record.status }}
    </template>
    <template #requestResource="{ record }">
      {{
        record.requestResource &&
        `${record.requestResource.cpu} | ${record.requestResource.memory}`
      }}
      <br />
      爆发规格：{{ record.cpuBurst }}
    </template>
    <template #podIps="{ record }">
      <div v-for="ip in record.podIps">{{ ip }}</div>
    </template>
    <template #createTime="{ record }">
      {{
        record.createTime &&
        dayjs(record.createTime).format("YYYY-MM-DD HH:mm:ss")
      }}
    </template>
    <template #lastRestartTime="{ record }">
      {{
        record.lastRestartTime &&
        dayjs(record.lastRestartTime).format("YYYY-MM-DD HH:mm:ss")
      }}
    </template>
    <template #opt="{ record }">
      <a-space>
        <a-button
          @click="setMaster(record)"
          v-if="record.masterSlaveMode"
          :disabled="record.masterSlaveMode !== 'Standby'"
          type="primary"
          size="small"
          >设为主</a-button
        >
        <a-button type="primary" size="small" @click="deletePod(record)"
          >关闭</a-button
        >
      </a-space>
    </template>
  </a-table>
</template>
<script setup>
import { computed, onBeforeMount, onUnmounted, ref, watch } from "vue";
import {
  clusterPodList,
  clusterPodRestart,
  clusterPodDelete,
  setTheMasterNode,
} from "@/api/clusterlist.js";
import { useRoute } from "vue-router";
import dayjs from "dayjs";
import { Message, Modal } from "@arco-design/web-vue";

const props = defineProps(["detailsData"]);
const detailsData = computed(() => props.detailsData);
const route = useRoute();
const columns = [
  { title: "实例名称", dataIndex: "name" },
  { title: "实例状态", slotName: "status" },
  { title: "主从模式", dataIndex: "masterSlaveMode" },
  { title: "规格", slotName: "requestResource" },
  { title: "实例IP", dataIndex: "podIps" },
  { title: "主机IP", dataIndex: "nodeIp" },
  { title: "创建时间", slotName: "createTime" },
  { title: "重启时间", slotName: "lastRestartTime" },
  { title: "操作", slotName: "opt" },
];
const podList = ref([]);
const loading = ref(false);

let plTimer = null;
async function getPodList() {
  // 轮询挂一层保险
  if (!route.query.id || route.query.id === 0) {
    clearInterval(plTimer);
    return;
  }
  await clusterPodList(route.query.id).then((res) => {
    podList.value = res.data || {};
  });
}
// 定时查询
async function getPodListLoop() {
  if (plTimer) clearInterval(plTimer);
  await getPodList();
  plTimer = setInterval(() => {
    getPodList();
  }, 5000);
}
async function refresh() {
  loading.value = true;
  getPodListLoop().finally(() => {
    loading.value = false;
  });
}
function rollingRestart() {
  Modal.info({
    title: "确认重启",
    content: "确定要重启？",
    simple: true,
    okText: "重启",
    hideCancel: false,
    okButtonProps: {
      status: "danger",
    },
    "on-before-ok": (done) => {
      clusterPodRestart(route.query.id)
        .then((res) => {
          if (res.code === 200) {
            getPodListLoop();
            Message.success(res.msg);
          }
        })
        .finally(() => done());
    },
  });
}
function deletePod({ name }) {
  Modal.info({
    title: "确认关闭",
    content: "确定要关闭？",
    simple: true,
    okText: "关闭",
    hideCancel: false,
    okButtonProps: {
      status: "danger",
    },
    "on-before-ok": (done) => {
      clusterPodDelete(route.query.id, name)
        .then((res) => {
          if (res.code === 200) {
            getPodListLoop();
            Message.success("关闭成功!");
          }
        })
        .finally(() => done());
    },
  });
}
function setMaster({ name }) {
  Modal.info({
    title: "确认设为主",
    content: "确定要设为主？",
    simple: true,
    okText: "设置",
    hideCancel: false,
    okButtonProps: {
      status: "danger",
    },
    "on-before-ok": (done) => {
      setTheMasterNode({ id: route.query.id, targetMasterPod: name })
        .then((res) => {
          if (res.code === 200) {
            getPodListLoop();
            Message.success(res.msg);
          }
        })
        .finally(() => done());
    },
  });
}

watch(
  () => route.query.id,
  () => {
    if (route.query.id) {
      getPodListLoop();
    }
  }
);
onBeforeMount(() => {
  getPodListLoop();
});
// 卸载时解除自动请求
onUnmounted(() => {
  clearInterval(plTimer);
});
</script>
<style lang="less" scoped>
.cluster-details-title {
  display: flex;
  padding: 20px 0;
  width: 100%;
  align-items: center;
  justify-content: space-between;
  .green {
    color: rgb(var(--green-6));
  }
  .gold {
    color: rgb(var(--gold-6));
  }
  .red {
    color: rgb(var(--red-6));
  }
}
</style>
