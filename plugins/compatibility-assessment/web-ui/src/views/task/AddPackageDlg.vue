<template>
  <a-modal
    unmount-on-close
    :mask-closable="false"
    :esc-to-close="false"
    :ok-loading="submitLoading"
    :visible="data.show"
    :title="data.title"
    :modal-style="{ width: '50vw' }"
    @ok="handleBeforeOk"
    @on-cancel="close(false, $event)"
    @cancel="close(false, $event)"
  >
    <a-form
      :model="data.formData"
      :rules="data.rules"
      ref="formRef"
      auto-label-width
    >
      <a-form-item
        field="taskName"
        :label="$t('packageManage.AddPackageDlg.5myq5c8zpu93')"
      >
        <a-input
          v-model="data.formData.taskName"
          :placeholder="$t('packageManage.AddPackageDlg.5myq5c8zpu95')"
          @input="data.isNameDirty = true"
        />
      </a-form-item>
      <a-form-item field="host" :label="$t('packageManage.index.5myq5c8zpu83')">
        <a-select
          v-model="data.formData.host"
          :placeholder="$t('packageManage.AddPackageDlg.5myq6nneap41')"
          dropdown-style="{ top: 'auto', bottom: 0 }"
          @change="hostChange"
        >
          <a-option
            v-for="(item, index) in ipList"
            :key="index"
            :value="item.publicIp"
            :label="item.publicIp"
          />
        </a-select>
      </a-form-item>
      <a-form-item field="hostUser" :label="$t('packageManage.AddPackageDlg.user')">
        <a-select
          v-model="data.formData.hostUser"
          :placeholder="$t('packageManage.AddPackageDlg.userPlaceholder')"
          dropdown-style="{ top: 'auto', bottom: 0 }"
          @change="hostUserChange"
          @focus="focusUserSelect"
        >
          <a-option
            v-for="(item, index) in userList"
            :key="index"
            :value="item.username"
            :label="item.username"
          />
        </a-select>
      </a-form-item>
      <a-form-item
        field="pid"
        :label="$t('packageManage.AddPackageDlg.5myq6nnebag0')"
      >
        <a-select
          v-model="data.formData.pid"
          :placeholder="$t('packageManage.AddPackageDlg.5myq6nnebew0')"
          dropdown-style="{ top: 'auto', bottom: 0 }"
          @click="handleSelectClick"
        >
          <a-option
            v-for="(item, index) in pidList"
            :key="index"
            :value="item.value"
            :label="item.label"
          />
        </a-select>
      </a-form-item>

      <a-form-item
        field="filePath"
        :label="$t('packageManage.index.4myq5c8zu5w0')"
      >
        <a-input
          v-model="data.formData.filePath"
          :placeholder="$t('packageManage.index.4myq5c8zu5w1')"
          @input="data.isNameDirty = true"
        />
      </a-form-item>

      <a-form-item
        :label="$t('packageManage.AddPackageDlg.5myq6nnebn44')"
        field="timeInterval"
      >
        <a-input-tag v-model="data.formData.timeInterval" :style="{width:'100%'}" :placeholder="$t('packageManage.AddPackageDlg.5myq5c8zpu02')" :max-tag-count="5" allow-clear>
          <template #prefix>
            <a-tooltip>
              <icon-question-circle-fill />
              <template #content>
                <div>{{$t("packageManage.AddPackageDlg.5myq5c8zpu04")}}</div>
                <div>{{$t("packageManage.AddPackageDlg.5myq5c8zpu05")}}</div>
                <div>{{$t("packageManage.AddPackageDlg.5myq5c8zpu06")}}</div>
              </template>
            </a-tooltip>
          </template>
         <template #suffix>
              <a-range-picker
                separator='~'
                show-time
                :allow-clear="false"
                :time-picker-props="{ defaultValue: ['00:00:00', '23:59:59'] }"
                format="YYYY-MM-DD HH:mm:ss"
                @ok="dateOnOk"
                v-model="data.formData.timeNone"
              >
              <icon-calendar />
            </a-range-picker>
          </template> 
        </a-input-tag>
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script lang="ts" setup>
import { KeyValue } from "@/types/global";
import { FormInstance } from "@arco-design/web-vue/es/form";
import { nextTick, reactive, ref, computed, onMounted } from "vue";
import {
  addPackage,
  editPackage,
  getAllIps, 
  getAllPids,
  hostListAll,
  hostUserListAll
} from "@/api/ops";
import { FileItem, Message } from "@arco-design/web-vue";
import { useI18n } from "vue-i18n";
import { Modal } from "@arco-design/web-vue";

const { t } = useI18n();

const config = {
  headers: {
    "Content-Type": "multipart/form-data",
  },
};

const data = reactive<KeyValue>({
  show: false,
  title: "",
  isViewVersion: false,
  formData: {
    taskName: "",
    taskId: "",
    host: "",
    hostUser: "",
    pid: "",
    filePath: "",
    remark: "",
    timeInterval:[],
    timeNone:[]
  },
  rules: {},
  typeList: [],
});

const remove = (tag: any) => {
  const index = data.formData.timeNone.indexOf(tag.value);
  if (index !== -1) {
    data.formData.timeNone.splice(index, 1);
  }
};
const checkAndAddTimeRange = (selectedStartTime: any, selectedEndTime: any, timeRanges: any) => {
  let shouldAdd = '';
  for (let item of timeRanges) {
    if (new Date(item.split('~')[0]).getTime() === new Date(item.split('~')[1]).getTime()) {
      shouldAdd = t("packageManage.AddPackageDlg.5myq5c8zpu07");
      break;
    } else if (new Date(selectedStartTime).getTime() >= new Date(item.split('~')[0]).getTime() && new Date(selectedStartTime).getTime() <= new Date(item.split('~')[1]).getTime()) {
      shouldAdd = t("packageManage.AddPackageDlg.5myq5c8zpu03");
      break;
    } else if (new Date(selectedEndTime).getTime() >= new Date(item.split('~')[0]).getTime() && new Date(selectedEndTime).getTime() <= new Date(item.split('~')[1]).getTime()) {
      shouldAdd = t("packageManage.AddPackageDlg.5myq5c8zpu03");
      break;
    } else if (new Date(selectedStartTime).getTime() <= new Date(item.split('~')[0]).getTime() && new Date(selectedEndTime).getTime() >= new Date(item.split('~')[1]).getTime()) {
      shouldAdd = t("packageManage.AddPackageDlg.5myq5c8zpu03");
      break;
    }
  }
  if (new Date(selectedStartTime).getTime() < new Date().getTime()) {
    shouldAdd = t("packageManage.AddPackageDlg.5myq5c8zpu08");
  } else if (new Date(selectedStartTime).getTime() === new Date(selectedEndTime).getTime()) {
    shouldAdd = t("packageManage.AddPackageDlg.5myq5c8zpu07");
  }
  if (!shouldAdd) {
    timeRanges.push(selectedStartTime+'~'+selectedEndTime);
  }
  return shouldAdd;
}
const dateOnOk = (date: any) => {
  data.formData.remark = "时间段";
    let shouldAdd = '';
    let [selectedStartTime,selectedEndTime] = date
  for (let item of data.formData.timeInterval) {
    if (new Date(item.split('~')[0]).getTime() === new Date(item.split('~')[1]).getTime()) {
      shouldAdd = t("packageManage.AddPackageDlg.5myq5c8zpu07");
      break;
    } else if (new Date(selectedStartTime).getTime() >= new Date(item.split('~')[0]).getTime() && new Date(selectedStartTime).getTime() <= new Date(item.split('~')[1]).getTime()) {
      shouldAdd = t("packageManage.AddPackageDlg.5myq5c8zpu03");
      break;
    } else if (new Date(selectedEndTime).getTime() >= new Date(item.split('~')[0]).getTime() && new Date(selectedEndTime).getTime() <= new Date(item.split('~')[1]).getTime()) {
      shouldAdd = t("packageManage.AddPackageDlg.5myq5c8zpu03");
      break;
    } else if (new Date(selectedStartTime).getTime() <= new Date(item.split('~')[0]).getTime() && new Date(selectedEndTime).getTime() >= new Date(item.split('~')[1]).getTime()) {
      shouldAdd = t("packageManage.AddPackageDlg.5myq5c8zpu03");
      break;
    }
  }
  if (new Date(selectedStartTime).getTime() < new Date().getTime()) {
    shouldAdd = t("packageManage.AddPackageDlg.5myq5c8zpu08");
  } else if (new Date(selectedStartTime).getTime() === new Date(selectedEndTime).getTime()) {
    shouldAdd = t("packageManage.AddPackageDlg.5myq5c8zpu07");
  }
  if (!shouldAdd) {
    data.formData.timeInterval.push(selectedStartTime+'~'+selectedEndTime);
    data.formData.timeNone = [selectedStartTime+'~'+selectedEndTime];
    if (data.formData.timeInterval){ formRef.value?.clearValidate("timeInterval"); }
  } else {
    Message.warning(shouldAdd);
  }
  // let messageList = checkAndAddTimeRange(date[0], date[1], data.formData.timeNone)
};
const emits = defineEmits([`finish`,'']);

const submitLoading = ref<boolean>(false);
const formRef = ref<null | FormInstance>(null);
const handleBeforeOk = () => {
  formRef.value?.validate().then((result) => {
    if (!result) {
      submitLoading.value = true;
      const params = new FormData();
      Object.keys(data.formData).map((key) => {
        if (data.formData[key]) {
          params.append(key, data.formData[key]);
        } else {
          params.append(key, "");
        }
      });
      if (data.formData.taskId) {
        // edit
        editPackage(params)
          .then(() => {
            Message.success({ content: `Modified success` });
            emits(`finish`);
            close(false);
          })
          .finally(() => {
            submitLoading.value = false;
          });
      } else {
        addPackage(params)
          .then(() => {
            Message.success({ content: `Create success` });
            emits(`finish`);
            close(false);
          })
          .finally(() => {
            submitLoading.value = false;
          });
      }
    }
  });
};

const close = (flag: boolean, e?: Event) => {
  if (data.type === "create" && flag) {
    return new Promise((resolve, reject) => {
      Modal.confirm({
        title: t("packageManage.AddPackageDlg.5myq6nnecc41"),
        content: t("packageManage.AddPackageDlg.5myq6nnecc42"),
        onOk: () => {
          doClose();
        },
        onCancel: () => reject("cancel"),
      });
    });
  } else {
    doClose();
  }
};

const doClose = () => {
  data.show = false;
  nextTick(() => {
    formRef.value?.clearValidate();
    formRef.value?.resetFields();
  });
};

const open = (
  type: string,
  packageData?: KeyValue,
  defaultVersion?: string
) => {
  getIpList();
  data.show = true;
  data.type = type;
  if (type === "create") {
    data.title = t("packageManage.AddPackageDlg.5myq6nnebrc0");
    // init formData
      data.formData.timeInterval = [];
      data.formData.timeNone = [];
    Object.assign(data.formData, {
      taskId: "",
      host: "",
      taskName: "",
      hostUser: "",
      pid: "",
      filePath: ""
    });
  } else {
    data.title = t("packageManage.AddPackageDlg.5myq6nnebwo0");
    if (packageData) {
      let time_Interval = []
      let packageData_timeInterval = packageData.timeInterval
      if (packageData_timeInterval.length > 1) {
          time_Interval = packageData_timeInterval.split(",");
      } else {
        time_Interval = [packageData.timeInterval]
      }
      const { taskId, taskName, host, hostUser, pid, filePath } = packageData
      Object.assign(data.formData, {
        taskId,
        taskName,
        host,
        hostUser,
        timeInterval: time_Interval,
        timeNone: [],
        pid,
        filePath
      });
    }
  }
  initData();
};

const ipList = ref<KeyValue[]>([]);

const getIpList = async () => {
  try {
    const res = await hostListAll();
    if (Number(res.code) === 200) {
      ipList.value = res.data;
    }
  } catch (error) {
    console.error(error);
  }
};

const userList = ref<KeyValue[]>([])
const hostChange = async () =>{
  data.formData.hostUser = ''
  data.formData.pid = ''
  try {
    const hostId = ipList.value.find(item => item.publicIp === data.formData.host)?.hostId
    const res = await hostUserListAll(hostId)
    if(Number(res.code)===200){
      userList.value = res.data
    }
  } catch (error) {
    console.log(error)
  }
}

const hostUserChange = () => {
  data.formData.pid = ''
}

const focusUserSelect = () => {
    if (!data.formData.host) {
      Message.error(t("packageManage.index.5myq5c8zu510"));
      return;
    }
}

const pidList = ref<KeyValue[]>([]);

const getPidList = async () => {
  pidList.value = []
  const host = data.formData.host;
  const hostUser = data.formData.hostUser
  if (!host) {
    Message.error(t("packageManage.index.5myq5c8zu510"));
    return;
  }
  if (!hostUser) {
    Message.error(t("packageManage.AddPackageDlg.userTip"));
    return;
  }
  try {
    const res = await getAllPids(host,hostUser);
    if (Number(res.code) === 200) {
      pidList.value = res.obj.map((item) => ({
        label: item,
        value: item,
      }));
    }
  } catch (error) {
    console.error(error);
  }
};

const handleSelectClick = () => {
  getPidList();
};

const initData = () => {
  data.rules = {
    pid: [
      {
        required: true,
        "validate-trigger": "blur",
        message: t("packageManage.AddPackageDlg.5myq6nnebew0"),
      },
    ],
    taskName: [
      {
        required: true,
        message: t("packageManage.AddPackageDlg.5myq5c8zpu95"),
      },
    ],
    filePath: [
      {
        required: true,
        message: t("packageManage.index.4myq5c8zu5w1"),
      },
    ],
    host: [
      {
        required: true,
        message: t("packageManage.AddPackageDlg.5myq5c8zpu96"),
      },
    ],
    hostUser: [
      {
        required: true,
        message: t("packageManage.AddPackageDlg.userPlaceholder"),
      },
    ],
    timeInterval: [
      {
        required: true,
        message: t("packageManage.AddPackageDlg.5myq5c8zpu99"),
      },
    ],
  };
};

defineExpose({
  open,
});
</script>
<style lang="less" scoped>
.upload-info {
  background: var(--color-fill-2);
  border: 1px dotted var(--color-fill-4);
  height: 120px;
  width: 100%;
  border-radius: 2px;

  .tips-1 {
    font-size: 14px;
    color: var(--color-text-2);
    margin-bottom: 12px;

    .highlight {
      color: rgb(var(--primary-6));
    }
  }
}
.iconClass {
    margin: 0 10px;
    font-size: 18px;
    color: #165DFF;
}
:deep(.arco-upload-progress) {
  display: none !important;
}
</style>
