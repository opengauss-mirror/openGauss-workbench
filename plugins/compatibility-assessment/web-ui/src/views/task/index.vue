<template>
  <div class="package-list" id="compatibilityTask">
    <div class="flex-between mb">
      <div class="flex-row">
        <a-button type="primary" class="mr" @click="handleAdd('create')">
          <template #icon>
            <icon-plus />
          </template>
          {{ $t("packageManage.index.5myq5c8yz7c0") }}
        </a-button>
      </div>
      <div class="flex-row">
        <div class="flex-row mr">
          <div class="label-color top-label mr-s">
            {{ $t("packageManage.index.5myq5c8zns00") }}
          </div>
          <a-select
            style="width: 200px"
            v-model="filter.host"
            :placeholder="$t('packageManage.index.else3')"
            allow-clear
          >
            <a-option
              v-for="(item, index) in packageVersionList"
              :key="index"
              :label="item.label"
              :value="item.value"
            />
          </a-select>
        </div>
        <a-input-search
          v-model="filter.taskName"          
          allowClear
          @search="isFilter"
          @press-enter="isFilter"
          @clear="isFilter"
          :placeholder="$t('packageManage.index.5myq5c8z8540')"
          search-button
        />
        <a-button type="primary" style="margin:0 0 0 20px" class="mr arco-input-append" @click="getListData()">
          <template #icon>
            <icon-refresh />
          </template>
        </a-button>
      </div>
    </div>
    <a-table
      class="d-a-table-row"
      :data="list.data"
      :columns="columns"
      :pagination="list.page"
      @page-change="currentPage"
      :loading="list.loading"
    >
      <template #version="{ record }">
        {{ getVersionName(record.packageVersion) }}
      </template>
      <template #timeInterval="{ record }">
        <a-tooltip position="top">
          <template #content>
            <div
              style="margin-bottom:'5px'"
              v-for="(data, index) in record.timeInterval.split(',')"
              :key="index"
              >{{ data }}
            </div>
          </template>
           <div>{{ record.timeInterval }}</div>
        </a-tooltip>
      </template>
      <template #currentStatus="{ record }">
        <div v-if="record.currentStatus === 'running'">
          <div
            style="
              display: inline-block;
              width: 12px;
              height: 12px;
              border-radius: 6px;
              background-color: #00d0b6;
              margin-right: 5px;
            "
          ></div>
          <span style="color: #00d0b6">Running</span>
        </div>
        <div v-else-if="record.currentStatus === 'stopping'">
          <div
            style="
              display: inline-block;
              width: 12px;
              height: 12px;
              border-radius: 6px;
              background-color: lightcoral;
              margin-right: 5px;
            "
          ></div>
          <span style="color: lightcoral">Stopping</span>
        </div>
        <div v-else-if="record.currentStatus === 'completed'">
          <div
            style="
              display: inline-block;
              width: 12px;
              height: 12px;
              border-radius: 6px;
              background-color: #14a9f8;
              margin-right: 5px;
            "
          ></div>
          <span style="color: #14a9f8">completed</span>
        </div>
        <div v-else-if="record.currentStatus === 'waiting'">
          <div
            style="
              display: inline-block;
              width: 12px;
              height: 12px;
              border-radius: 6px;
              background-color: #f8ca14;
              margin-right: 5px;
            "
          ></div>
          <span style="color: #f8ca14">waiting</span>
        </div>
      </template>
      <template #operation="{ record }">
        <div class="flex-row-start">
          <a-link class="mr" @click="handleAdd('update', record)">{{
            $t("packageManage.index.5myq5c8zmbk0")
          }}</a-link>
          <a-popconfirm
            :content="$t('packageManage.index.5myq5c8zms40')"
            type="warning"
            :ok-text="$t('packageManage.index.5myq5c8zn100')"
            :cancel-text="$t('packageManage.index.5myq5c8zn7k0')"
            @ok="deleteRows(record)"
          >
            <a-link status="danger">{{
              $t("packageManage.index.5myq5c8znew0")
            }}</a-link>
          </a-popconfirm>
          <div @click="fileDownload(record)">
            <a-link status="danger" style="margin: 0 0px 0 10px">{{
              $t("packageManage.index.5myq5c8znew1")
            }}</a-link>
          </div>

          <a-modal
            simple
            v-model:visible="record.downloading"
            hide-cancel
            :mask-closable="false"
          >
            <template #title>
              <div>{{ $t("packageManage.index.5mja76hbim00") }}</div>
            </template>
            <a-progress :percent="record.progress" />
          </a-modal>
          <div style="display: flex; align-items: center; margin: 0 0px 0 10px">
            <a-switch
              size="medium"
              v-model="record.switchStatus"
              @change="handleSwitchChange(record)"
            >
            </a-switch>
            <span
              v-if="record.switchStatus"
              style="color: black; margin-left: 10px"
              >{{ $t("packageManage.index.5mja76hbim001") }}</span
            >
            <span v-else style="color: black; margin-left: 10px">{{ $t("packageManage.index.5mja76hbim002") }}</span>
          </div>
        </div>
      </template>
    </a-table>
    <add-package-dlg
      ref="addPackageRef"
      @finish="getListData"
    ></add-package-dlg>
  </div>
</template>

<script setup lang="ts">
import { KeyValue } from "@/types/global";
import { Message } from "@arco-design/web-vue";
import { computed, onMounted, reactive, ref ,onUnmounted} from "vue";
import {
  packagePage,
  packageDel,
  getAllIps,
  downloadFile,
  startTask,
} from "@/api/ops"; // eslint-disable-line
import AddPackageDlg from "./AddPackageDlg.vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
const { t } = useI18n();
const route = useRoute();
const filter = reactive({
  taskName: "",
  host: "",
  pageNum: 1,
  pageSize: 10,
});

const columns = computed(() => [
  {
    title: t("packageManage.index.5myq5c8zpu85"),
    dataIndex: "taskName",
    ellipsis: true,
    width: 80,
    align: "left",
    tooltip: true,
  },
  {
    title: t("packageManage.index.5myq5c8zpu80"),
    dataIndex: "host",
    ellipsis: true,
    width: 80,
    align: "center",
    tooltip: true,
  },
  {
    title: t("packageManage.index.5myq5c8zpu82"),
    dataIndex: "timeInterval",
    slotName: "timeInterval",
    ellipsis: true,
    width: 120,
    align: "center",
    // tooltip: true,
  },
  {
    title: t("packageManage.index.5myq5c8zu5w0"),
    dataIndex: "pid",
    ellipsis: true,
    align: "left",
    width: 100,
  },
  {
    title: t("packageManage.index.4myq5c8zu5w0"),
    dataIndex: "filePath",
    ellipsis: true,
    align: "left",
    width: 100,
  },
  {
    title: t("packageManage.index.5myq5c8zu5w1"),
    dataIndex: "currentStatus",
    slotName: "currentStatus",
    align: "left",
    width: 120,
  },
  {
    title: t("packageManage.index.5myq5c8zq380"),
    slotName: "operation",
    width: 180,
  },
]);

const list = reactive<KeyValue>({
  data: [],
  page: {
    total: 0,
    pageSize: 10,
    "show-total": true,
  },
  loading: false,
});

const packageVersionList = ref<KeyValue[]>([]);

const fetchPackageVersionList = async () => {
  try {
    const res = await getAllIps();
    if (Number(res.code) === 200) {
      packageVersionList.value = res.obj.map((item: KeyValue) => ({
        label: item,
        value: item,
      }));
    }
  } catch (error) {
    console.error(error);
  }
};
let intervalId;
onMounted(async () => {
  if (route.query?.taskName) {
    filter.taskName = route.query?.taskName;
  }
  await getListData();
  await fetchPackageVersionList(); // 调用接口获取packageVersionList的值
  intervalId = setInterval(getListData, 10000);
});

onUnmounted(() => {
  // 组件卸载时清除定时器
  if (intervalId) {
    clearInterval(intervalId);
  }
});

const getListData = () => {
  list.loading = true;
  packagePage(filter)
    .then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        list.data = res.obj;
        list.page.total = res.total;
      }
    })
    .finally(() => {
      list.loading = false;
    });
};

const currentPage = (e: number) => {
  filter.pageNum = e;
  getListData();
};

const isFilter = () => {
  filter.pageNum = 0;
  getListData();
};

const addPackageRef = ref<null | InstanceType<typeof AddPackageDlg>>(null);
const handleAdd = (type: string, data?: KeyValue) => {
  addPackageRef.value?.open(type, data);
};

const deleteRows = (record: KeyValue) => {
  console.log(record);
  packageDel(record.taskId).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: "delete success",
      });
      getListData();
    }
  });
};

const handleSwitchChange = (record) => {
  startTask(record)
    .then((res) => {
      if (Number(res.code) === 200) {
        Message.success({
          content: "start success",
        });
        getListData();
      } else {
        Message.success({
          content: res.msg,
        });
        getListData();
      }
    })
    .catch((error) => {
      getListData();
    });
};

const downloadStatus = reactive({
  progress: 0.1,
  downloading: false,
});

// 在 Promise 的回调函数中，当接口调用成功时，调用 getListData() 来更新数据列表。
// 在接口调用失败时，则显示错误信息弹窗。
// 添加 catch() 方法，当接口调用发生错误时，也能够显示错误信息弹窗提示。

const fileDownload = (record: KeyValue) => {
  record.downloading = true;
  record.progress = 0;
  let progess = 0;
  const interval = setInterval(() => {
    progess += Math.random() * (0.08 - 0.01) + 0.01;
    if (progess < 0.9) {
      record.progress = Math.floor((progess / 1) * 100) / 100;
    } else {
      record.progress = Math.floor((0.99 / 1) * 100) / 100;
    }
  }, 1000);
  downloadFile(record.host, record.filePath)
    .then((res: any) => {
      if (res) {
        const blob = new Blob([res], {
          type: "zip",
        });
        const a = document.createElement("a");
        const URL = window.URL || window.webkitURL;
        const herf = URL.createObjectURL(blob);
        a.href = herf;
        a.download = "sql_stack.zip";
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(herf);
      }
      record.progress = 1;
      record.downloading = false;
      clearInterval(interval);
      refreshPage();
      getListData();
    })
    .catch((error) => {
      record.downloading = false;
      refreshPage();
      getListData();
      console.error("Error downloading file:", error);
    });
};

const refreshPage = () => {
  // 执行刷新页面的操作，例如重新加载数据等
  window.location.reload(); // 示例，使用 window.location.reload() 方法刷新页面
};

const getVersionName = (version: string) => {
  if (version === "ENTERPRISE") {
    return t("packageManage.index.5myq5c8zu5w0");
  } else if (version === "MINIMAL_LIST") {
    return t("packageManage.index.5myq5c8zuxc0");
  } else {
    return t("packageManage.index.5myq5c8zw680");
  }
};

const getPackagePath = (value: KeyValue) => {
  if (value) {
    return value.name;
  }
  return "";
};
</script>
<style>
body,
html {
  width: 100% !important;
}
</style>
<style lang="less" scoped>
.package-list {
  padding: 20px;
  border-radius: 8px;
  .top-label {
    white-space: nowrap;
  }

  .select-w {
    width: 200px;
  }
}
</style>
