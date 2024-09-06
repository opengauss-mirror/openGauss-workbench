<template>
  <div class="select-comp-container cm-search-form-content">
    <a-form :model="searchParams" style="width: auto" layout="inline">
      <a-button type="primary" @click="toNewCluster" style="margin-right: 16px"
        >创建集群</a-button
      >
      <a-form-item label="所属域">
        <a-select
          v-model="searchParams.k8sId"
          @change="search"
          unmount-on-close
          allow-clear
          style="width: fit-content"
          placeholder="请选择所属域"
          :trigger-props="triggerProps"
          popup-container=".select-comp-container"
        >
          <a-option v-for="domain of domainList" :value="domain.id">{{
            domain.name
          }}</a-option>
        </a-select>
      </a-form-item>
      <a-form-item label="集群名称">
        <a-input
          v-model="searchParams.ogcName"
          @press-enter="search"
          @blur="search"
          placeholder="请输入集群名称"
        />
      </a-form-item>
    </a-form>
    <a-space>
      <refresh-btn :onClick="getList"></refresh-btn>
      <a-button
        v-show="dangerOpt"
        @click="changeDangerOpt(false)"
        type="primary"
        >关闭危险操作</a-button
      >
      <a-button
        v-show="!dangerOpt"
        @click="changeDangerOpt(true)"
        type="primary"
        >打开危险操作</a-button
      >
    </a-space>
  </div>
  <div class="cm-table-content">
    <a-table
      :columns="columns"
      row-key="id"
      :bordered="false"
      :data="tableData"
      :pagination="false"
    >
      <template #name="{ record }">
        <a-link @click="toDetails(record.id)">{{ record.name }}</a-link>
      </template>
      <template #domains="{ record }">
        <div v-for="domain of record.domains || []">{{ domain }}</div>
      </template>
      <template #size="{ record }">
        <div>
          {{
            record?.resourceRequest?.cpu && `CPU:${record.resourceRequest.cpu}C`
          }}
        </div>
        <div>
          {{
            record?.resourceRequest?.memory &&
            `内存:${record.resourceRequest.memory}`
          }}
        </div>
        <div v-if="record?.cpuBurstMultiple > 1">
          {{ `CPU爆发:${record.cpuBurstMultiple}倍` }}
        </div>
      </template>
      <template #createTime="{ record }">
        {{
          record.createTime &&
          dayjs(record.createTime).format("YYYY-MM-DD HH:mm:ss")
        }}
      </template>
      <template #clusterStatus="{ record }">
        <a-tag v-if="record.clusterStatus === 'Running'" bordered color="green"
          >正常</a-tag
        >
        <a-tag
          v-else-if="record.clusterStatus === 'Creating'"
          bordered
          color="gold"
          >创建中</a-tag
        >
        <a-tag v-else bordered color="red">异常</a-tag>
      </template>
      <template #opt="{ record }">
        <a-button
          v-show="dangerOpt"
          type="primary"
          status="danger"
          size="small"
          @click="deleteBtn(record.id)"
          >删除</a-button
        >
      </template>
    </a-table>
    <cm-pagination
      :current="pagination.pageNum"
      :page-size="pagination.pageSize"
      :total="pagination.total"
      @change="pageNumChange"
      @page-size-change="pageSizeChange"
    ></cm-pagination>
  </div>
  <new-cluster
    ref="modalRef"
    :domainList="domainList"
    :systemList="systemList"
    @refresh="getList"
  ></new-cluster>
</template>
<script setup>
import {
  systemListData,
  domainListData,
  cluterListData,
  deleteCluster,
} from "@/api/clusterlist";
import { reactive, ref, onBeforeMount } from "vue";
import dayjs from "dayjs";
import newCluster from "./newCluster.vue";
import { Modal } from "@arco-design/web-vue";
import { Message } from "@arco-design/web-vue";
import { useRouter } from "vue-router";

const triggerProps = {
  autoFitPosition: true,
  autoFitPopupMinWidth: true,
};
const router = useRouter();

const dangerOpt = ref(false);
function changeDangerOpt(show) {
  dangerOpt.value = show;
}

const searchParams = reactive({
  k8sId: "",
  ogcName: "",
});
const systemList = ref([]);
const domainList = ref([]);
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
});
const columns = [
  { title: "集群名称", slotName: "name" },
  { title: "所属系统", dataIndex: "cmdbSysName" },
  { title: "域名", slotName: "domains" },
  { title: "端口", dataIndex: "port" },
  { title: "实例数", dataIndex: "instance" },
  { title: "规格", slotName: "size" },
  { title: "创建时间", slotName: "createTime" },
  { title: "集群状态", slotName: "clusterStatus" },
  { title: "操作", slotName: "opt", width: 100 },
];
const tableData = ref([]);

const modalRef = ref(null);

function toNewCluster() {
  modalRef.value.open();
}

function search() {
  pagination.pageNum = 1;
  getList();
}
function getList() {
  const params = {
    pageNum: pagination.pageNum,
    pageSize: pagination.pageSize,
  };
  if (searchParams.k8sId) params.k8sId = searchParams.k8sId;
  if (searchParams.ogcName) params.ogcName = searchParams.ogcName;
  return cluterListData(params).then((res) => {
    tableData.value = res.data.data || [];
    pagination.total = res.data.total || 0;
  });
}

function pageNumChange(pageNum) {
  pagination.pageNum = pageNum;
  getList();
}
function pageSizeChange(pageSize) {
  pagination.pageSize = pageSize;
  search();
}
function deleteBtn(id) {
  Modal.info({
    title: "确认删除",
    content: "确定要删除？",
    simple: true,
    okText: "删除",
    hideCancel: false,
    okButtonProps: {
      status: "danger",
    },
    "on-before-ok": async () => {
      const res = await deleteCluster(id);
      if (res.code === 200) {
        getList();
        Message.success("删除成功!");
        return true;
      } else {
        return false;
      }
    },
  });
}
function toDetails(id) {
  router.push(`/cluster-manage/cluster-list/details?id=${id}`);
}

onBeforeMount(() => {
  systemListData().then((res) => {
    systemList.value = res.data || [];
  });
  domainListData().then((res) => {
    domainList.value = res.data || [];
  });
  getList();
});
</script>
