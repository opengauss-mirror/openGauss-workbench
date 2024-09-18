<template>
  <div class="downloadContainer" >
    <div class="home-container">
      <div class="formCommon">
        <a-collapse style="margin-bottom: 20px;">
          <a-collapse-item :header="$t('home.index.1mq3hrx8s6o6')" key="1">
            <a-row class="grid-demo">
              <a-col :span="1">
                <p style="text-align: center;font-size: 20px;"><icon-exclamation-circle-fill /></p>
              </a-col>
              <a-col :span="23">
                <p>{{$t("home.index.1mq3hrx8s6o2")}}</p>
                <p>{{$t("home.index.1mq3hrx8s6o3")}}</p>
                <p>{{$t("home.index.1mq3hrx8s6o4")}}</p>
                <p>{{$t("home.index.1mq3hrx8s6o5")}}</p>
              </a-col>
            </a-row>
          </a-collapse-item>
        </a-collapse>
        <a-form ref="formCommonRef" :model="formCommon" auto-label-width>
          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item
                label-col-flex="80px"
                field="sqlInputType"
                :label="$t('home.index.5mq3d6o5z6g15')"
                :rules="[
                  {
                    required: true,
                    message: $t('home.index.5mq3d6o5z6g16'),
                  },
                ]"
              >
                <a-select
                  v-model="formCommon.sqlInputType"
                  :placeholder="$t('home.index.5mq3d6o5z6g15')"
                  @change="onTypeChange"
                  dropdown-style="{ top: 'auto', bottom: 0 }"
                >
                  <a-option label="file" :value="file" :key="1"></a-option>
                  <a-option label="collect" :value="collect" :key="0"></a-option>
                  <a-option
                    label="Attach_Application"
                    :value="Attach_Application"
                    :key="2"
                  ></a-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="8" v-if="isPidType">
              <a-form-item
                label-col-flex="80px"
                field="proccessPid"
                :label="$t('home.index.5mq3d6o5z6g21')"
                :rules="[
                  {
                    required: isPidType,
                    message: $t('home.index.5mq3d6o5z6g20'),
                  },
                ]"
              >
                <a-select
                  :disabled="!isPidType"
                  v-model="formCommon.proccessPid"
                  :placeholder="$t('home.index.5mq3d6o5z6g20')"
                  @change="onTypeChange"
                  dropdown-style="{ top: 'auto', bottom: 0 }"
                >
                  <a-option
                    v-for="option in completePidList"
                    :label="option.label"
                    :value="option.value"
                    :key="option.value"
                  ></a-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="8" v-if="isCollectInputType">
              <a-form-item
                label-col-flex="90px"
                field="assessmenttype"
                :label="$t('home.index.5mq3d6o5z6g2')"
                :rules="[
                  {
                    required: isCollectInputType,
                    message: $t('home.index.5mq3d6o5z6g9'),
                  },
                ]"
              >
                <a-select
                  :disabled="!isCollectInputType"
                  v-model="formCommon.assessmenttype"
                  :placeholder="$t('home.index.5mq3d6o5z6g10')"
                  @change="onTypeChange"
                  dropdown-style="{ top: 'auto', bottom: 0 }"
                >
                  <a-option label="sql" :value="sql" :key="0"></a-option>
                  <a-option label="all" :value="all" :key="1"></a-option>
                  <a-option label="object" :value="object" :key="2"></a-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item
                v-if="isSqlType"
                label-col-flex="85px"
                field="sqltype"
                :label="$t('home.index.5mq3d6o5z6g3')"
                :rules="[
                  {
                    required: isSqlType,
                    message: $t('home.index.5mq3d6o5z6g12'),
                  },
                ]"
              >
                <a-select
                  :disabled="!isSqlType"
                  v-model="formCommon.sqltype"
                  :placeholder="$t('home.index.5mq3d6o5z6g11')"
                  @change="onTypeChange"
                  dropdown-style="{ top: 'auto', bottom: 0 }"
                >
                  <a-option label="slow" :value="slow" :key="1"></a-option>
                  <a-option label="general" :value="general" :key="0"></a-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <div style="display: flex; width: 100%">
        <div class="form-con">
          <div class="form-left" v-if="isFileInputType">
            <a-card :style="{ width: '450px', height: '400px' }" bordered>
              <template v-slot:title>
                <div class="card-title">
                  <span class="card-title-text">{{
                    $t("home.index.1mq3hrx8s6o1")
                  }}</span>
                  <a-tooltip
                    :content-style="{ color: '#1d212a' }"
                    background-color="#fff7e8"
                    :content="`当前的上传路径为: ${filePath}，您可以在系统设置中进行更改`"
                    position="right"
                  >
                    <icon-question-circle-fill
                      :style="{ 'margin-left': '5px', color: '#ff7d00' }"
                    />
                  </a-tooltip>
                </div>
              </template>
              <a-form ref="formCommonRef" :model="formCommon" auto-label-width>
                <div>
                  <div style="margin-bottom: 20px">
                    <a-space>
                      <span>Type: </span>
                      <a-radio-group
                        v-model="formCommon.typeChecked"
                        :default-checked="zip"
                      >
                        <a-radio value="zip">zip</a-radio>
                      </a-radio-group>
                    </a-space>
                  </div>
                  <a-upload
                    v-model="formCommon.file"
                    name="file"
                    action="/"
                    :limit="1"
                    @before-upload="beforeUpload"
                    :show-retry-button="false"
                    :auto-upload="false"
                    @before-remove="deletedata"
                  />
                </div>
              </a-form>
            </a-card>
          </div>
          <div class="form-left" v-if="isCollectInputType">
            <a-card
              :style="{ width: '450px', height: '400px' }"
              :title="$t('home.index.5mq3hrx8s6o0')"
              bordered
            >
              <a-form
                ref="formLeftRef"
                :model="formLeft"
                auto-label-width
                :rules="formRules"
              >
                <a-form-item
                  field="mysqlHost"
                  :label="$t('home.index.5mq3d6o5z6g0')"
                  validate-trigger="change"
                >
                  <a-select
                    v-model="formLeft.mysqlHost"
                    :placeholder="$t('home.index.5mq3d6o5zjk0')"
                    allow-create
                    @change="getData(0)"
                  >
                    <a-option
                      v-for="option in leftIpsList"
                      :label="option.label"
                      :value="option.value"
                      :key="option.value"
                    ></a-option>
                  </a-select>
                </a-form-item>
                <a-form-item
                  field="mysqlPort"
                  :label="$t('home.index.5mq3d6o608g0')"
                  validate-trigger="change"
                >
                  <a-input
                    v-model="formLeft.mysqlPort"
                    :placeholder="$t('home.index.5mq3d6o60mc0')"
                  />
                </a-form-item>
                <a-form-item
                  field="mysqlUser"
                  :label="$t('home.index.5mq3d6o60po0')"
                  validate-trigger="change"
                >
                  <a-input
                    v-model="formLeft.mysqlUser"
                    :placeholder="$t('home.index.5mq3d6o60vw0')"
                  />
                </a-form-item>
                <a-form-item
                  field="mysqlPassword"
                  :label="$t('home.index.5mq3d6o612g0')"
                >
                  <a-input-password
                    v-model="formLeft.mysqlPassword"
                    :placeholder="$t('home.index.5mq3d6o62lc0')"
                  />
                </a-form-item>
                <a-form-item field="mysqlDbname" :label="$t('home.index.1mq3hrx8s6o7')" validate-trigger="change" >
                  <a-select
                    v-model="formLeft.mysqlDbname"
                    :placeholder="$t('home.index.5mq3d6o66ys0')"
                    allow-create
                  >
                    <a-option
                      v-for="option in leftDbList"
                      :label="option.label"
                      :value="option.value"
                      :key="option.value"
                    ></a-option>
                  </a-select>
                </a-form-item>
              </a-form>
            </a-card>
          </div>
          <div class="form-right">
            <a-card
              :style="{ width: '450px', height: '400px' }"
              :title="$t('home.index.5mq3d6o677g0')"
              bordered
            >
              <a-form
                ref="formRightRef"
                :model="formRight"
                auto-label-width
                :rules="formRules"
              >
                <a-form-item
                  field="opengaussHost"
                  :label="$t('home.index.5mq3d6o5z6g0')"
                  validate-trigger="change"
                >
                  <a-select
                    v-model="formRight.opengaussHost"
                    :placeholder="$t('home.index.5mq3d6o5zjk0')"
                    allow-create
                    @change="getData(1)"
                  >
                    <a-option
                      v-for="option in rightIpsList"
                      :label="option.label"
                      :value="option.value"
                      :key="option.value"
                    ></a-option>
                  </a-select>
                </a-form-item>
                <a-form-item
                  field="opengaussPort"
                  :label="$t('home.index.5mq3d6o608g0')"
                  :validate-trigger="change"
                >
                  <a-input
                    v-model="formRight.opengaussPort"
                    :placeholder="$t('home.index.5mq3d6o60mc0')"
                  />
                </a-form-item>
                <a-form-item
                  field="opengaussUser"
                  :label="$t('home.index.5mq3d6o60po0')"
                  :validate-trigger="change"
                  :rules="[
                    {
                      required: true,
                      message: $t('home.index.5mq3d6o60t00'),
                    },
                  ]"
                >
                  <a-input
                    v-model="formRight.opengaussUser"
                    :placeholder="$t('home.index.5mq3d6o60vw0')"
                  />
                </a-form-item>
                <a-form-item
                  field="opengaussPassword"
                  :label="$t('home.index.5mq3d6o612g0')"
                  :rules="[
                    {
                      required: true,
                      message: $t('home.index.5mq3d6o615g0'),
                    },
                  ]"
                >
                  <a-input-password
                    v-model="formRight.opengaussPassword"
                    :placeholder="$t('home.index.5mq3d6o62lc0')"
                  />
                </a-form-item>
                <a-form-item
                  field="opengaussDbname"
                  :label="$t('home.index.1mq3hrx8s6o7')"
                  validate-trigger="change"
                >
                  <a-select
                    v-model="formRight.opengaussDbname"
                    :placeholder="$t('home.index.5mq3d6o66ys0')"
                    allow-create
                  >
                    <a-option
                      v-for="option in rightDbList"
                      :label="option.label"
                      :value="option.value"
                      :key="option.value"
                    ></a-option>
                  </a-select>
                </a-form-item>
              </a-form>
            </a-card>
          </div>
        </div>
        <div class="button">
          <div style="display: flex; align-items: center; margin-left: 60px">
            <a-button
              style="width: 150px"
              type="primary"
              :loading="loading"
              @click="startSync"
              >{{
                loading
                  ? $t("home.index.5mq3d6o67dc1")
                  : $t("home.index.5mq3d6o67fo0")
              }}</a-button
            >
          </div>
        </div>
      </div>
      <div style="width:100%">
        <a-table :columns="columns" :data="list.data" :loading="list.loading" @page-change="currentPage" :pagination="list.page" style="margin-top: 20px">
              <template #mysqlHost="{ record }">
                <a-tooltip position="top">
                  <template #content>
                    <div style="margin-bottom:'5px'"  >{{$t("home.index.5mq3d6o5z6g0")}}:{{record.mysqlHost}}</div>
                    <div style="margin-bottom:'5px'"  >{{$t("home.index.5mq3d6o608g0")}}:{{record.mysqlPort}}</div>
                    <div style="margin-bottom:'5px'"  >{{$t("home.index.5mq3d6o60po0")}}:{{record.mysqlUser}}</div>
                    <div style="margin-bottom:'5px'"  >{{$t('home.index.1mq3hrx8s6o7')}}:{{record.mysqlDbname}}</div>
                    <div style="margin-bottom:'5px'"  >{{$t("home.index.5mq3d6o612g0")}}:{{record.mysqlPassword}}</div>
                  </template>
                  <div>{{record.mysqlHost}}</div>
                </a-tooltip>
              </template>
              <template #opengaussHost="{ record }">
                <a-tooltip position="top" >
                  <div>{{record.opengaussHost}}</div>
                  <template #content>
                    <div style="margin-bottom:'5px'"  >{{$t("home.index.5mq3d6o5z6g0")}}:{{record.opengaussHost}}</div>
                    <div style="argin-bottom:'5px'"  >{{$t("home.index.5mq3d6o608g0")}}:{{record.opengaussPort}}</div>
                    <div style="margin-bottom:'5px'"  >{{$t("home.index.5mq3d6o60po0")}}:{{record.opengaussUser}}</div>
                    <div style="margin-bottom:'5px'"  >{{$t("home.index.1mq3hrx8s6o7")}}:{{record.opengaussDbname}}</div>
                    <div style="margin-bottom:'5px'"  >{{$t("home.index.5mq3d6o612g0")}}:{{record.opengaussPassword}}</div>
                  </template>
                </a-tooltip>
              </template>
              <template #operation="{ record }">
                <a-link class="mr" @click="fileDownload(record.reportFileName)">{{
                  $t("home.index.5mq3d6o67fo1")
                }}</a-link>
                <a-popconfirm
                  :content="$t('packageManage.index.5myq5c8zms40')"
                  type="warning"
                  :ok-text="$t('packageManage.index.5myq5c8zn100')"
                  :cancel-text="$t('packageManage.index.5myq5c8zn7k0')"
                  @ok="deleteRows(record)"
                >
                  <a-link  status="danger">{{
                    $t("packageManage.index.5myq5c8znew0")
                  }}</a-link>
                </a-popconfirm>
              </template>
        </a-table>
      </div>
    </div>
  </div>
  <a-modal v-model:visible="modalVisible" :popupContainer="modalContainer" :title="$t('home.index.1mq3hrx8s6o8')"  @cancel="handleCancel" :footer="false"  unmountOnClose fullscreen>
    <div v-html="modalContent"></div>
  </a-modal>
</template>

<script setup>
import { KeyValue } from "@/types/global";
import {
  reactive,
  ref,
  onMounted,
  onUnmounted,
  computed,
  watchEffect,
  nextTick
} from "vue";
import {
  getCompletePids,
  getUploadPath,
  startAssess,
  getAllIps,
  downloadReportFile,
  assessInitData,
  sysUploadPath,
  getLeftIps,
  getRightIps,
  getOpenGauss,
  getMysql,
  getListResult,
  deleteAssess
} from "@/api/ops";
import { Message } from "@arco-design/web-vue";
import {
  IconUpload,
  IconFileAudio,
  IconClose,
  IconFaceFrownFill,
} from "@arco-design/web-vue/es/icon";
import { useI18n } from "vue-i18n";
import { async } from "@antv/x6/lib/registry/marker/async";

const list = reactive({
  data: [],
  page: {
    total: 0,
    pageSize: 10,
    "show-total": true,
  },
  loading: false,
});
const filter = reactive({
  pageNum: 1,
  pageSize: 10,
});
const columns = computed(() => [
  {
    title: t('home.index.5mq3d6o5z6g15'),
    dataIndex: "sqlInputType",
    ellipsis: true,
    width: 80,
    align: "left",
    tooltip: true,
  },
  {
    title: t('home.index.5mq3d6o5z6g2'),
    dataIndex: "assessmenttype",
    ellipsis: true,
    width: 80,
    align: "left",
    tooltip: true,
  },
  {
    title: t('home.index.5mq3d6o5z6g3'),
    dataIndex: "sqltype",
    ellipsis: true,
    align: "left",
    width: 100,
  },
  {
    title: t('home.index.5mq3hrx8s6o0'),
    dataIndex: "mysqlHost",
    slotName: "mysqlHost",
    ellipsis: true,
    width: 140,
    align: "left",
    // tooltip: true,
  },
  {
    title: t('home.index.5mq3d6o677g0'),
    dataIndex: "opengaussHost",
    slotName: "opengaussHost",
    ellipsis: true,
    width: 140,
    align: "left",
    // tooltip: true,
  },
  {
    title: t('home.index.5mq3ijy1zw41'),
    dataIndex: "startTime",
    ellipsis: true,
    align: "left",
    width: 100,
  },
  {
    title: t("packageManage.index.5myq5c8zq380"),
    slotName: "operation",
    align: "center",
    width: 160,
  },
]);
const { t } = useI18n();
const type = ref("text");

const getCustomIcon = () => {
  return {
    retryIcon: () => <IconUpload />,
    cancelIcon: () => <IconClose />,
    fileIcon: () => <IconFileAudio />,
    removeIcon: () => <IconClose />,
    errorIcon: () => <IconFaceFrownFill />,
    fileName: (file) => {
      return `文件名： ${file.name}`;
    },
  };
};

let timer = null;

let proccessId = "Attach Application proccess Id";

const isFileInputType = computed(() => formCommon.sqlInputType === "file");

const isCollectInputType = computed(
  () => formCommon.sqlInputType === "collect"
);

const isPidType = computed(
  () => formCommon.sqlInputType === "Attach_Application"
);

const isSqlType = computed(
  () =>
    formCommon.sqlInputType === "collect" &&
    (formCommon.assessmenttype === "sql" || formCommon.assessmenttype === "all")
);

const modalVisible = ref(false);
const formCommon = reactive({
  assessmenttype: "",
  sqltype: "",
  sqlInputType: "collect",
  osuser: "",
  ospassword: "",
  proccessPid: "",
  typeChecked: "zip",
  file: [],
});

const formLeft = reactive({
  mysqlHost: "",
  mysqlPort: "",
  mysqlUser: "",
  mysqlPassword: "",
  mysqlDbname: "",
});

const formRight = reactive({
  opengaussHost: "",
  opengaussPort: "",
  opengaussUser: "",
  opengaussPassword: "",
  opengaussDbname: "",
});

let filePath = ref("");
const oldStatus = ref(0);
const loading = ref(false);
const loading1 = ref(false);
const formLeftRef = ref();
const formCommonRef = ref();
const formRightRef = ref();
const code = ref(`  <span style="color: #9d0006;">[mysqld]</span>
  <span style="color: #b57614;">binlog_format</span> = <span style="color: #797403;">ROW</span>
  <span style="color: #b57614;">log_bin</span> = <span style="color: #797403;">mysql-bin</span>
  <span style="color: #b57614;">server_id</span> = <span style="color: #8f3f71;">1</span>
  <span style="color: #b57614;">binlog_row_image</span> = <span style="color: #797403;">FULL</span>
  <span style="color: #b57614;">enforce_gtid_consistency</span> = <span style="color: #797403;">ON</span>
  <span style="color: #b57614;">gtid_mode</span> = <span style="color: #797403;">ON</span>`);

const options = ref([]); // Initialize an empty options array
const completePidList = ref([]);
const rightIpsList = ref([]);
const leftIpsList = ref([]);
const rightDbList = ref([]);
const leftDbList = ref([]);
const modalContent = ref();
const modalContainer = ref();
// Fetch the options when the component is mounted
onMounted(async () => {
  formCommon.sqlInputType === "collect";
  modalContainer.value = document.querySelector('.downloadContainer');
   await getListData()
  try {
    const [getLeftList, getRightList, pids, uploadPath] = await Promise.all([
      getLeftIps(),
      getRightIps(),
      getCompletePids(),
      getUploadPath()
    ]);
    completePidList.value = pids.obj.map((item) => ({
      label: item,
      value: item,
    }));

    leftIpsList.value = getLeftList.obj.map((ip) => ({
      label: ip,
      value: ip,
    }));
    rightIpsList.value = getRightList.obj.map((ip) => ({
      label: ip,
      value: ip,
    }));
    filePath.value = uploadPath.obj
  } catch (error) {
    console.error(error);
  }
});

const onTypeChange = () => {
  formCommon.file = []
};
const currentPage = (e) => {
  filter.pageNum = e;
  getListData();
};
const getListData = () => {
  list.loading = true;
  getListResult(filter)
    .then((res) => {
      if (Number(res.code) === 200) {
        let resObj = res.obj.map((row) => {
          return {
            "assessmentId": row.assessmentId ,
            "sqlInputType": row.sqlInputType || "-",
            "proccessPid": row.proccessPid ,
            "assessmenttype": row.assessmenttype || "-",
            "filedir": row.filedir ,
            "sqltype": row.sqltype || "-",
            "mysqlPassword": row.mysqlPassword || "-",
            "mysqlUser": row.mysqlUser || "-",
            "mysqlPort": row.mysqlPort || "-",
            "mysqlHost": row.mysqlHost || "-",
            "mysqlDbname": row.mysqlDbname || "-",
            "opengaussUser": row.opengaussUser || "-",
            "opengaussPassword": row.opengaussPassword || "-",
            "opengaussPort": row.opengaussPort || "-",
            "opengaussHost": row.opengaussHost || "-",
            "opengaussDbname": row.opengaussDbname || "-",
            "startTime": row.startTime || "-",
            "reportFileName": row.reportFileName || "-"
          };
        });
        list.data = resObj;
        list.page.total = res.total;
      }
    })
    .finally(() => {
      list.loading = false;
    });
};
const deleteRows = (record) => {
  deleteAssess(record.assessmentId).then((res) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: "delete success",
      });
      getListData();
    }
  });
};
const getData = (flg) => {
  if (flg) {
    getOpenGauss(formRight.opengaussHost).then((res) => {
      rightDbList.value = res.obj.dbNames.map((dbName) => ({
        label: dbName,
        value: dbName
      }));
      let parma = res.obj.node;
      formRight.opengaussHost = parma.ip,
      formRight.opengaussPort= parma.port,
      formRight.opengaussUser = parma.username,
      formRight.opengaussPassword = '',
      formRight.opengaussDbname = res.obj.dbNames.length > 0 ? res.obj.dbNames[0] : ''
    });
  } else {
    getMysql(formLeft.mysqlHost).then((res) => {
      leftDbList.value = res.obj.dbNames.map((dbName) => ({
        label: dbName,
        value: dbName,
      }));
      let parma = res.obj.node;
      formLeft.mysqlHost = parma.ip,
      formLeft.mysqlPort= parma.port,
      formLeft.mysqlUser = parma.username,
      formLeft.mysqlPassword = '',
      formLeft.mysqlDbname = res.obj.dbNames.length > 0 ? res.obj.dbNames[0] : ''
    });
  }
};
const beforeUpload = (file) => {
  let name = ["zip"];
  let fileName = file.name.split(".");
  let fileExt = fileName[fileName.length - 1];
  let isTypeOk = name.indexOf(fileExt) >= 0;
  let isSize = 1024 * 1024 * 500;
  if (!isTypeOk) {
    Message.error(t('home.index.1mq3hrx8s6o9'));
  } else if (file.size > isSize) {
    Message.error(t('home.index.1mq3hrx8s610'));
  } else {
    return new Promise((resolve, reject) => {
    file['fileId'] = new Date().getTime()
    formCommon.file = file
    resolve(true);
    console.log(formCommon);
  })
  }
};
const deletedata = (file) => {
  // formCommon.file = formCommon.file.filter(
  //   (item) => file.file.fileId !== item.fileId
  // );
  formCommon.file = []
  return new Promise((resolve, reject) => {
    resolve(true);
  });
};
const handleCancel = () => {
  modalVisible.value = false;
}
const fileDownload = (fileName) => {
  loading1.value = true;
  downloadReportFile(fileName)
    .then((res) => {
      if (res) {
        const blob = new Blob([res], {
          type: "application/octet-stream",
        });
        const a = document.createElement("a");
        const URL = window.URL || window.webkitURL;
        const herf = URL.createObjectURL(blob);
        a.href = herf;
        a.download = "report.html";
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(herf);
        var reader = new FileReader();
        reader.readAsText(blob);
        reader.onload = () => {
          modalContent.value = reader.result;
          analysisScript(modalContent.value);
        };
        modalVisible.value = true;
      }
      loading1.value = false;
    })
    .catch((error) => {
      loading1.value = false;
      console.error("Error downloading file:", error);
    });
};
const analysisScript = (data) => {
  let mutiScriptSelect = /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi;
  var scriptArray = data.match(mutiScriptSelect);
  nextTick(() => {
    scriptArray.forEach((item) => {
      var scriptSelect = /<script>([\s\S]*?)<\/script>/;
      if (item.match(scriptSelect)) {
        let scriptElement = document.createElement('script');
        scriptElement.innerHTML = item.match(scriptSelect)[1];
        document.getElementsByTagName('body')[0].appendChild(scriptElement);
      }
    })
  })
};
const downloadStatus = reactive({
  progress: 0.0,
  downloading: false,
});
 const getFormData = (object) => {
    const formData = new FormData()
    Object.keys(object).forEach(key => {
        const value = object[key]
        if (Array.isArray(value)) {
            value.forEach((subValue, i) =>
                formData.append(key + `[${i}]`, subValue)
            )
        } else {
            formData.append(key, object[key])
        }
    })
    return formData
}
const formRules = computed(() => {
  return {
    mysqlHost: [
      { required: isCollectInputType, message: t("home.index.5mq3d6o5zb00") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            const reg =
              /^(1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|[0-9])\.((1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|\d)\.){2}(1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|\d)$/;
            const re = new RegExp(reg);
            if (re.test(value)) {
              resolve(true);
            } else {
              cb(t("home.index.5mq3d6o5zb01"));
              resolve(false);
            }
          });
        },
      },
    ],
    mysqlPort: [
      { required: isCollectInputType, message: t("home.index.5mq3d6o60gk0") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            const reg =
              /^([0-9]|[1-9]\d{1,3}|[1-5]\d{4}|6[0-4]\d{4}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/;
            const re = new RegExp(reg);
            if (re.test(value)) {
              resolve(true);
            } else {
              cb(t("home.index.5mq3d6o60gk1"));
              resolve(false);
            }
          });
        },
      },
    ],
    mysqlUser: [
      { required: isCollectInputType, message: t("home.index.5mq3d6o60t00") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("home.index.5mq3d6o60t00"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    mysqlPassword: [
      { required: isCollectInputType, message: t("home.index.5mq3d6o615g0") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("home.index.5mq3d6o615g0"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    mysqlDbname: [
      { required: isCollectInputType, message: t("home.index.5mq3ijy1z1s0") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("home.index.5mq3ijy1z1s0"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    opengaussHost: [
      { required: true, message:  t("home.index.5mq3d6o5zb00")},
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            const reg =
              /^(1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|[0-9])\.((1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|\d)\.){2}(1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|\d)$/;
            const re = new RegExp(reg);
            if (re.test(value)) {
              resolve(true);
            } else {
              cb(t("home.index.5mq3d6o5zb01"));
              resolve(false);
            }
          });
        },
      },
    ],
    opengaussPort: [
      { required: true, message: t("home.index.5mq3d6o60gk0") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            const reg =
              /^(1(02[4-9]|0[3-9][0-9]|[1-9][0-9]{2})|[2-9][0-9]{3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$/;
            const re = new RegExp(reg);
            if (re.test(value)) {
              resolve(true);
            } else {
              cb(t("home.index.5mq3d6o60gk1"));
              resolve(false);
            }
          });
        },
      },
    ],
    opengaussUser: [
      { required: true, message: t("home.index.5mq3d6o60t00") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("home.index.5mq3d6o60t00"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    opengaussPassword: [
      { required: true, message: t("home.index.5mq3d6o615g0") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("home.index.5mq3d6o615g0"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    opengaussDbname: [
      { required: true, message: t("home.index.5mq3ijy1z1s0") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("home.index.5mq3ijy1z1s0"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
  };
});
const startSync = async () => {
  const params = {
    osuser: formCommon.osuser,
    ospassword: formCommon.ospassword,
    mysqlHost: formLeft.mysqlHost,
    mysqlPort: formLeft.mysqlPort,
    mysqlUser: formLeft.mysqlUser,
    mysqlPassword: formLeft.mysqlPassword,
    mysqlDbname: formLeft.mysqlDbname,
    opengaussHost: formRight.opengaussHost,
    opengaussPort: formRight.opengaussPort,
    opengaussUser: formRight.opengaussUser,
    opengaussPassword: formRight.opengaussPassword,
    opengaussDbname: formRight.opengaussDbname,
  };
  let data = getFormData(params)
  formCommon.sqlInputType === 'file' ? data.append("file", formCommon.file) : ''
  formCommon.sqlInputType === 'collect' ? data.append("assessmenttype", formCommon.assessmenttype) : ''
  formCommon.sqlInputType === 'collect' && formCommon.assessmenttype !== 'object' ? data.append("sqltype", formCommon.sqltype) : ''
  formCommon.sqlInputType === 'Attach_Application' ? data.append("proccessPid", formCommon.proccessPid) : ''
  const validCommon = await formCommonRef.value?.validate();
  const validRight = await formRightRef.value?.validate();
  const validLeft = await formLeftRef.value?.validate();
  if (formCommon.sqlInputType === "file" || formCommon.sqlInputType === "Attach_Application") {
    if (validCommon !== undefined || validRight !== undefined) {
      return;
    }
  } else if (formCommon.sqlInputType === "collect") {
    if (validCommon !== undefined || validRight !== undefined || validLeft !== undefined) {
      return;
    }
  }
  downloadStatus.downloading = true;
  downloadStatus.progress = 0;
  let progess = 0;
  const interval = setInterval(() => {
    progess += Math.random() * (0.08 - 0.01) + 0.01;
    if (progess < 0.9) {
      downloadStatus.progress = Math.floor((progess / 1) * 100) / 100;
    } else {
      downloadStatus.progress = Math.floor((0.99 / 1) * 100) / 100;
    }
  }, 3000);
    loading.value = true;
  startAssess(data, formCommon.sqlInputType).then((res) => {
    if (Number(res.code) === 200) {
      Message.success(res.msg);
      getListData()
    } else {
      Message.error(
        "Sync error, please check whether the configuration is correct"
      );
    }
    downloadStatus.progress = 0;
    downloadStatus.downloading = false;
    clearInterval(interval);
    loading.value = false;
  }).catch((error) => {
    downloadStatus.progress = 0;
    downloadStatus.downloading = false;
    clearInterval(interval);
    loading.value = false;
  });;
};

// clear timer
const clearTimer = () => {
  timer && clearTimeout(timer);
};

onUnmounted(() => {
  clearTimer();
});
</script>
<style>
body,
html {
  width: 100% !important;
}
.downloadContainer .arco-modal-container{
    height: 100%;
    position: sticky;
}
</style>
<style lang="less" scoped>
.formCommon {
  width: calc(100%);
}
.button {
  width: 30%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 100px;
  justify-content: center; /* 添加此样式 */
}

.button .a-button {
  flex: 1;
}

.downloadContainer{
  overflow-y: auto;
  position: relative;
  width: 100%;
  height: 100%;
  .arco-modal-container{
    height: 100%;
    position: sticky;
  }
}

.home-container {
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  .tips-con {
    width: 1590px;
    margin-left: 0px;
    background: rgb(255, 247, 232);
    margin-bottom: 20px;
    font-size: 14px;
    :deep(.arco-collapse) {
      background-color: rgb(255, 247, 232);
      border: 1px solid transparent;
      .arco-collapse-item-header {
        background-color: rgb(255, 247, 232);
        color: rgb(29, 33, 41);
      }
      .arco-collapse-item-content {
        padding-left: 16px;
        padding-right: 16px;
        background: rgb(255, 247, 232);
      }
      .arco-icon-hover {
        color: rgb(255, 125, 0);
      }
    }
    .tips {
      line-height: 20px;
      color: rgb(29, 33, 41);
      margin-bottom: 5px;
      display: flex;
      .num {
        width: 20px;
        flex: 0 0 auto;
      }
    }
    .code {
      margin-top: 5px;
      margin-bottom: 5px;
      margin-left: 20px;
      white-space: pre-wrap;
      background: #fcf1d6;
      border-radius: 4px;
      line-height: 20px;
      padding: 10px;
    }
  }
  .form-con {
    width: 70%;
    display: flex;
    justify-content: space-between;
    .form-center {
      margin-left: 40px;
      margin-right: 200px;
      color: #999;
    }
  }
  .btn-con {
    margin-top: 30px;
  }
  :deep(.arco-upload-progress) {
    display: none !important;
  }
}
</style>
