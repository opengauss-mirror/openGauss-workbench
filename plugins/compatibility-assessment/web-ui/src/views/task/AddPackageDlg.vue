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
          @change="onTypeChange"
          dropdown-style="{ top: 'auto', bottom: 0 }"
        >
          <a-option
            v-for="(item, index) in ipList"
            :key="index"
            :value="item.value"
            :label="item.label"
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
      >
        <a-range-picker
          style="width: 100%"
          show-time
          :default-value="getCurrentTime"
          :allow-clear="false"
          :time-picker-props="{ defaultValue: ['00:00:00', '23:59:59'] }"
          format="YYYY-MM-DD HH:mm:ss"
          @ok="dateOnOk"
        />
      
      </a-form-item>
        <a-form-item
          :label="$t('packageManage.AddPackageDlg.5myq5c8zpu01')"
          field="timeInterval"
        >
        <a-input-tag v-model="data.formData.timeInterval"  :default-value="getCurrentTime" :style="{width:'100%'}" :placeholder="$t('packageManage.AddPackageDlg.5myq5c8zpu02')" :max-tag-count="3" allow-clear/>
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
  analysisPkg,
  delPkgTar,
  editPackage,
  hasPkgName,
} from "@/api/ops";
import { FileItem, Message } from "@arco-design/web-vue";
import { useI18n } from "vue-i18n";
import { OpenGaussVersionEnum } from "@/types/ops/install";
import { getAllIps, getAllPids } from "@/api/ops"; // eslint-disable-line
import { UploadInfo } from "@/types/resource/package";
import { Modal } from "@arco-design/web-vue";
import dayjs from "dayjs";

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
    startTime: "",
    endTime: "",
    pid: "",
    filePath: "",
    remark: "",
    timeInterval:[]
  },
  rules: {},
  typeList: [],
});

const getCurrentTime = computed(() => {
  const startTime = dayjs().add(2, "minutes").format("YYYY-MM-DD HH:mm:ss");
  const endTime = dayjs().add(2, "hour").format("YYYY-MM-DD HH:mm:ss");
  console.log([startTime+'-'+endTime])
  return [startTime, endTime];
});

const remove = (tag: any) => {
  const index = data.formData.timeInterval.indexOf(tag.value);
  if (index !== -1) {
    data.formData.timeInterval.splice(index, 1);
  }
};
const checkAndAddTimeRange = (selectedStartTime: any, selectedEndTime: any, timeRanges: any) => {
  let shouldAdd = true;
  for (const [startTime, endTime] of timeRanges) {
    if (
      (new Date(selectedStartTime).getTime() >= new Date(startTime).getTime() && new Date(selectedStartTime).getTime() <= new Date(endTime).getTime()) ||
      (new Date(selectedEndTime).getTime() >= new Date(startTime).getTime() && new Date(selectedEndTime).getTime() <= new Date(endTime).getTime()) ||
      (new Date(selectedStartTime).getTime() <= new Date(startTime).getTime() && new Date(selectedEndTime).getTime() >= new Date(endTime).getTime())
    ) {
      shouldAdd = false;
      break;
    }
  }
  if (shouldAdd) {
    timeRanges.push([selectedStartTime+'~'+selectedEndTime]);
  }
  return shouldAdd;
}
const dateOnOk = (date: any) => {
  data.formData.remark = "时间段";
    if (!checkAndAddTimeRange(date[0], date[1], data.formData.timeInterval)) {
      Message.warning(t("packageManage.AddPackageDlg.5myq5c8zpu03"));
    }
};
const emits = defineEmits([`finish`]);

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
    Object.assign(data.formData, {
      taskId: "",
      host: "",
      taskName: "",
      startTime: "",
      endTime: "",
      pid: "",
    });
  } else {
    data.title = t("packageManage.AddPackageDlg.5myq6nnebwo0");
    if (packageData) {
      Object.assign(data.formData, {
        taskId: packageData.taskId,
        taskName: packageData.taskName,
        host: packageData.host,
        startTime: packageData.startTime,
        endTime: packageData.endTime,
        pid: packageData.pid,
      });
    }
  }
  initData();
};

const ipList = ref<KeyValue[]>([]);

const getIpList = async () => {
  try {
    const res = await getAllIps();
    if (Number(res.code) === 200) {
      ipList.value = res.obj.map((item) => ({
        label: item,
        value: item,
      }));
    }
  } catch (error) {
    console.error(error);
  }
};

const pidList = ref<KeyValue[]>([]);

const getPidList = async () => {
  let host = data.formData.host;
  if (!host) {
    Message.error(t("packageManage.index.5myq5c8zu510"));
    return;
  }
  try {
    const res = await getAllPids(host);
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
:deep(.arco-upload-progress) {
  display: none !important;
}
</style>
