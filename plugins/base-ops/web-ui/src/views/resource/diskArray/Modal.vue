<template>
  <a-modal
    id="disk-array-modal"
    :mask-closable="false"
    :esc-to-close="false"
    :visible="isShow"
    :title="title"
    :unmount-on-close="true"
    :modal-style="{ width: '650px' }"
    @cancel="close"
    @before-open="onOpen"
  >
    <template #footer>
      <div class="flex-between">
        <div class="flex-row">
          <div class="mr label-color" v-if="connectivityStatus !== CONNECTIVITY_STATUS.UNTEST">
            {{ $t("diskArray.currentStatus") }}
          </div>
          <a-tag v-if="connectivityStatus === CONNECTIVITY_STATUS.SUCCESS" color="green">{{
            $t("diskArray.available")
          }}</a-tag>
          <a-tag v-if="connectivityStatus === CONNECTIVITY_STATUS.FAILURE" color="red">{{
            $t("diskArray.unavailable")
          }}</a-tag>
        </div>
        <div>
          <a-button class="mr" @click="close">{{ $t("diskArray.cancel") }}</a-button>
          <a-button :loading="isTestLoading" class="mr" @click="checkConnectivity()">{{
            $t("diskArray.connectivityTest")
          }}</a-button>
          <a-button :loading="isSubmitLoading" type="primary" @click="submit">{{
            $t("diskArray.ok")
          }}</a-button>
        </div>
      </div>
    </template>
    <a-form auto-label-width class="form" :model="data" ref="formRef" :rules="formRules">
      <a-row>
        <a-form-item field="name" :label="$t('diskArray.diskArrayName')" validate-trigger="blur">
          <a-input
            v-model.trim="data.name"
            max-length="50"
            validate-trigger="blur"
            :placeholder="$t('diskArray.inputName')"
            :disabled="props.type === MODAL_TYPE.UPDATE"
          ></a-input>
        </a-form-item>
        <a-form-item field="hostIp" :label="$t('diskArray.hostIp')" validate-trigger="blur">
          <a-input
            v-model.trim="data.hostIp"
            :placeholder="$t('diskArray.inputHostIp')"
            max-length="20"
          ></a-input>
        </a-form-item>
        <a-form-item field="port" :label="$t('diskArray.port')" validate-trigger="blur">
          <a-input
            v-model.trim="data.port"
            :placeholder="$t('diskArray.inputPort')"
            @change="onConnectionFieldChange"
            max-length="10"
          ></a-input>
        </a-form-item>
        <a-form-item field="userName" :label="$t('diskArray.username')" validate-trigger="blur">
          <a-input
            v-model.trim="data.userName"
            :placeholder="$t('diskArray.inputUsername')"
            @change="onConnectionFieldChange"
            max-length="50"
          ></a-input>
        </a-form-item>
        <a-form-item
          field="password"
          :label="$t('diskArray.password')"
          :validate-trigger="['change', 'blur']"
        >
          <a-input-password
            v-model="data.password"
            :placeholder="$t('diskArray.inputPassword')"
            max-length="256"
            allow-clear
            @focus="onPasswordFocus"
            @change="onPasswordChange"
          ></a-input-password>
        </a-form-item>
        <a-form-item field="pairId" :label="$t('diskArray.pairId')" validate-trigger="blur">
          <a-input
            v-model.trim="data.pairId"
            :placeholder="$t('diskArray.inputPairId')"
            max-length="256"
          ></a-input>
        </a-form-item>
      </a-row>
    </a-form>
  </a-modal>
</template>
<script setup lang="ts">
import { ref, computed } from "vue";
import { useI18n } from "vue-i18n";
import { FormInstance } from "@arco-design/web-vue/es/form";

import { CONNECTIVITY_STATUS, MODAL_TYPE } from "./constant";
import {
  apiAddDiskArray,
  apiModifyDiskArray,
  apiCheckConnectivity,
  apiCheckName,
} from "@/api/resource/diskArray";
import { encryptPassword } from "@/utils/jsencrypt";
import { Message } from "@arco-design/web-vue";

const { t } = useI18n();

const props = defineProps({
  isShow: {
    type: Boolean,
    default: () => false,
  },
  type: {
    type: Number,
    default: () => MODAL_TYPE.CREATE,
  },
  data: {
    type: Object,
    default: () => {},
  },
});

const emit = defineEmits(["updateList", "close"]);

const formRef = ref<FormInstance>();
const connectivityStatus = ref(CONNECTIVITY_STATUS.UNTEST);
const isTestLoading = ref(false);
const isSubmitLoading = ref(false);
const isPasswordChange = ref(false);

const title = computed(
  () =>
    (props.type === MODAL_TYPE.CREATE ? t("diskArray.create") : t("diskArray.update")) +
    t("diskArray.index")
);

import { IpRegex } from '../../../../../../../openGauss-datakit/visualtool-ui/src/types/global'

const formRules = computed(() => {
  return {
    name: [
      { required: true, message: t("diskArray.inputName") },
      {
        validator: (value: any, cb: any) => {
          return new Promise(async (resolve) => {
            if (props.type === MODAL_TYPE.UPDATE) {
              resolve(true);
            } else {
              const regExp = /[\"\'{}()\[\]^%|,;&$><`\\\-!\n]/;
              if (regExp.test(value)) {
                resolve(false);
                cb(t("diskArray.illegalCharacters"));
              } else {
                const res: any = await apiCheckName(value).catch(() => {
                  cb(t("diskArray.duplicatedName"));
                  resolve(false);
                });
                if (res?.code == 200 && res?.data) {
                  cb(t("diskArray.duplicatedName"));
                  resolve(false);
                } else {
                  resolve(true);
                }
              }
            }
          });
        },
      },
    ],
    hostIp: [
      { required: true, message: t("diskArray.inputHostIp") },
      {
        validator: (value: any, cb: any) => {
          return new Promise((resolve) => {
            if (IpRegex.ipv4Reg.test(value) || IpRegex.ipv6Reg.test(value)) {
              resolve(true)
            } else {
              cb(t("diskArray.wrongHostIp"));
              resolve(false);
            }
          });
        },
      },
    ],
    port: [
      { required: true, message: t("diskArray.inputPort") },
      {
        validator: (value: any, cb: any) => {
          return new Promise((resolve) => {
            const reg =
              /^([0-9]|[1-9]\d{1,3}|[1-5]\d{4}|6[0-4]\d{4}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/;
            const re = new RegExp(reg);
            if (re.test(value)) {
              resolve(true);
            } else {
              cb(t("diskArray.wrongPort"));
              resolve(false);
            }
          });
        },
      },
    ],
    userName: [
      { required: true, message: t("diskArray.inputUsername") },
      {
        validator: (value: any, cb: any) => {
          return new Promise((resolve) => {
            const regExp = /[|;&$><`\\!\n]/;
            if (regExp.test(value)) {
              resolve(false);
              cb(t("diskArray.illegalCharacters"));
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    password: [{ required: true, message: t("diskArray.inputPassword") }],
    pairId: [
      { required: true, message: t("diskArray.inputPairId") },
      {
        validator: (value: any, cb: any) => {
          return new Promise((resolve) => {
            const regExp = /[|;&$><`\\!\n]/;
            if (regExp.test(value)) {
              resolve(false);
              cb(t("diskArray.illegalCharacters"));
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
  };
});

const close = () => {
  emit("close");
};

const onOpen = () => {
  connectivityStatus.value = CONNECTIVITY_STATUS.UNTEST;
  isTestLoading.value = false;
  isPasswordChange.value = false;
};

const onPasswordChange = () => {
  isPasswordChange.value = true;
  connectivityStatus.value = CONNECTIVITY_STATUS.UNTEST;
};

const onConnectionFieldChange = () => {
  connectivityStatus.value = CONNECTIVITY_STATUS.UNTEST;
};

const checkConnectivity = async () => {
  if (isTestLoading.value) {
    return;
  }
  isTestLoading.value = true;
  const param = {
    hostIp: props.data.hostIp,
    name: props.data.name,
    pairId: props.data.pairId,
    port: props.data.port,
    userName: props.data.userName,
    password: props.data.password,
  };
  if (isPasswordChange.value) {
    props.data.password = await encryptPassword(props.data.password).catch(() => {});
    param.password = props.data.password;
    isPasswordChange.value = false;
  }
  const res: any = await apiCheckConnectivity(param).catch(() => {
    connectivityStatus.value = CONNECTIVITY_STATUS.FAILURE;
  });
  if (res?.code == 200) {
    connectivityStatus.value = CONNECTIVITY_STATUS.SUCCESS;
  } else {
    connectivityStatus.value = CONNECTIVITY_STATUS.FAILURE;
  }
  isTestLoading.value = false;
};

const submit = async () => {
  const formError = await formRef.value?.validate();
  if (formError || isTestLoading.value) {
    return;
  }
  if (connectivityStatus.value !== CONNECTIVITY_STATUS.SUCCESS) {
    await checkConnectivity();
  }
  if (connectivityStatus.value !== CONNECTIVITY_STATUS.SUCCESS) {
    return;
  }
  const param = {
    hostIp: props.data.hostIp,
    name: props.data.name,
    pairId: props.data.pairId,
    port: props.data.port,
    userName: props.data.userName,
    password: props.data.password,
  };
  isSubmitLoading.value = true;
  if (props.type === MODAL_TYPE.CREATE) {
    const res: any = await apiAddDiskArray(param).catch(() => {
      Message.error("network error");
    });
    if (res?.code == 200) {
      Message.success(`${t("diskArray.create")}${t("diskArray.index")}${t("diskArray.success")}`);
      emit("updateList");
      emit("close");
    } else {
      Message.error(res?.msg || "network error");
    }
  } else {
    const res: any = await apiModifyDiskArray(param).catch(() => {
      Message.error("network error");
    });
    if (res?.code == 200) {
      Message.success(`${t("diskArray.modify")}${t("diskArray.index")}${t("diskArray.success")}`);
      emit("updateList");
      emit("close");
    } else {
      Message.error(res?.msg || "network error");
    }
  }
  isSubmitLoading.value = false;
};

const onPasswordFocus = () => {
  if (props.type === MODAL_TYPE.UPDATE) {
    props.data.password = "";
  }
};
</script>
<style>
#disk-array-modal {
  .arco-input-wrapper .arco-input[disabled] {
    -webkit-text-fill-color: #c9cdd4;
  }
}
.label-color {
  color: var(--color-text-2);
}
</style>
