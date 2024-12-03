<template>
  <div style="padding: 20px">
    <div class="cluster-details-title">
      <span
        >服务名称：
        <a-link @click="copy2clipboard(detailsData.name)">{{
          detailsData.name
        }}</a-link>
      </span>
      <a-space>
        <a-button type="primary" @click="refresh" :loading="loading"
          >刷新</a-button
        >
        <a-button type="primary" @click="back">返回</a-button>
        <a-button
          type="primary"
          @click="detailsData.isAddMonitor ? deleteMonitor() : addMonitor()"
        >
          {{ detailsData.isAddMonitor ? "删除监控" : "添加监控" }}<icon-down />
        </a-button>
      </a-space>
    </div>
    <a-tabs default-active-key="1" style="width: 100%" lazy-load>
      <a-tab-pane key="1" title="集群详情">
        <div class="cm-search-form-content" style="align-items: start">
          <desc-item label="所属域">{{ detailsData.k8sName }}</desc-item>
          <desc-item label="资源池类型">{{
            detailsData.resourceType
          }}</desc-item>
          <desc-item label="集群架构">
            {{ detailsData.archType && archTypeMap[detailsData.archType] }}
          </desc-item>
          <desc-item label="OpenGauss镜像">{{
            detailsData.openGaussImage
          }}</desc-item>
          <desc-item label="端口号">{{ detailsData.port }}</desc-item>
          <desc-item label="备份镜像">{{
            detailsData.backRecoveryImage
          }}</desc-item>
          <desc-item label="集群规模">{{ detailsData.instance }}</desc-item>
          <desc-item label="监控镜像">{{
            detailsData.exporterImage
          }}</desc-item>
          <desc-item label="实例规格">
            {{
              `${detailsData.requestResource?.cpu}C ${detailsData.requestResource?.memory}`
            }}
            <a-link @click="toEditRR">
              <icon-edit />
            </a-link>
          </desc-item>
          <desc-item label="创建者">{{ detailsData.creator }}</desc-item>
          <desc-item label="CPU爆发数">
            {{ detailsData.scaleTimes }}
            <a-link @click="toEditRR">
              <icon-edit />
            </a-link>
          </desc-item>
          <desc-item label="监控">{{
            detailsData.isAddMonitor ? "已添加" : "未添加"
          }}</desc-item>
          <desc-item label="存储容量"
            >{{ detailsData.diskCapacity }}G</desc-item
          >
          <desc-item label="创建时间">
            {{
              detailsData.createTime &&
              dayjs(detailsData.createTime).format("YYYY-MM-DD HH:mm:ss")
            }}
          </desc-item>
          <desc-item label="测试集群">{{
            detailsData.isTestCluster ? "是" : "否"
          }}</desc-item>
          <desc-item label="域名">
            <a-link
              v-for="domain in detailsData.domain"
              @click="copy2clipboard(domain)"
              style="display: block"
            >
              {{ domain }}
            </a-link>
          </desc-item>
          <desc-item label="备注">{{ detailsData.remark }}</desc-item>
        </div>
        <pod-list :detailsData="detailsData"></pod-list>
      </a-tab-pane>
      <a-tab-pane key="2" title="备份恢复"> 备份恢复 </a-tab-pane>
      <a-tab-pane key="3" title="集群配置"> 集群配置 </a-tab-pane>
    </a-tabs>
  </div>
  <request-resource ref="RRRef" @refresh="getDetailsData"></request-resource>
</template>

<script setup>
import { onBeforeMount, ref, watch } from "vue";
import {
  addClusterMonitor,
  cluterDetails,
  deleteClusterMonitor,
} from "@/api/clusterlist.js";
import { useRoute, useRouter } from "vue-router";
import dayjs from "dayjs";
import { Message, Modal } from "@arco-design/web-vue";
import requestResource from "./requestResource.vue";
import podList from "./podList.vue";

const modalConfig = {
  simple: true,
  hideCancel: false,
  okButtonProps: {
    status: "danger",
  },
};
const archTypeMap = {
  merge: "集成",
};

const route = useRoute();
const router = useRouter();

const detailsData = ref({});
const RRRef = ref(null);
const loading = ref(false);

function back() {
  router.push("/cluster-manage/cluster-list");
}

async function getDetailsData() {
  await cluterDetails(route.query.id).then((res) => {
    detailsData.value = res.data || {};
  });
}
function copy2clipboard(text) {
  navigator.clipboard.writeText(text);
  Message.success("已复制到剪贴板");
}
// 修改实例规格相关
function toEditRR() {
  RRRef.value.open(detailsData.value);
}
async function refresh() {
  loading.value = true;
  getDetailsData().finally(() => {
    loading.value = false;
  });
}

function addMonitor() {
  Modal.info({
    ...modalConfig,
    title: "确认添加",
    content: "确定要添加监控？",
    okText: "添加",
    "on-before-ok": (done) => {
      addClusterMonitor(route.query.id)
        .then((res) => {
          if (res.code === 200) {
            getDetailsData();
            Message.success("添加完成");
          }
        })
        .finally(() => done(true));
    },
  });
}
function deleteMonitor() {
  Modal.info({
    ...modalConfig,
    title: "确认删除",
    content: "确定要删除监控？",
    okText: "删除",
    "on-before-ok": (done) => {
      deleteClusterMonitor(route.query.id)
        .then((res) => {
          if (res.code === 200) {
            getDetailsData();
            Message.success("删除完成");
          }
        })
        .finally(() => done(true));
    },
  });
}
watch(
  () => route.query.id,
  () => {
    if (route.query.id) {
      getDetailsData();
    }
  }
);
onBeforeMount(() => {
  getDetailsData();
});
</script>

<style lang="less" scoped>
.cluster-details-title {
  display: flex;
  padding: 8px 0;
  width: 100%;
  align-items: center;
  justify-content: space-between;
}
</style>
